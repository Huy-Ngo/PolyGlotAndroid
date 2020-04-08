package com.polyglotandroid.core.nodes

import com.polyglotandroid.core.PGUtil
import org.w3c.dom.Document
import org.w3c.dom.Element

/**
 * Container class for declension auto-transform transformation pairs
 */
class DeclensionGenerationTransform (regex: String = "", replaceText: String = "") {
    var regex: String? = null
    var replaceText: String? = null

    /**
     * Sets transform equal to an [other] transform
     */
    fun setEqual(other: DeclensionGenerationTransform?) {
        if (other != null) {
            regex = other.regex
            replaceText = other.replaceText
        }
    }

    override fun equals(other: Any?): Boolean {
        var ret = false
        if (other === this) {
            ret = true
        } else if (other is DeclensionGenerationTransform) {
            ret = regex == other.regex
            ret = ret && replaceText == other.replaceText
        }
        return ret
    }

    fun writeXML(doc: Document, rootElement: Element) {
        val transNode = doc.createElement(PGUtil.DECLENSION_GENERATION_TRANS_XID)
        rootElement.appendChild(transNode)

        var wordValue = doc.createElement(PGUtil.DECLENSION_GENERATION_TRANS_REGEX_XID)
        wordValue.appendChild(doc.createTextNode(regex))
        transNode.appendChild(wordValue)

        wordValue = doc.createElement(PGUtil.DECLENSION_GENERATION_TRANS_REPLACE_XID)
        wordValue.appendChild(doc.createTextNode(replaceText))
        transNode.appendChild(wordValue)

    }

    override fun hashCode(): Int {
        var result = regex?.hashCode() ?: 0
        result = 31 * result + (replaceText?.hashCode() ?: 0)
        return result
    }

}
