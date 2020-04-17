package com.polyglotandroid.core.nodes

class PEntry<K, V>(override val key: K, override var value: V) :
    MutableMap.MutableEntry<K, V> {

    override fun setValue(newValue: V): V {
        val old = this.value
        this.value = newValue
        return old
    }

    fun equals(test: PEntry<*, *>): Boolean {
        return key == test.key && value == test.value
    }
}
