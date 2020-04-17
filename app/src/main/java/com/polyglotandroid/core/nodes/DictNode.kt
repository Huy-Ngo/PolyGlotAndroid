package com.polyglotandroid.core.nodes

import com.polyglotandroid.core.customControls.AlphaMap
import java.lang.ClassCastException
import kotlin.math.min

abstract class DictNode (_id: Int?) : Comparable<DictNode> {
    open var value: String = ""
        set(conWord: String) {
            field = conWord.trim()
        }
    var id: Int = 0
    var alphaOrder: AlphaMap<String, Int> = AlphaMap()

    abstract override fun equals(other: Any?): Boolean
    override fun hashCode(): Int {
        var result = value.hashCode()
        result = 31 * result + id
        result = 31 * result + alphaOrder.hashCode()
        return result
    }

    init {
        if (_id is Int)
            this.id = _id
    }

    @Throws(ClassCastException::class)
    abstract fun setEqual(_node: DictNode)
    override fun compareTo(other: DictNode): Int {
        val BEFORE = -1
        val EQUAL = 0
        val AFTER = 1
        val comp: String = other.value
        val self: String = this.value
        val ret: Int
        if (this.alphaOrder.isEmpty) {
            ret = self.compareTo(comp)
        } else {
            if (comp.equals(self) || comp.isEmpty() && self.isEmpty())
                ret = EQUAL
            else if (comp.isEmpty())
                ret = AFTER
            else if (self.isEmpty())
                ret = BEFORE
            else {
                val longest: Int = alphaOrder.longestEntry
                val selfLen: Int = self.length
                val compLen: Int = comp.length
                var selfAlpha: Int = -1
                var compAlpha: Int = -1
                var preLen: Int = 0
                for (i in min(selfLen, longest) downTo 0) {
                    val selfPrefix: String = self.substring(0, i)
                    if (alphaOrder.containsKey(selfPrefix)) {
                        selfAlpha = alphaOrder.get(selfPrefix)!!
                        break
                    }
                }
                for (i in min(compLen, longest) downTo 0) {
                    val compPrefix: String = comp.substring(0, i)
                    if (alphaOrder.containsKey(compPrefix)) {
                        compAlpha = alphaOrder.get(compPrefix)!!
                        preLen = compPrefix.length
                        break
                    }
                }

                if (selfAlpha == -1 && compAlpha == -1)
                    ret = EQUAL
                else if (selfAlpha == -1)
                    ret = BEFORE
                else if (compAlpha > selfAlpha)
                    ret = BEFORE
                else if (compAlpha < selfAlpha)
                    ret = AFTER
                else {
                    val compChild = ConWord(null)
                    val selfChild = ConWord(null)
                    compChild.alphaOrder = alphaOrder
                    selfChild.alphaOrder = alphaOrder
                    compChild.value = other.value.substring(preLen)
                    selfChild.value = this.value.substring(preLen)
                    ret = selfChild.compareTo(compChild)
                }
            }
        }
        return ret
    }

    override fun toString(): String = if (this.value.isEmpty()) " " else value
}