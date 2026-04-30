package com.methoda.tranquillo.screens.actions

/**
 * Daily inspirational quotes — soft, on-brand, gentle. The displayed quote is
 * picked deterministically from [MorningQuotes] by day-of-year so the same
 * card shows all day, then rolls to a new one at midnight.
 */
object MorningQuotes {

    val all: List<String> = listOf(
        "Soft today is enough.",
        "You don't have to be sharp. You have to be here.",
        "The day is long enough for tea.",
        "Small things, often.",
        "Begin gently.",
        "Be where your feet are.",
        "Slowness is a skill.",
        "You are allowed to take up the whole morning.",
        "Notice what is already kind in this room.",
        "The world won't fall if you sit a moment longer.",
        "Drink the water.",
        "Open the window before the email.",
        "Tend, don't push.",
        "Whatever you choose first becomes the day.",
        "Move like a tide, not a deadline.",
        "Your body has been carrying you. Thank it.",
        "Light is patient. So is your breath.",
        "Today, leave room for not-knowing.",
        "Make one thing a little softer.",
        "Hold the morning before it holds you.",
        "Choose a sentence for today: kindness, ease, presence.",
        "There is no rush in the garden.",
        "Be a friend to your tired self.",
        "Quiet is also progress.",
        "Let things be a little slower than you'd like.",
        "Let the first hour be yours.",
        "Notice one good ordinary thing.",
        "Little kindness makes longer days.",
        "Steady, like a wave.",
        "You don't have to earn the morning.",
        "Make the simple thing again.",
        "Breath first. Plan later.",
        "Curiosity over urgency, today.",
        "Trust the small movement.",
        "It's okay to do less than you want.",
        "Be soft with the morning, and it will be soft with you.",
        "Begin where you are. That is the only door.",
        "Let warmth find you.",
        "Listen for what's already steady.",
        "Pace, not push."
    )

    /** Deterministic-by-day quote pick — same across the whole day, rolls at midnight. */
    fun forDayOfYear(dayOfYear: Int): String {
        val safe = ((dayOfYear % all.size) + all.size) % all.size
        return all[safe]
    }
}
