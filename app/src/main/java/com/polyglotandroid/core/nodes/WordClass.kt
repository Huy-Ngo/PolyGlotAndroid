package com.polyglotandroid.core.nodes

import com.polyglotandroid.core.PGUtil
import org.w3c.dom.Document
import org.w3c.dom.Element
import java.util.function.Consumer

/**
 * Word properties cover things such as gender. They may apply to all parts of
 * speech, or only select parts of speech.
 * @author Draque Thompson
 */
class WordClass : DictNode() {
    private var parent: WordClassCollection? = null
    private val values: MutableMap<Int, WordClassValue> = HashMap()
    private val applyTypes: MutableList<Int> = ArrayList()
    var isFreeText = false
    private var topId = 0
    var buffer: WordClassValue = WordClassValue()

    /**
     * Returns true if existing [valId] is passed, false otherwise
     */
    fun isValid(valId: Int): Boolean {
        return values.containsKey(valId)
    }

    override fun equals(other: Any?): Boolean =
        if (other == null)
            false
        else if (other !is WordClass)
            false
        else
            this.parent == other.parent &&
                    this.values == other.values &&
                    this.value == other.value &&
                    this.isFreeText == other.isFreeText &&
                    this.applyTypes == other.applyTypes &&
                    this.topId == other.topId &&
                    this.buffer == other.buffer

    @Throws(ClassCastException::class)
    override fun setEqual(_node: DictNode) {
        if (_node !is WordClass) {
            throw ClassCastException("Object not of type WordPropValueNode")
        }
        value = _node.value
        for (node in _node.getValues()) {
            try {
                addValue(node.getValue(), node.getId())
            } catch (e: Exception) {
                throw ClassCastException(
                    "Problem setting class value: "
                            + e.localizedMessage
                )
            }
        }
    }

    /**
     * inserts current buffer node into list of word property values
     * @throws java.lang.Exception
     */
    @Throws(Exception::class)
    fun insert() {
        addValue(buffer.getValue(), buffer.getId())
        buffer = WordClassValue()
    }

    /**
     * Adds type id to list of types this property applies to
     * -1 means "apply to all"
     * @param _typeId ID of type
     */
    fun addApplyType(_typeId: Int) {
        if (!applyTypes.contains(_typeId)) {
            applyTypes.add(_typeId)
        }
    }

    /**
     * Removes type id to list of types this property applies to
     * @param _typeId ID of type
     */
    fun deleteApplyType(_typeId: Int) {
        if (applyTypes.contains(_typeId)) {
            applyTypes.remove(_typeId)
        }
    }

    /**
     * Tests whether a this property applies to a given type
     * @param _typeId ID of type
     * @return true if applies
     */
    fun appliesToType(_typeId: Int): Boolean {
        return applyTypes.contains(_typeId)
    }

    /**
     * Gets copy of list of apply types
     * @return list of int values (ids)
     */
    fun getApplyTypes(): List<Int> {
        return ArrayList(applyTypes)
    }

    /**
     * Gets iterator of values
     * @return iterator with all values of word property
     */
    fun getValues(): Collection<WordClassValue> {
        return values.values
    }

    /**
     * Deletes value
     * @param valueId id of value to delete
     * @throws Exception on id notexists
     */
    @Throws(Exception::class)
    fun deleteValue(valueId: Int) {
        if (!values.containsKey(valueId)) {
            throw Exception("Id: $valueId does not exist in WordProperty: $value.")
        }
        values.remove(valueId)
        if (parent != null) {
            parent.classValueDeleted(id, valueId)
        }
    }

    @Throws(Exception::class)
    fun getValueById(_id: Int): WordClassValue? {
        if (!values.containsKey(_id)) {
            throw Exception("No value with id: $_id in property: $value.")
        }
        return values[_id]
    }

    /**
     * Adds new value, assigning ID automatically.
     * @param name
     * @return value created
     * @throws java.lang.Exception if auto-assigned ID fails
     */
    @Throws(Exception::class)
    fun addValue(name: String): WordClassValue {
        val ret: WordClassValue = addValue(name, topId)
        topId++
        return ret
    }

    /**
     * Inserts value with ID (only use on file loading)
     * @param name
     * @param id
     * @return value created
     * @throws Exception if ID already exists
     */
    @Throws(Exception::class)
    fun addValue(name: String, id: Int): WordClassValue {
        if (values.containsKey(id)) {
            throw Exception("Cannot insert value: $name Id: $id into $value (already exists).")
        }
        val ret = WordClassValue()
        ret.setId(id)
        ret.setValue(name)
        values[id] = ret
        if (id >= topId) {
            topId = id + 1
        }
        return ret
    }

    fun setParent(_parent: WordClassCollection?) {
        parent = _parent
    }

    fun writeXML(doc: Document, rootElement: Element) {
        val classElement: Element = doc.createElement(PGUtil.CLASS_XID)

        // ID element
        var classValue: Element = doc.createElement(PGUtil.CLASS_ID_XID)
        classValue.appendChild(doc.createTextNode(id.toString()))
        classElement.appendChild(classValue)

        // Name element
        classValue = doc.createElement(PGUtil.CLASS_NAME_XID)
        classValue.appendChild(doc.createTextNode(value))
        classElement.appendChild(classValue)

        // Is Text Override
        classValue = doc.createElement(PGUtil.CLASS_IS_FREE_TEXT_XID)
        classValue.appendChild(doc.createTextNode(if (isFreeText) PGUtil.TRUE else PGUtil.FALSE))
        classElement.appendChild(classValue)

        // generates element with all type IDs of types this class applies to
        var applyTypesRec = ""
        for (typeId in getApplyTypes()) {
            if (applyTypesRec.isNotEmpty()) {
                applyTypesRec += ","
            }
            applyTypesRec += typeId.toString()
        }
        classValue = doc.createElement(PGUtil.CLASS_APPLY_TYPES_XID)
        classValue.appendChild(doc.createTextNode(applyTypesRec))
        classElement.appendChild(classValue)

        // element for collection of values of class
        classValue = doc.createElement(PGUtil.CLASS_VALUES_COLLECTION_XID)
        for (curValue in getValues()) {
            curValue.writeXML(doc, classValue)
        }
        classElement.appendChild(classValue)
        rootElement.appendChild(classElement)
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + (parent.hashCode() ?: 0)
        result = 31 * result + values.hashCode()
        result = 31 * result + applyTypes.hashCode()
        result = 31 * result + isFreeText.hashCode()
        result = 31 * result + topId
        result = 31 * result + buffer.hashCode()
        return result
    }

    init {
        // default to apply to all
        applyTypes.add(-1)
    }
}