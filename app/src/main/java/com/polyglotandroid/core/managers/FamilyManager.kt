package com.polyglotandroid.core.managers

import com.polyglotandroid.core.DictCore
import com.polyglotandroid.core.PGUtil
import com.polyglotandroid.core.WebInterface
import com.polyglotandroid.core.nodes.ConWord
import com.polyglotandroid.core.nodes.FamilyNode
import org.w3c.dom.Document
import org.w3c.dom.Element


class FamilyManager(
    /**
     * Returns dict core for use in nodes
     * @return dictionary core
     */
    var core: DictCore
) {
    private var famRoot: FamilyNode? = null
    private var buffer: FamilyNode? = null

    /**
     * Gets root family node
     * @return
     */
    val root: FamilyNode?
        get() {
            if (famRoot == null) {
                famRoot = FamilyNode(null, "Families", this)
            }
            return famRoot
        }

    /**
     * This deletes words from the word list that no longer exist in the lexicon
     * @param fam family entry to clean
     * @param wordList raw words from entry
     */
    fun removeDeadWords(fam: FamilyNode, wordList: List<ConWord?>?) {
        val wordIt: Iterator<ConWord> = ArrayList(wordList).iterator()
        while (wordIt.hasNext()) {
            val curWord = wordIt.next()
            if (!core.wordCollection.exists(curWord.id)) {
                fam.removeWord(curWord)
            }
        }
    }

    /**
     * Used when loading from save file and building families.
     * Either creates new root or adds child/sets buffer to that.
     */
    fun buildNewBuffer() {
        if (famRoot == null) {
            famRoot = FamilyNode(null, this)
            buffer = famRoot
        } else {
            val newBuffer = FamilyNode(buffer, this)
            buffer?.addNode(newBuffer)
            buffer = newBuffer
        }
    }

    /**
     * Gets buffer for building families from saved file
     * @return current FamilyNode buffer
     */
    fun getBuffer(): FamilyNode? {
        return buffer
    }

    /**
     * jumps to buffer parent, or does nothing if at root
     */
    fun bufferDone() {
        if (buffer?.parent == null) {
            return
        }
        buffer = buffer!!.parent
    }

    /**
     * returns Element containing all family data to be saved to XML
     * @param doc the document this is to be inserted into
     * @return an element containing all family data
     */
    fun writeToSaveXML(doc: Document): Element {
        return writeToSaveXML(doc, famRoot)
    }

    /**
     * this is the recursive function that completes the work of its overridden method
     * @param doc the document this is to be inserted into
     * @param curNode node to build element for
     * @return an element containing all family data
     */
    private fun writeToSaveXML(doc: Document, curNode: FamilyNode?): Element {
        val curElement: Element = doc.createElement(PGUtil.FAMILY_NODE_XID)
        if (curNode == null) {
            return curElement
        }

        // save name
        var property: Element = doc.createElement(PGUtil.FAMILY_NAME_XID)
        property.appendChild(doc.createTextNode(curNode.value))
        curElement.appendChild(property)

        // save notes
        property = doc.createElement(PGUtil.FAMILY_NOTES_XID)
        property.appendChild(doc.createTextNode(WebInterface.archiveHTML(curNode.notes)))
        curElement.appendChild(property)

        // save words
        val wordIt: Iterator<ConWord> = curNode.getWords()
        while (wordIt.hasNext()) {
            val curWord = wordIt.next()
            property = doc.createElement(PGUtil.FAMILY_WORD_XID)
            property.appendChild(doc.createTextNode(curWord.id.toString()))
            curElement.appendChild(property)
        }

        // save subnodes
        for (child in curNode.nodes) {
            curElement.appendChild(writeToSaveXML(doc, child))
        }
        return curElement
    }

}
