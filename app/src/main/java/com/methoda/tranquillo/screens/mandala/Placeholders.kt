package com.methoda.tranquillo.screens.mandala

import com.methoda.tranquillo.data.Phase
import com.methoda.tranquillo.data.ResourceKey

/**
 * Placeholder prompt text for the Resource / Challenge fields, keyed by
 * (ResourceKey, Phase). Copied verbatim from `prototype/src/screens-mandala.jsx`
 * `placeholderFor`. All 8 resources × 2 phases × 2 fields = 32 strings.
 */
object PetalPlaceholders {

    data class Pair2(val resource: String, val challenge: String)

    private val AM_RESOURCE: Map<ResourceKey, String> = mapOf(
        ResourceKey.Physical      to "slept deep, woke without alarm…",
        ResourceKey.Intellectual  to "a book waiting on the nightstand…",
        ResourceKey.Emotional     to "feeling soft, patient with myself…",
        ResourceKey.Sensory       to "cool air from the window…",
        ResourceKey.Interactional to "a good morning text from…",
        ResourceKey.Nutritional   to "proper coffee, no rushing…",
        ResourceKey.Contextual    to "desk is tidy, room feels open…",
        ResourceKey.Spiritual     to "a reason to be glad it's today…"
    )

    private val PM_RESOURCE: Map<ResourceKey, String> = mapOf(
        ResourceKey.Physical      to "body feels worked but not wrecked…",
        ResourceKey.Intellectual  to "learned one small thing about…",
        ResourceKey.Emotional     to "had a real laugh with…",
        ResourceKey.Sensory       to "the sunset over the rooftops…",
        ResourceKey.Interactional to "honest conversation with…",
        ResourceKey.Nutritional   to "cooked something, ate slowly…",
        ResourceKey.Contextual    to "home is soft tonight…",
        ResourceKey.Spiritual     to "something felt meaningful when…"
    )

    private val CHALLENGE: Map<ResourceKey, String> = mapOf(
        ResourceKey.Physical      to "tight shoulders, not enough rest…",
        ResourceKey.Intellectual  to "brain feels foggy, scattered…",
        ResourceKey.Emotional     to "feeling thin, touchy…",
        ResourceKey.Sensory       to "too much screen, not enough outside…",
        ResourceKey.Interactional to "lonely corner of the day…",
        ResourceKey.Nutritional   to "ate past when I was hungry…",
        ResourceKey.Contextual    to "space feels cluttered, loud…",
        ResourceKey.Spiritual     to "everything feels flat today…"
    )

    fun forKey(key: ResourceKey, phase: Phase): Pair2 = Pair2(
        resource = (if (phase == Phase.Am) AM_RESOURCE else PM_RESOURCE)[key] ?: "one good thing…",
        challenge = CHALLENGE[key] ?: "one small ache…"
    )

    /** Inline hint line shown just below the header in the PetalSheet. */
    fun hintFor(key: ResourceKey): String = when (key) {
        ResourceKey.Physical      -> "your body — rest, movement, the shape of today"
        ResourceKey.Intellectual  -> "your mind — what's feeding it, what's tiring it"
        ResourceKey.Emotional     -> "your heart — weather inside, how you're meeting it"
        ResourceKey.Sensory       -> "your senses — what you noticed, what you drank in"
        ResourceKey.Interactional -> "connection — who you reached, who reached you"
        ResourceKey.Nutritional   -> "nourish — food, water, the slow meal"
        ResourceKey.Contextual    -> "place — the room around you, the light, the quiet"
        ResourceKey.Spiritual     -> "spirit — meaning, wonder, the quiet big things"
    }
}
