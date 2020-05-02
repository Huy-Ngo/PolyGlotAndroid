package com.polyglotandroid.core.collections

import com.polyglotandroid.core.DictCore
import com.polyglotandroid.core.PGUtil
import com.polyglotandroid.core.nodes.ConWord
import com.polyglotandroid.core.nodes.DictNode
import com.polyglotandroid.core.nodes.PartOfSpeechNode
import org.w3c.dom.Document
import org.w3c.dom.Element
import kotlin.collections.ArrayList


class PartOfSpeechCollection(_core: DictCore) : DictionaryCollection<PartOfSpeechNode?>() {
    val core: DictCore
    val bufferType: PartOfSpeechNode
        get() = bufferNode as PartOfSpeechNode
    var bufferNode: PartOfSpeechNode
    var gloss: String

    @Throws(Exception::class)
    fun deleteNodeById(_id: Int?) {
        super.deleteNodeById(_id!!)
    }

    @Throws(Exception::class)
    override fun addNode(_addType: DictNode?): Int {
        bufferNode = PartOfSpeechNode()
        return super.addNode(_addType)
    }

    /**
     * Tests whether type based requirements met for word
     *
     * @param word word to check
     * @return empty if no problems, string with problem description otherwise
     */
    fun typeRequirementsMet(word: ConWord): String {
        var ret = ""
        val type: PartOfSpeechNode? = getNodeById(word.getWordTypeId())

        // all requirements met if no type set at all.
        if (type != null) {
            val procVal: String
            procVal = try {
                word.pronunciation
            } catch (e: Exception) {
                // IOHandler.writeErrorLog(e);
                "<ERROR>"
            }
            if (type.isDefMandatory() && word.definition.length == 0) {
                ret = type.getValue().toString() + " requires a definition."
            } else if (type.isProcMandatory() && procVal.length == 0) {
                ret = type.getValue().toString() + " requires a pronunciation."
            }
        }
        return ret
    }

    /**
     * This is a method used for finding nodes by name. Only for use when loading
     * old PolyGlot files. DO NOT rely on names for uniqueness moving forward.
     * @param name name of part of speech to search for
     * @return matching part of speech. Throws error otherwise
     * @throws java.lang.Exception if not found
     */
    @Throws(Exception::class)
    fun findByName(name: String): PartOfSpeechNode {
        var ret: PartOfSpeechNode? = null
        for (n in nodeMap.values) {
            val curNode: PartOfSpeechNode = n as PartOfSpeechNode
            if (curNode.getValue().toLowerCase().equals(name.toLowerCase())) {
                ret = curNode
                break
            }
        }
        if (ret == null) {
            throw Exception("Unable to find part of speech: $name")
        }
        return ret
    }

    /**
     * Finds/returns type (if extant) by name
     *
     * @param _name
     * @return found type node, null otherwise
     */
    fun findTypeByName(_name: String): PartOfSpeechNode? {
        var ret: PartOfSpeechNode? = null
        if (_name.length != 0) {
            val it: Iterator<Map.Entry<Int, PartOfSpeechNode>> =
                nodeMap.entries.iterator()
            var curEntry: Map.Entry<Int, PartOfSpeechNode>
            while (it.hasNext()) {
                curEntry = it.next()
                if (curEntry.value.getValue().toLowerCase().equals(_name.toLowerCase())) {
                    ret = curEntry.value
                    break
                }
            }
        }
        return ret
    }

    /**
     * inserts current buffer word to conWord list based on id; blanks out
     * buffer
     *
     * @param _id
     * @return
     * @throws Exception
     */
    @Throws(Exception::class)
    fun insert(_id: Int): Int {
        val insWord = PartOfSpeechNode()
        insWord.setEqual(bufferNode)
        insWord.id = _id
        val ret: Int = super.insert(_id, bufferNode)
        bufferNode = PartOfSpeechNode()
        return ret
    }

    /**
     * inserts current buffer to conWord list and generates id; blanks out
     * buffer
     *
     * @return ID of newly created node
     * @throws Exception
     */
    @Throws(Exception::class)
    public override fun insert(): Int? {
        val ret: Int = super.insert(bufferNode)
        bufferNode = PartOfSpeechNode()
        return ret
    }

    override fun getNodeById(_id: Int?): PartOfSpeechNode? {
        return super.getNodeById(_id) as PartOfSpeechNode?
    }

    override fun clear() {
        bufferNode = PartOfSpeechNode()
    }

    /**
     * returns iterator of nodes with their IDs as the entry key (ordered)
     *
     * @return
     */
    val nodes: List<PartOfSpeechNode>
        get() {
            val retList: ArrayList<PartOfSpeechNode?> = ArrayList(nodeMap.values)
            retList.sort()
            return retList
        }

    fun nodeExists(findType: String?): Boolean {
        var ret = false
        val searchList: MutableIterator<MutableMap.MutableEntry<Int?, PartOfSpeechNode?>> =
            nodeMap.entries
                .iterator()
        while (searchList.hasNext()) {
            val curEntry: Map.Entry<Int, PartOfSpeechNode> = searchList.next()
            val curType: PartOfSpeechNode = curEntry.value
            if (curType.value == findType) {
                ret = true
                break
            }
        }
        return ret
    }

    fun nodeExists(id: Int): Boolean {
        return nodeMap.containsKey(id)
    }

    @Throws(Exception::class)
    fun findOrCreate(name: String?): PartOfSpeechNode? {
        val node = PartOfSpeechNode()
        node.value = name
        return findOrCreate(node)
    }

    @Throws(Exception::class)
    fun findOrCreate(node: PartOfSpeechNode): PartOfSpeechNode? {
        var ret: PartOfSpeechNode? = null
        for (n in nodeMap.values) {
            val compNode: PartOfSpeechNode = n as PartOfSpeechNode
            if (compNode.value == node.value
                && compNode.gloss == node.gloss
            ) {
                ret = compNode
                break
            }
        }
        if (ret == null) {
            ret = getNodeById(insert(node))
        }
        return ret
    }

    /**
     * Writes all type information to XML document
     * @param doc Document to write to
     * @param rootElement root element of document
     */
    fun writeXML(doc: Document, rootElement: Element) {
        val typeContainer: Element = doc.createElement(PGUtil.POS_COLLECTION_XID)
        for (curPos in nodes) {
            curPos.writeXML(
                doc,
                typeContainer
            )
        }
        rootElement.appendChild(typeContainer)
    }

    override fun notFoundNode(): Any? {
        val emptyNode = PartOfSpeechNode()
        emptyNode.value = "POS NOT FOUND"
        emptyNode.notes = "POS NOT FOUND"
        return emptyNode
    }

    init {
        bufferNode = PartOfSpeechNode()
        core = _core
    }
}
