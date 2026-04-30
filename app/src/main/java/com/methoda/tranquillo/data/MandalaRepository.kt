package com.methoda.tranquillo.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Pair of (resource text, challenge text) for a given (key, phase). */
data class EntryPair(val resource: String = "", val challenge: String = "")

enum class Phase(val tag: String) {
    Am("am"), Pm("pm");

    companion object {
        fun fromTag(t: String): Phase = if (t == "pm") Pm else Am
    }
}

/**
 * Derives the AM/PM fill for each resource from a list of entries per v1 rules
 * (docs/design/README.md § "Data model"):
 *   resource only ⇒ 0.9
 *   challenge only ⇒ 0.3
 *   both ⇒ 0.6
 *   neither ⇒ 0.0
 */
class MandalaRepository(private val dao: MandalaEntryDao) {

    fun entriesForDate(date: String): Flow<List<MandalaEntryEntity>> = dao.entriesForDate(date)

    fun entryMapForDate(date: String): Flow<Map<Pair<ResourceKey, Phase>, EntryPair>> =
        dao.entriesForDate(date).map { list -> buildEntryMap(list) }

    fun fillsForDate(date: String): Flow<Map<ResourceKey, AmPmFill>> =
        dao.entriesForDate(date).map { list -> buildFills(list) }

    /**
     * Average fill (AM and PM averaged together) per resource over the inclusive
     * date range [startDate, endDate]. Days with no entry for a resource count
     * as 0 in the denominator.
     */
    fun averageFillsInRange(
        startDate: String,
        endDate: String,
        days: Int
    ): Flow<Map<ResourceKey, Float>> =
        dao.entriesInRange(startDate, endDate).map { list -> buildAverages(list, days) }

    /**
     * Per-day fill maps over the inclusive range startDate..endDate. Returns
     * date → resource map; dates with no entries are absent from the map
     * (caller fills 0s).
     */
    fun fillsByDateInRange(
        startDate: String,
        endDate: String
    ): Flow<Map<String, Map<ResourceKey, AmPmFill>>> =
        dao.entriesInRange(startDate, endDate).map { list ->
            list.groupBy { it.date }
                .mapValues { (_, rows) -> buildFills(rows) }
        }

    /** Raw mandala entries between startDate and endDate. Used by the Garden archive. */
    fun entriesInRange(startDate: String, endDate: String): Flow<List<MandalaEntryEntity>> =
        dao.entriesInRange(startDate, endDate)

    suspend fun saveEntry(date: String, key: ResourceKey, phase: Phase, kind: String, text: String) {
        dao.upsertForKeyPhaseKind(date, key.name, phase.tag, kind, text)
    }

    companion object {
        fun buildEntryMap(list: List<MandalaEntryEntity>): Map<Pair<ResourceKey, Phase>, EntryPair> {
            val out = mutableMapOf<Pair<ResourceKey, Phase>, EntryPair>()
            for (e in list) {
                val k = runCatching { ResourceKey.valueOf(e.key) }.getOrNull() ?: continue
                val p = Phase.fromTag(e.phase)
                val pair = out[k to p] ?: EntryPair()
                out[k to p] = when (e.kind) {
                    "resource"  -> pair.copy(resource = e.text)
                    "challenge" -> pair.copy(challenge = e.text)
                    else -> pair
                }
            }
            return out
        }

        fun buildFills(list: List<MandalaEntryEntity>): Map<ResourceKey, AmPmFill> {
            val map = buildEntryMap(list)
            val out = mutableMapOf<ResourceKey, AmPmFill>()
            for (k in ResourceKey.orderedClockwise) {
                val am = fillValue(map[k to Phase.Am])
                val pm = fillValue(map[k to Phase.Pm])
                if (am > 0f || pm > 0f) {
                    out[k] = AmPmFill(am = am, pm = pm)
                }
            }
            return out
        }

        /**
         * Per-resource average over [days] (each resource's daily fill = (am+pm)/2).
         * Days with no rows count as 0 — so the user's actual coverage shows up.
         */
        fun buildAverages(list: List<MandalaEntryEntity>, days: Int): Map<ResourceKey, Float> {
            if (days <= 0) return emptyMap()
            // Group by date → fills map.
            val byDate: Map<String, Map<ResourceKey, AmPmFill>> = list
                .groupBy { it.date }
                .mapValues { (_, rows) -> buildFills(rows) }
            val out = mutableMapOf<ResourceKey, Float>()
            for (key in ResourceKey.orderedClockwise) {
                var sum = 0f
                for ((_, fills) in byDate) {
                    val f = fills[key] ?: continue
                    sum += (f.am + f.pm) / 2f
                }
                out[key] = (sum / days).coerceIn(0f, 1f)
            }
            return out
        }

        private fun fillValue(pair: EntryPair?): Float {
            if (pair == null) return 0f
            val hasR = pair.resource.isNotBlank()
            val hasC = pair.challenge.isNotBlank()
            return when {
                hasR && hasC -> 0.6f
                hasR         -> 0.9f
                hasC         -> 0.3f
                else         -> 0f
            }
        }
    }
}
