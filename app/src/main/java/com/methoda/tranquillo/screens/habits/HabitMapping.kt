package com.methoda.tranquillo.screens.habits

import com.methoda.tranquillo.data.ResourceKey

/**
 * Seeded habit id → mapped Satir resource.
 *
 * Derived from `docs/design/prototype/src/screens-habits.jsx` `toggle()` and
 * `docs/design/README.md` v1 resource↔habit mapping. Completing a mapped habit
 * adds +0.15 to that resource's PM fill (see HabitsRepository).
 */
object HabitMapping {
    val seeded: Map<String, ResourceKey> = mapOf(
        // dailies
        "no-phone"  to ResourceKey.Contextual,
        "workout"   to ResourceKey.Physical,
        "break"     to ResourceKey.Sensory,
        "eat"       to ResourceKey.Nutritional,
        "water"     to ResourceKey.Nutritional,
        "sleep"     to ResourceKey.Physical,
        "gratitude" to ResourceKey.Emotional,
        // weeklies
        "therapy"    to ResourceKey.Emotional,
        "call-mom"   to ResourceKey.Interactional,
        "meal-prep"  to ResourceKey.Nutritional,
        "long-walk"  to ResourceKey.Physical,
        "deep-clean" to ResourceKey.Contextual,
        "read"       to ResourceKey.Intellectual,
        "date-night" to ResourceKey.Interactional
    )

    fun resourceFor(id: String): ResourceKey? = seeded[id]

    const val FILL_CONTRIBUTION: Float = 0.15f
}
