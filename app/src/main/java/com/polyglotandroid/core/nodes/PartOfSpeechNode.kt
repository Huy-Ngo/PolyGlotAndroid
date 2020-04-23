package com.polyglotandroid.core.nodes

import com.polyglotandroid.core.PGUtil
import com.polyglotandroid.core.WebInterface
import org.w3c.dom.Document
import org.w3c.dom.Element

/**
 * This represents a part of speech
 */
class PartOfSpeechNode(_id: Int) : DictNode(_id) {
    var notes = ""
    var pattern = ""
    var gloss = ""
    var isPronunciationMandatory = false
    var isDefinitionMandatory = false

    override fun equals(other: Any?): Boolean {
        var ret = false
        if (other != null) {
            ret = other is PartOfSpeechNode
            if (ret) {
                ret = (other as PartOfSpeechNode).id == id
            }
        }
        return ret
    }

    @Throws(ClassCastException::class)
    override fun setEqual(_node: DictNode) {
        if (_node !is PartOfSpeechNode) {
            throw ClassCastException("Object not of type PartOfSpeechNode")
        }
        id = _node.id
        value = _node.value
        isDefinitionMandatory = _node.isDefinitionMandatory
        isPronunciationMandatory = _node.isPronunciationMandatory
        gloss = _node.gloss
    }

    fun writeXML(doc: Document, rootElement: Element) {
        val wordNode: Element = doc.createElement(PGUtil.POS_XID)
        var wordValue: Element = doc.createElement(PGUtil.POS_ID_XID)
        val wordId = id
        wordValue.appendChild(doc.createTextNode(wordId.toString()))
        wordNode.appendChild(wordValue)
        wordValue = doc.createElement(PGUtil.POS_NAME_XID)
        wordValue.appendChild(doc.createTextNode(value))
        wordNode.appendChild(wordValue)
        wordValue = doc.createElement(PGUtil.POS_NOTES_XID)
        wordValue.appendChild(doc.createTextNode(WebInterface.archiveHTML(notes)))
        wordNode.appendChild(wordValue)
        wordValue = doc.createElement(PGUtil.POS_DEFINITION_MANDATORY_XID)
        wordValue.appendChild(doc.createTextNode(if (isDefinitionMandatory) PGUtil.TRUE else PGUtil.FALSE))
        wordNode.appendChild(wordValue)
        wordValue = doc.createElement(PGUtil.POS_PRONUNCIATION_MANDATORY_XID)
        wordValue.appendChild(doc.createTextNode(if (isPronunciationMandatory) PGUtil.TRUE else PGUtil.FALSE))
        wordNode.appendChild(wordValue)
        wordValue = doc.createElement(PGUtil.POS_PATTERN_XID)
        wordValue.appendChild(doc.createTextNode(pattern))
        wordNode.appendChild(wordValue)
        wordValue = doc.createElement(PGUtil.POS_GLOSS_XID)
        wordValue.appendChild(doc.createTextNode(gloss))
        wordNode.appendChild(wordValue)
        rootElement.appendChild(wordNode)
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + notes.hashCode()
        result = 31 * result + pattern.hashCode()
        result = 31 * result + gloss.hashCode()
        result = 31 * result + isPronunciationMandatory.hashCode()
        result = 31 * result + isDefinitionMandatory.hashCode()
        return result
    }
}