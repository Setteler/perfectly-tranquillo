package com.methoda.tranquillo.data

/**
 * Re-export of the visual StoneKind as a data-layer alias so screens and
 * view-model code can depend on it without pulling in the UI package.
 *
 * The actual drawing palette lives in `ui.components.Stone`. Both enums are
 * kept in sync — if you add a kind here, add it to the visual enum too.
 */
typealias StoneKind = com.methoda.tranquillo.ui.components.StoneKind
