package com.polyglotandroid.core.nodes

import com.polyglotandroid.core.PGUtil
import org.w3c.dom.Document
import org.w3c.dom.Element

/**
 * A single dimensional value of a conjugation element, for example, tense
 */
class DeclensionDimension(id: Int = -1): DictNode(id) {
    fun writeXML(doc: Document, rootElement: Element) {
        val wordValue: Element = doc.createElement(PGUtil.DIMENSION_NODE_XID)

        var dimensionNode: Element = doc.createElement(PGUtil.DIMENSION_ID_XID)
        dimensionNode.appendChild(doc.createTextNode(this.id.toString()))
        wordValue.appendChild(dimensionNode)

        dimensionNode = doc.createElement(PGUtil.DIMENSION_NAME_XID)
        dimensionNode.appendChild(doc.createTextNode(this.value))
        wordValue.appendChild(dimensionNode)

        rootElement.appendChild(wordValue)
    }

    override fun equals(other: Any?): Boolean =
        if (this === other)
            true
        else if (other != null && other is DeclensionDimension)
            this.value == (other as DictNode).value
        else
            false

    override fun setEqual(_node: DictNode) {
        this.value = (_node as DeclensionDimension).value
    }
}