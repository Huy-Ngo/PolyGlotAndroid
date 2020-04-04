package com.polyglotandroid.core.customControls

import java.util.*
import kotlin.collections.HashMap

/**
 * Provides alphabetical ordering map-compatible with multi-unicode character,
 * constructed lettering.
 */
class AlphaMap<K, V> {
    var longestEntry: Int = 0
    val delegate: HashMap<K, V> = hashMapOf()
    val isEmpty: Boolean
        get() = delegate.isEmpty()

    fun put(key: K, orderValue: V): V? {
        val sKey: String = key as String
        val keyLen: Int = sKey.length
        if (keyLen > longestEntry)
            longestEntry = keyLen
        return delegate.put(key, orderValue)
    }
    fun containsKey(key: K): Boolean = delegate.containsKey(key)
    fun get(key: K): V? = delegate[key]
    fun clear() = delegate.clear()
    override fun equals(comp: Any?): Boolean {
        var ret = false
        if (comp is AlphaMap<*, *>)
            ret = (comp.longestEntry == this.longestEntry && comp.delegate == this.delegate)
        return ret
    }

    override fun hashCode(): Int {
        var result = longestEntry
        result = 31 * result + delegate.hashCode()
        return result
    }
}