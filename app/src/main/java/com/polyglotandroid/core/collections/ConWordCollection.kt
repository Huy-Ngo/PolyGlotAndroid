package com.polyglotandroid.core.collections

import com.polyglotandroid.core.DictCore

class ConWordCollection (_dictCore: DictCore): DictionaryCollection<ConWord> {
    private val SPLIT_CHAR: String = ','
    private val dictCore: DictCore
    private val allConWords: Map<String, Int>
    private val allLocalWords: Map<String, Int>
    private  var orderByLocal: Boolean = false

    init {
        super(ConWord())
    }
}