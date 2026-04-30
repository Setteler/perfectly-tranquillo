package com.methoda.tranquillo.screens.actions

import com.methoda.tranquillo.data.ResourceKey
import com.methoda.tranquillo.data.StoneKind

enum class BreakKind(val label: String) {
    Tiny("Tiny"),
    Sensory("Sensory"),
    Body("Body"),
    World("World")
}

/**
 * One micro-break definition. `stoneColor` picks the radial-gradient hue for
 * the small pebble preview on each list card (matches the prototype's color
 * strings) — it is NOT a StoneKind award; completing any break awards Jade.
 */
data class BreakDef(
    val id: String,
    val kind: BreakKind,
    val title: String,
    val subtitle: String,
    val durationSec: Int,
    val mappedResource: ResourceKey,
    val prompt: String,
    val stoneColor: StoneKind,
    /** If set, tapping this break navigates to the route instead of running
     *  the inline countdown. Used to send "Five-minute breath" to the existing
     *  box-breathing flow at Route.Breath. */
    val redirectRoute: String? = null
)

/**
 * 19 curated micro-breaks, ported verbatim from
 * docs/design/prototype/src/screens-break.jsx. IDs must stay stable — the
 * filter chips reference them by kind, and #5's Stones table will later
 * persist the id when a break is completed.
 */
val Breaks: List<BreakDef> = listOf(
    // Tiny (<=30s)
    BreakDef(
        id = "look-far", kind = BreakKind.Tiny,
        title = "Look far", subtitle = "20 seconds, 20 feet away",
        durationSec = 20, mappedResource = ResourceKey.Sensory,
        prompt = "Rest your eyes on the furthest thing you can see. A tree, a wall, a patch of sky.",
        stoneColor = StoneKind.Moon
    ),
    BreakDef(
        id = "sun", kind = BreakKind.World,
        title = "Go look at the sun", subtitle = "30 seconds of warm light",
        durationSec = 30, mappedResource = ResourceKey.Sensory,
        prompt = "Step to a window or outside. Close your eyes and let the light land on your face.",
        stoneColor = StoneKind.Sand
    ),
    BreakDef(
        id = "sky", kind = BreakKind.World,
        title = "Find a piece of sky", subtitle = "30 seconds, no phone",
        durationSec = 30, mappedResource = ResourceKey.Spiritual,
        prompt = "Look up. Notice the color. Notice what is moving across it.",
        stoneColor = StoneKind.Shell
    ),
    BreakDef(
        id = "water-sip", kind = BreakKind.Tiny,
        title = "Slow sip of water", subtitle = "30 seconds, one glass",
        durationSec = 30, mappedResource = ResourceKey.Nutritional,
        prompt = "One glass, slower than you want to. Feel the temperature. Notice the first swallow.",
        stoneColor = StoneKind.Moon
    ),
    BreakDef(
        id = "three-breaths", kind = BreakKind.Tiny,
        title = "Three long breaths", subtitle = "30 seconds",
        durationSec = 30, mappedResource = ResourceKey.Physical,
        prompt = "In through your nose. Out through your mouth. Three times. That's it.",
        stoneColor = StoneKind.Jade
    ),

    // Sensory (60s)
    BreakDef(
        id = "senses", kind = BreakKind.Sensory,
        title = "Five senses", subtitle = "60 seconds, one of each",
        durationSec = 60, mappedResource = ResourceKey.Sensory,
        prompt = "Name one thing you can see. Hear. Feel. Smell. Taste.",
        stoneColor = StoneKind.Sand
    ),
    BreakDef(
        id = "flower", kind = BreakKind.World,
        title = "Look for a flower", subtitle = "60 seconds, a small hunt",
        durationSec = 60, mappedResource = ResourceKey.Sensory,
        prompt = "Step away and find one flower, anywhere. On a wall, a table, outside. Really look at it.",
        stoneColor = StoneKind.Coral
    ),
    BreakDef(
        id = "cat", kind = BreakKind.World,
        title = "Go pet your cat", subtitle = "60 seconds of softness",
        durationSec = 60, mappedResource = ResourceKey.Interactional,
        prompt = "Pet your cat. Or dog. Or plant. Or a soft blanket. Something living or warm, slowly.",
        stoneColor = StoneKind.Coral
    ),
    BreakDef(
        id = "cold-water", kind = BreakKind.Sensory,
        title = "Cold water, wrists", subtitle = "60 seconds at the sink",
        durationSec = 60, mappedResource = ResourceKey.Physical,
        prompt = "Cool water over your wrists, then your face if you want. Notice the shift.",
        stoneColor = StoneKind.Moon
    ),
    BreakDef(
        id = "smell", kind = BreakKind.Sensory,
        title = "Find a good smell", subtitle = "60 seconds",
        durationSec = 60, mappedResource = ResourceKey.Sensory,
        prompt = "Coffee, a candle, soap, a plant, the outside air. Breathe it in twice, slowly.",
        stoneColor = StoneKind.Coral
    ),
    BreakDef(
        id = "song", kind = BreakKind.Sensory,
        title = "One song, eyes closed", subtitle = "About 3 minutes",
        durationSec = 180, mappedResource = ResourceKey.Emotional,
        prompt = "Pick a song you love. Play it. Close your eyes. That's the whole break.",
        stoneColor = StoneKind.Deep
    ),

    // Body (60–90s)
    BreakDef(
        id = "stretch", kind = BreakKind.Body,
        title = "Soft stretch", subtitle = "90 seconds, slow",
        durationSec = 90, mappedResource = ResourceKey.Physical,
        prompt = "Roll your shoulders. Open your chest. Look up, then slowly side to side.",
        stoneColor = StoneKind.Jade
    ),
    BreakDef(
        id = "shake", kind = BreakKind.Body,
        title = "Shake it off", subtitle = "30 seconds, silly allowed",
        durationSec = 30, mappedResource = ResourceKey.Physical,
        prompt = "Stand up. Shake your hands, arms, legs. Loosely. Make a noise if you want.",
        stoneColor = StoneKind.Sand
    ),
    BreakDef(
        id = "walk", kind = BreakKind.Body,
        title = "A small walk", subtitle = "2 minutes, no phone",
        durationSec = 120, mappedResource = ResourceKey.Physical,
        prompt = "Anywhere. Kitchen, hallway, outside. Phone stays here.",
        stoneColor = StoneKind.Jade
    ),
    BreakDef(
        id = "posture", kind = BreakKind.Body,
        title = "Tall spine", subtitle = "30 seconds",
        durationSec = 30, mappedResource = ResourceKey.Physical,
        prompt = "Feet flat. Crown of head lifted. Shoulders down the back. Three breaths here.",
        stoneColor = StoneKind.Jade
    ),

    // World (connection / environment)
    BreakDef(
        id = "text", kind = BreakKind.World,
        title = "Text someone kind", subtitle = "60 seconds, one message",
        durationSec = 60, mappedResource = ResourceKey.Interactional,
        prompt = "Send one sentence to someone you love. No reason needed. \"thinking of you\" is enough.",
        stoneColor = StoneKind.Coral
    ),
    BreakDef(
        id = "tidy", kind = BreakKind.World,
        title = "Tidy one small thing", subtitle = "60 seconds, one surface",
        durationSec = 60, mappedResource = ResourceKey.Contextual,
        prompt = "Pick one surface. Clear just that. Leave the rest.",
        stoneColor = StoneKind.Shell
    ),
    BreakDef(
        id = "window", kind = BreakKind.World,
        title = "Open a window", subtitle = "60 seconds of fresh air",
        durationSec = 60, mappedResource = ResourceKey.Contextual,
        prompt = "Crack a window. Stand near it. Let the outside in for a minute.",
        stoneColor = StoneKind.Shell
    ),
    BreakDef(
        id = "nothing", kind = BreakKind.Tiny,
        title = "Do absolutely nothing", subtitle = "60 seconds of sky mind",
        durationSec = 60, mappedResource = ResourceKey.Spiritual,
        prompt = "No task. No plan. No phone. Just sit. Notice what arrives.",
        stoneColor = StoneKind.Deep
    ),

    // Longer rests — added in v0.11
    BreakDef(
        id = "five-rest", kind = BreakKind.Body,
        title = "Five-minute rest", subtitle = "5 min, eyes closed",
        durationSec = 300, mappedResource = ResourceKey.Physical,
        prompt = "Lie down or recline. Eyes closed. Soften your jaw, your forehead, your shoulders. " +
                 "Nowhere to be for five minutes.",
        stoneColor = StoneKind.Moon
    ),
    BreakDef(
        id = "five-breath", kind = BreakKind.Body,
        title = "Five-minute breath", subtitle = "box breathing 4·4·4·4",
        durationSec = 300, mappedResource = ResourceKey.Emotional,
        prompt = "Inhale 4, hold 4, exhale 4, hold 4. The animation will guide you.",
        stoneColor = StoneKind.Jade,
        redirectRoute = "breath"
    ),
    BreakDef(
        id = "meditation", kind = BreakKind.Body,
        title = "Quiet meditation", subtitle = "10 min, soft sit",
        durationSec = 600, mappedResource = ResourceKey.Spiritual,
        prompt = "Sit comfortably. Notice your breath without changing it. When the mind wanders, " +
                 "kindly bring it back. Again. And again.",
        stoneColor = StoneKind.Deep
    )
)
