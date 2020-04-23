package com.polyglotandroid.core.managers

import com.polyglotandroid.core.DictCore
import com.polyglotandroid.core.PGUtil
import com.polyglotandroid.core.nodes.PronunciationNode
import org.w3c.dom.Document
import org.w3c.dom.Element


class RomanizationManager(_core: DictCore?) : PronunciationManager(_core) {
    /**
     * @return the enabled
     */
    /**
     * @param enabled the enabled status to set
     */
    var isEnabled = false

    /**
     * Writes all romanization information to XML document
     * @param doc Document to write to
     * @param rootElement root element of document
     */
    fun writeXML(doc: Document, rootElement: Element) {
        val romGuide: List<PronunciationNode> = getPronunciations()
        val guideNode: Element = doc.createElement(PGUtil.ROMANIZATION_GUIDE_XID)
        rootElement.appendChild(guideNode)
        var enabledNode: Element = doc.createElement(PGUtil.ROMANIZATION_GUIDE_ENABLED_XID)
        enabledNode.appendChild(doc.createTextNode(if (isEnabled) PGUtil.TRUE else PGUtil.FALSE))
        guideNode.appendChild(enabledNode)
        enabledNode = doc.createElement(PGUtil.ROMANIZATION_GUIDE_RECURSIVE_XID)
        enabledNode.appendChild(doc.createTextNode(if (recurse) PGUtil.TRUE else PGUtil.FALSE))
        guideNode.appendChild(enabledNode)
        for (curNode in romGuide) {
            val romNode: Element = doc.createElement(PGUtil.ROMANIZATION_GUIDE_NODE_XID)
            guideNode.appendChild(romNode)
            val valueNode: Element = doc.createElement(PGUtil.ROMANIZATION_GUIDE_BASE_XID)
            valueNode.appendChild(doc.createTextNode(curNode.value))
            romNode.appendChild(valueNode)
            val pronunciationNode: Element =
                doc.createElement(PGUtil.ROMANIZATION_GUIDE_PHONOLOGY_XID)
            pronunciationNode.appendChild(doc.createTextNode(curNode.pronunciation))
            romNode.appendChild(pronunciationNode)
        }
    }

    protected val toolLabel: String
        protected get() = "Romanization Manager"
}
