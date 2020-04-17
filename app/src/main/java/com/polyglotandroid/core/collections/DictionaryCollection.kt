package com.polyglotandroid.core.collections

import com.polyglotandroid.core.customControls.AlphaMap
import com.polyglotandroid.core.nodes.ConWord

abstract class DictionaryCollection<N> {
    abstract fun testWordLegality(n: N): ConWord
    abstract fun externalBalanceWordCounts(id: Int, _value: String, localWord: String)

    protected var alphaOrder: AlphaMap<String, Integer>
    protected val nodeMap: Map<Int, N>
}