package com.methoda.tranquillo.data

/**
 * The 8 Satir resources — clockwise from 12 o'clock.
 * Labels are the user-visible short names (see docs/design/README.md § "Data model").
 */
enum class ResourceKey {
    Physical, Intellectual, Emotional, Sensory,
    Interactional, Nutritional, Contextual, Spiritual;

    val label: String get() = when (this) {
        Physical -> "Physical"
        Intellectual -> "Intellectual"
        Emotional -> "Emotional"
        Sensory -> "Sensual"
        Interactional -> "Interactional"
        Nutritional -> "Nutritional"
        Contextual -> "Contextual"
        Spiritual -> "Spiritual"
    }

    companion object {
        /** Ordered clockwise from 12 o'clock, matching the mandala spec. */
        val orderedClockwise: List<ResourceKey> = values().toList()
    }
}

/** Fill state for a resource: AM and PM each in 0f..1f. */
data class AmPmFill(val am: Float = 0f, val pm: Float = 0f)
