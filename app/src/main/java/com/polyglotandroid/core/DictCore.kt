package com.polyglotandroid.core

import com.polyglotandroid.core.collections.ConWordCollection

class DictCore (_polyGlot: PolyGlot) {
    private val polyGlot: PolyGlot = _polyGlot
    private var wordCollection: ConWordCollection
    // TODO: other properties
    init {
        try {
            wordCollection = ConWordCollection(this)
        }
    }
}