package com.polyglotandroid.core.nodes

import java.util.*
import kotlin.collections.ArrayList

class FamilyNode : DictNode {
    private val subNodes: MutableList<FamilyNode> = ArrayList()
    private val words: MutableList<ConWord> = ArrayList()

    /**
     * Gets node's parent
     * @return FamilyNode representing node's parent. null if root
     */
    val parent: FamilyNode?
    /**
     * gets notes
     * @return notes of node
     */
    /**
     * sets notes
     * @param _notes new notes
     */
    var notes = ""
    private val manager: FamilyManager

    /**
     * sets parent of node
     * @param _parent node's parent (null if root)
     * @param _manager a link to the parent manager
     */
    constructor(_parent: FamilyNode?, _manager: FamilyManager) : super() {
        parent = _parent
        manager = _manager
    }

    /**
     * sets parent and value of node
     * @param _parent parent of note (null if root)
     * @param _value node's string value
     * @param _manager A link to the parent manager
     */
    constructor(_parent: FamilyNode?, _value: String?, _manager: FamilyManager) : super() {
        parent = _parent
        value = _value
        manager = _manager
    }

    override var value: String?
        get() = super.value
        set(_value) {
            super.value = _value!!
        }

    override fun equals(other: Any?): Boolean =
        if (other == null || other !is FamilyNode)
            false
        else this.subNodes == other.subNodes && this.words == other.words &&
                this.parent == other.parent && this.notes == other.notes &&
                this.manager == other.manager

    /**
     * gets node's manager
     * @return Family Manager
     */
    fun getManager(): FamilyManager {
        return manager
    }

    /**
     * NOT IMPLEMENTED IN FAMILYNODE
     * @param _node NOTHING
     */
    @Throws(ClassCastException::class)
    override fun setEqual(_node: DictNode) {
        throw ClassCastException("setEqual should never be called on FamilyNode instances.")
    }

    /**
     * adds word to family. ignores dupes
     * @param _word the word to add
     */
    fun addWord(_word: ConWord) {
        if (!words.contains(_word)) {
            words.add(_word)
        }
    }

    /**
     * removes word from family
     * @param _word id of word to remove
     */
    fun removeWord(_word: ConWord) {
        words.remove(_word)
    }

    /**
     * gets all words in immediate family
     * @return iterator of all words in immediate family
     */
    fun getWords(): Iterator<ConWord> {
        val ret: MutableList<ConWord> = ArrayList()
        manager.removeDeadWords(this, words)
        val convert: Iterator<ConWord> = words.iterator()
        while (convert.hasNext()) {
            val curWord = convert.next()
            ret.add(curWord)
        }
        ret.sort()
        return ret.iterator()
    }

    /**
     * returns all words within family and subfamilies
     * @return sorted list of ConWords
     */
    val wordsIncludeSubs: List<ConWord>
        get() {
            val ret = wordsIncludeSubsInternal
            Collections.sort(ret)
            return ret
        }// only add current word to return value if not already present

    /**
     * internally facing, recursive method for getting all words in this and subfamilies
     * @return list of (non duped) words in this and all subnodes
     */
    private val wordsIncludeSubsInternal: List<ConWord>
        get() {
            manager.removeDeadWords(this, words)
            val ret: MutableList<ConWord> = ArrayList(words)
            val subIt: Iterator<FamilyNode> = subNodes.iterator()
            while (subIt.hasNext()) {
                val curNode = subIt.next()
                val wordIt = curNode.wordsIncludeSubsInternal
                    .iterator()
                while (wordIt.hasNext()) {
                    val curWord = wordIt.next()

                    // only add current word to return value if not already present
                    if (!ret.contains(curWord)) {
                        ret.add(curWord)
                    }
                }
            }
            return ret
        }

    /**
     * gets all subnodes
     * @return alphabetically sorted iterator of all subnodes
     */
    val nodes: List<FamilyNode>
        get() {
            subNodes.sort()
            return subNodes
        }

    fun addNode(_node: FamilyNode) {
        subNodes.add(_node)
    }

    /**
     * removes self from parent, does nothing of root
     * @return false if root
     */
    fun removeFromParent(): Boolean {
        if (parent == null) {
            return false
        }
        parent.removeChild(this)
        return true
    }

    /**
     * removes given child from this parent node
     * @param _child
     */
    fun removeChild(_child: FamilyNode) {
        subNodes.remove(_child)
    }
}
