package com.polyglotandroid.core.nodes

/**
 * A label/combined ID pair for a constructed declension
 */
class DeclensionPair(val combinedId: String, val label: String) {
    override fun toString(): String {
        return label
    }
}