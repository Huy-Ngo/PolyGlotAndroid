package com.polyglotandroid.core

import com.polyglotandroid.core.collections.ConWordCollection

class DictCore (_polyGlot: PolyGlot) {
    fun getTypes(): Any {
        TODO("not yet implemented")
    }

    val types: Any
    private val polyGlot: PolyGlot = _polyGlot
    var wordCollection: ConWordCollection
    var propertiesManager: PropertiesManager
    // TODO: other properties
    init {
        try {
            wordCollection = ConWordCollection(this)
        }
    }
}