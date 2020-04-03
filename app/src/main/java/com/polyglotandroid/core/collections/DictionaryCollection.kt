package com.polyglotandroid.core.collections

import com.polyglotandroid.core.customControls.AlphaMap

abstract class DictionaryCollection<N> {
    protected var alphaOrder: AlphaMap<String, Integer>
    protected val nodeMap: Map<Int, N>
}