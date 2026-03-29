package com.motherledisa.domain.orchestration

/**
 * Multi-tower orchestration modes.
 * Per D-03: Horizontal segmented control for mode switching.
 * Per D-04: Mode selection is global across all towers.
 */
enum class OrchestrationMode(val displayName: String) {
    /** All towers show identical animation simultaneously */
    MIRROR("Mirror"),

    /** Each tower starts with staggered delay (D-06: 0-2000ms between consecutive towers) */
    OFFSET("Offset"),

    /** Tower-to-tower relay: next tower starts when previous completes (D-07: immediate handoff) */
    CASCADE("Cascade"),

    /** Each tower plays independently assigned animation */
    INDEPENDENT("Independent")
}
