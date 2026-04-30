package com.methoda.tranquillo.data

import com.methoda.tranquillo.ui.components.StoneKind
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class StonesRepository(private val dao: StoneDao) {

    val allStones: Flow<List<StoneKind>> =
        dao.all().map { rows -> rows.mapNotNull { row -> row.kind.toStoneKindOrNull() } }

    val count: Flow<Int> = dao.count()

    suspend fun addStone(kind: StoneKind, source: String) {
        dao.insert(
            StoneEntity(
                kind = kind.name,
                source = source,
                awardedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun clear() = dao.clear()

    companion object {
        private fun String.toStoneKindOrNull(): StoneKind? =
            runCatching { StoneKind.valueOf(this) }.getOrNull()
    }
}
