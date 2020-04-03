package com.polyglotandroid.core.customControls

/**
 * Provides alphabetical ordering map-compatible with multi-unicode character,
 * constructed lettering.
 */
/
class AlphaMap<K, V> {
    private var longestEntry: Int = 0
    private val delegate: HashMap<K, V> = hashMapOf()
    val isEmpty: Boolean
        get() = delegate.isEmpty()

    fun put(key: K, orderValue: V): V? {
        val sKey: String = key as String
        val keyLen: Int = sKey.length
        if (keyLen > longestEntry)
            longestEntry = keyLen
        return delegate.put(key, orderValue)
    }
    // TODO: containsKey
    // TODO: get(K key)
    // TODO: clear()
    // TODO: equals()
    // TODO: hashCode()
}