package com.polyglotandroid.core.nodes

import com.polyglotandroid.core.PGUtil
import org.w3c.dom.Document
import org.w3c.dom.Element
import java.util.function.Consumer

/**
 * This class represents both the header for declension templates, and the actual
 * body object for fully realized declension constructs (with full combined dimension IDs)
 */
class DeclensionNode (declensionId: Int): DictNode(declensionId) {
    var notes: String = ""
    var combinedDimId = ""
    get() = if (dimensionless) id.toString() else field
    var dimensionless = false
    set(value) {
        if (field != value) {
            field = value
            dimensions.clear()
        }
        if (value) {
            val dim: DeclensionDimension = DeclensionDimension()
            dim.value = "SINGLETON-DIMENSION"
            addDimension(dim)
        }
    }
    var highestDimension = 1
    val dimensions: HashMap<Int, DeclensionDimension> = HashMap()
    var buffer = DeclensionDimension(-1)

    @Throws(Exception::class)
    fun insertBuffer() {
        if (buffer.id == -1) {
            throw Exception("Dimension with ID -1 cannot be inserted.")
        }
        this.addDimension(buffer)
        buffer = DeclensionDimension(-1)
    }

    fun clearBuffer() {
        buffer = DeclensionDimension(-1)
    }

    fun addDimension(dim: DeclensionDimension): Any {
        val addDim: DeclensionDimension
        val ret: Int = if (dim.id.equals(-1)) {
            highestDimension + 1
        } else {
            dim.id
        }

        if (highestDimension < ret) {
            highestDimension = ret;
        }

        addDim = DeclensionDimension(ret);
        addDim.value = dim.value;

        dimensions.put(ret, addDim);

        return ret;
    }

    /**
     * Deletes the dimension from this declension, given its [_id]
     */
    fun deleteDimension(_id: Int?) {
        dimensions.remove(_id)
    }

    /**
     * Returns a declension dimension by its [_id] if it exists
     */
    fun getDeclensionDimensionById(_id: Int): DeclensionDimension? {
        var ret: DeclensionDimension? = null
        if (dimensions.containsKey(_id)) {
            ret = dimensions[_id]
        }
        return ret
    }

    fun writeXMLTemplate(doc: Document, rootElement: Element, relatedId: Int) {
        val wordNode = doc.createElement(PGUtil.DECLENSION_XID)
        rootElement.appendChild(wordNode)

        var nodeValue = doc.createElement(PGUtil.DECLENSION_ID_XID)
        nodeValue.appendChild(doc.createTextNode(id.toString()))
        wordNode.appendChild(nodeValue)

        nodeValue = doc.createElement(PGUtil.DECLENSION_TEXT_XID)
        nodeValue.appendChild(doc.createTextNode(this.value))
        wordNode.appendChild(nodeValue)

        nodeValue = doc.createElement(PGUtil.DECLENSION_NOTES_XID)
        nodeValue.appendChild(doc.createTextNode(WebInterface.archiveHTML(this.notes)))
        wordNode.appendChild(nodeValue)

        nodeValue = doc.createElement(PGUtil.DECLENSION_TEXT_XID)
        nodeValue.appendChild(doc.createTextNode(this.value))
        wordNode.appendChild(nodeValue)

        nodeValue = doc.createElement(PGUtil.DECLENSION_NOTES_XID)
        nodeValue.appendChild(doc.createTextNode(WebInterface.archiveHTML(this.notes)))
        wordNode.appendChild(nodeValue);

        val dimIt: Iterator<DeclensionDimension> =
            this.dimensions.iterator() as Iterator<DeclensionDimension>
        while (dimIt.hasNext()) {
            dimIt.next().writeXML(doc, wordNode)
        }
    }

    fun writeXMLWordDeclension(doc: Document, rootElement: Element, relatedId: Int) {
        val wordNode = doc.createElement(PGUtil.DECLENSION_XID)
        rootElement.appendChild(wordNode)

        var wordValue = doc.createElement(PGUtil.DECLENSION_ID_XID)
        wordValue.appendChild(doc.createTextNode(id.toString()))
        wordNode.appendChild(wordValue)

        wordValue = doc.createElement(PGUtil.DECLENSION_TEXT_XID)
        wordValue.appendChild(doc.createTextNode(value))
        wordNode.appendChild(wordValue)

        wordValue = doc.createElement(PGUtil.DECLENSION_NOTES_XID)
        wordValue.appendChild(doc.createTextNode(notes))
        wordNode.appendChild(wordValue)

        wordValue = doc.createElement(PGUtil.DECLENSION_RELATED_ID_XID)
        wordValue.appendChild(doc.createTextNode(relatedId.toString()))
        wordNode.appendChild(wordValue)

        wordValue = doc.createElement(PGUtil.DECLENSION_COMBINED_DIMENSION_XID)
        wordValue.appendChild(doc.createTextNode(this.combinedDimId))
        wordNode.appendChild(wordValue)

        wordValue = doc.createElement(PGUtil.DECLENSION_IS_TEMPLATE_XID)
        wordValue.appendChild(doc.createTextNode("0"))
        wordNode.appendChild(wordValue)
    }

    override fun equals(other: Any?): Boolean =
        if (this == other)
            true
        else if (other != null && other is DeclensionNode)
            value == other.value &&
                    notes == other.notes &&
                    combinedDimId == other.combinedDimId &&
                    dimensionless == other.dimensionless &&
                    highestDimension == other.highestDimension &&
                    dimensions == other.dimensions
        else false

    override fun setEqual(_node: DictNode) {
        if (_node !is DeclensionNode) {
            throw ClassCastException("Object not of type DeclensionNode")
        }

        val node = _node

        this.notes = node.notes
        value = node.value
        combinedDimId = node.combinedDimId
        this.dimensionless = node.dimensionless

        for (entry in node.dimensions.entries) run {
            var cp_dim: DeclensionDimension = DeclensionDimension(entry.key)
            cp_dim = entry.value
            dimensions.put(entry.key, cp_dim)
        }

    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + notes.hashCode()
        result = 31 * result + dimensionless.hashCode()
        result = 31 * result + highestDimension
        result = 31 * result + dimensions.hashCode()
        result = 31 * result + buffer.hashCode()
        return result
    }

}