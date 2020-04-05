package com.polyglotandroid.core.collections

import com.polyglotandroid.core.DictCore
import com.polyglotandroid.core.nodes.ConWord


class ConWordCollection (_dictCore: DictCore): DictionaryCollection<ConWord>() {
    private val core: DictCore? = null
    private val allConWords: Map<String, Int>? = null
    private val allLocalWords: Map<String, Int>? = null
    private val orderByLocal = false

    override fun testWordLegality(n: ConWord): ConWord {
        TODO("Not yet implemented")
    }

    companion object {
        val SPLIT_CHAR: Char = ','
    }
}