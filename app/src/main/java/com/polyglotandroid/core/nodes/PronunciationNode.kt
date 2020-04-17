package com.polyglotandroid.core.nodes

import com.polyglotandroid.core.PGUtil
import org.w3c.dom.Document
import org.w3c.dom.Element

/**
 * Records orthographic pronunciation values
 */
class PronunciationNode(_id: Int) : DictNode(_id) {
    var pronunciation = ""

    override fun equals(other: Any?): Boolean =
        if (other != null && other is PronunciationNode)
            pronunciation == other.pronunciation && value == other.value
        else false

    @Throws(ClassCastException::class)
    override fun setEqual(_node: DictNode) {
        if (_node !is PronunciationNode) {
            throw ClassCastException("Object not of type PronunciationNode")
        }
        val node = _node
        pronunciation = node.pronunciation
        value = node.value
        id = node.id
    }

    fun writeXML(doc: Document, rootElement: Element) {
        val wordNode: Element = doc.createElement(PGUtil.PRONUNCIATION_GUIDE_XID)
        var wordValue: Element = doc.createElement(PGUtil.PRONUNCIATION_GUIDE_BASE_XID)
        wordValue.appendChild(doc.createTextNode(value))
        wordNode.appendChild(wordValue)
        wordValue = doc.createElement(PGUtil.PRONUNCIATION_GUIDE_PHONOLOGY_XID)
        wordValue.appendChild(doc.createTextNode(pronunciation))
        wordNode.appendChild(wordValue)
        rootElement.appendChild(wordNode)
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + pronunciation.hashCode()
        return result
    }
}