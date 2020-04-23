package com.polyglotandroid.core.nodes

import com.polyglotandroid.core.PGUtil
import org.w3c.dom.Document
import org.w3c.dom.Element

/**
 * This represents a single value within a word property
 */
class WordClassValue(_id: Int) : DictNode(_id) {
    override fun equals(other: Any?): Boolean =
        if (other == null || other !is WordClassValue)
            false
        else
            this.value == other.value &&
                    this.id == other.id

    @Throws(ClassCastException::class)
    override fun setEqual(_node: DictNode) {
        if (_node !is WordClassValue) {
            throw ClassCastException("Object not of type WordClassValue")
        }
        value = _node.value
        id = _node.id
    }

    fun writeXML(doc: Document, rootElement: Element) {
        val valueNode: Element = doc.createElement(PGUtil.CLASS_VALUES_NODE_XID)
        var valueElement: Element = doc.createElement(PGUtil.CLASS_VALUE_ID_XID)
        valueElement.appendChild(doc.createTextNode(id.toString()))
        valueNode.appendChild(valueElement)

        // value string
        valueElement = doc.createElement(PGUtil.CLASS_VALUE_NAME_XID)
        valueElement.appendChild(doc.createTextNode(value))
        valueNode.appendChild(valueElement)
        rootElement.appendChild(valueNode)
    }

}