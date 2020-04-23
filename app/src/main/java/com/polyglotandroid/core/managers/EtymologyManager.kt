package com.polyglotandroid.core.managers

import com.polyglotandroid.core.DictCore
import com.polyglotandroid.core.PGUtil
import com.polyglotandroid.core.collections.ConWordCollection
import com.polyglotandroid.core.nodes.ConWord
import com.polyglotandroid.core.nodes.EtymologyExternalParent
import org.w3c.dom.Document
import org.w3c.dom.Element
import java.util.function.Consumer
import java.util.function.Function


class EtymologyManager(private val core: DictCore) {
    private val parentToChild: MutableMap<Int, MutableList<Int>> =
        HashMap()
    private val childToParent: MutableMap<Int, MutableList<Int>> =
        HashMap()
    private val extParentToChild: MutableMap<String, MutableList<Int>> =
        HashMap()
    private val childToExtParent: MutableMap<Int, MutableMap<String, EtymologyExternalParent>> =
        HashMap()
    private val allExtParents: MutableMap<String, EtymologyExternalParent> =
        HashMap()
    private var bufferParent = 0
    private var bufferChild = 0
    private var bufferExtParent: EtymologyExternalParent = EtymologyExternalParent()

    /**
     * Adds a parent->child relationship to two words if the relationship does
     * not already exist.
     * @param parent the parent
     * @param child the child
     * @throws IllegalLoopException if relationship creates looping dependancy
     */
    @Throws(IllegalLoopException::class)
    fun addRelation(parent: Int, child: Int) {
        val collection: ConWordCollection = core.wordCollection
        if (createsLoop(parent, child)) {
            throw IllegalLoopException(
                "Parent/Child relation creates illegal loop."
                        + " A word may never have itself in its own etymological lineage."
            )
        }

        // fail silently if either doesn't exist        
        if (!collection.exists(parent) || !collection.exists(child)) {
            return
        }
        if (!parentToChild.containsKey(parent)) {
            val newList: MutableList<Int> = ArrayList()
            newList.add(child)
            parentToChild[parent] = newList
        } else {
            val myList = parentToChild[parent]!!
            if (!myList.contains(child)) {
                myList.add(child)
            }
        }
        if (!childToParent.containsKey(child)) {
            val newList: MutableList<Int> = ArrayList()
            newList.add(parent)
            childToParent[child] = newList
        } else {
            val myList = childToParent[child]!!
            if (!myList.contains(parent)) {
                myList.add(parent)
            }
        }
    }// TODO: add this back again when there's a good way to display multiple fonts in a single combobox

    /**
     * Collects and returns full list of all extant parents (both internal and
     * external)
     * @return
     */
    val allRoots: List<Any>
        get() {
            val ret: MutableList<Any> = ArrayList()
            val parents: MutableList<ConWord> = ArrayList()
            for (id in parentToChild.keys) {
                val curParent: ConWord = core.wordCollection.getNodeById(id)
                parents.add(curParent)
            }
            ret.addAll(parents)
            // TODO: add this back again when there's a good way to display multiple fonts in a single combobox
            ret.addAll(extParentList)
            return ret
        }

    /**
     * Returns a list of children that a word has
     * @param wordId ID of word to retrieve children of
     * @return list of integer IDs of child words (empty list if none)
     */
    fun getChildren(wordId: Int): List<Int> {
        val ret: List<Int>
        ret = if (parentToChild.containsKey(wordId)) {
            parentToChild[wordId]!!
        } else {
            ArrayList()
        }
        return ret
    }

    /**
     * Gets all external parents of a child by child id
     * @param childId id of child to get parents of
     * @return all external parents of child (empty if none)
     */
    fun getWordExternalParents(childId: Int): List<EtymologyExternalParent> {
        return if (childToExtParent.containsKey(childId)) ArrayList(childToExtParent[childId]!!.values) else ArrayList()
    }

    /**
     * Gets all parent ids of child word by child id
     * @param childId id of child to query for parents
     * @return list of parent ids (empty if none)
     */
    fun getWordParentsIds(childId: Int): List<Int> {
        return (if (childToParent.containsKey(childId)) childToParent[childId] else ArrayList())!!
    }

    /**
     * Sets relation of external parent to child word
     * NOTE: USE UNIQUE ID OF PARENT RATHER THAN SIMPLE VALUE
     * @param parent Full unique ID of external parent
     * @param child child's ID
     */
    fun addExternalRelation(parent: EtymologyExternalParent, child: Int) {
        // return immediately if child does not exist
        if (core.wordCollection.exists(child)) {
            if (!extParentToChild.containsKey(parent.uniqueId)) {
                val myList: MutableList<Int> = ArrayList()
                myList.add(child)
                extParentToChild[parent.uniqueId] = myList
                allExtParents[parent.uniqueId] = parent
            } else {
                val myList =
                    extParentToChild[parent.uniqueId]!!
                if (!myList.contains(child)) {
                    myList.add(child)
                }
            }
            if (!childToExtParent.containsKey(child)) {
                val myMap: MutableMap<String, EtymologyExternalParent> =
                    HashMap()
                myMap[parent.uniqueId] = parent
                childToExtParent[child] = myMap
            } else {
                val myMap: MutableMap<String, EtymologyExternalParent> =
                    childToExtParent[child]!!
                if (!myMap.containsKey(parent.uniqueId)) {
                    myMap[parent.uniqueId] = parent
                }
            }
        }
    }

    fun delExternalRelation(parent: EtymologyExternalParent, child: Int) {
        // only run if child exists
        if (core.wordCollection.exists(child)) {
            if (extParentToChild.containsKey(parent.uniqueId)) {
                val myList =
                    extParentToChild[parent.uniqueId]!!
                if (myList.contains(child)) {
                    myList.remove(child)
                }
                if (myList.isEmpty()) {
                    allExtParents.remove(getExtListParentValue(parent))
                }
            }
            if (childToExtParent.containsKey(child)) {
                val myMap: MutableMap<String, EtymologyExternalParent> =
                    childToExtParent[child]!!
                if (myMap.containsKey(parent.uniqueId)) {
                    myMap.remove(parent.uniqueId)
                }
            }
        }
    }

    /**
     * Add external parent to total list if it does not already exist.
     * No corrolary to remove, as this is regenerated at every load. Old
     * values will fall away at this point.
     * @param parent Parent to add to list.
     */
    private fun addExtParentToList(parent: EtymologyExternalParent) {
        val parentId: String = parent.uniqueId
        if (!allExtParents.containsKey(parentId)) {
            allExtParents[parent.uniqueId] = parent
        }
    }

    /**
     * Creates external parent display value (used as ID for list of all external
     * parents for use in filtering
     * @param parent
     * @return
     */
    private fun getExtListParentValue(parent: EtymologyExternalParent): String {
        // TODO: REVISIT THIS: NEED TO USE ACTUAL OBJECT IN LIST TO ALLOW FOR FILTERING
        return parent.externalWord.toString() + " (" + parent.externalLanguage + ")"
    }

    /**
     * Gets list of every external parent referenced in entire language
     * @return alphabetical list by word + (language)
     */
    private val extParentList: List<Any>
        private get() {
            val ret: List<EtymologyExternalParent> = ArrayList(allExtParents.values)
            return ret.sorted()
        }

    /**
     * Deletes relationship between parent and child if one exists
     * @param parentId
     * @param childId
     */
    fun delRelation(parentId: Int, childId: Int) {
        if (parentToChild.containsKey(parentId)) {
            val myList = parentToChild[parentId]!!
            if (myList.contains(childId)) {
                myList.remove(childId)
            }
        }
        if (childToParent.containsKey(childId)) {
            val myList = childToParent[childId]!!
            if (myList.contains(parentId)) {
                myList.remove(parentId)
            }
        }
    }

    /**
     * Writes all word information to XML document
     *
     * @param doc Document to write to
     * @param rootElement root element of document
     */
    fun writeXML(doc: Document, rootElement: Element) {
        val wordCollection: ConWordCollection = core.wordCollection
        val collection: Element = doc.createElement(PGUtil.ETYMOLOGY_COLLECTION_XID)

        // we only need to record the relationship one way, the bidirection will be regenerated
        for ((key, value) in parentToChild) {
            // skip nonexistant words
            if (!wordCollection.exists(key)) {
                continue
            }
            val myNode: Element = doc.createElement(PGUtil.ETYMOLOGY_INT_RELATION_NODE_XID)
            myNode.appendChild(doc.createTextNode(key.toString()))
            for (curChild in value) {
                if (!wordCollection.exists(curChild)) {
                    continue
                }
                val child: Element = doc.createElement(PGUtil.ETYMOLOGY_INT_CHILD_XID)
                child.appendChild(doc.createTextNode(curChild.toString()))
                myNode.appendChild(child)
            }
            collection.appendChild(myNode)
        }

        // adds a node for each word with at least one external parent
        childToExtParent.entries.stream()
            .map(
                Function<Map.Entry<Int, Map<String, EtymologyExternalParent>>, Any> { curEntry: Map.Entry<Int, Map<String, EtymologyExternalParent>> ->
                    val childContainer: Element = doc.createElement(PGUtil.EtyChildExternalsXID)
                    childContainer.appendChild(doc.createTextNode(curEntry.key.toString()))
                    // creates a node for each external parent within a word
                    curEntry.value.values.forEach(Consumer<EtymologyExternalParent> { parent: EtymologyExternalParent ->
                        val extParentNode: Element =
                            doc.createElement(PGUtil.EtyExternalWordNodeXID)
                        // record external word value
                        var curElement: Element = doc.createElement(PGUtil.EtyExternalWordValueXID)
                        curElement.appendChild(doc.createTextNode(parent.externalWord))
                        extParentNode.appendChild(curElement)
                        // record external word origin
                        curElement = doc.createElement(PGUtil.EtyExternalWordOriginXID)
                        curElement.appendChild(doc.createTextNode(parent.externalLanguage))
                        extParentNode.appendChild(curElement)
                        // record external word definition
                        curElement = doc.createElement(PGUtil.EtyExternalWordDefinitionXID)
                        curElement.appendChild(doc.createTextNode(parent.getDefinition()))
                        extParentNode.appendChild(curElement)
                        childContainer.appendChild(extParentNode)
                    })
                    childContainer
                }
            )
            .forEachOrdered { childContainer: Any? ->
                collection.appendChild(childContainer)
            }
        rootElement.appendChild(collection)
    }

    /**
     * Tests whether adding a parent-child relationship would create an illegal
     * looping scenario
     * @param parentId parent word ID to check
     * @param childId child word ID to check
     * @return true if illegal due to loop, false otherwise
     */
    private fun createsLoop(parentId: Int, childId: Int): Boolean {
        return (parentId == childId || createsLoopParent(parentId, childId)
                || createsLoopChild(parentId, childId))
    }

    /**
     * Tests whether a child->parent addition creates an illegal loop.
     * Recursive.
     * @param parentId current value to check against (begin with self)
     * @param childId bottommost child ID being checked
     * @return true if illegal due to loop, false otherwise
     */
    private fun createsLoopParent(curWordId: Int, childId: Int): Boolean {
        var ret = false
        if (childToParent.containsKey(curWordId)) {
            for (selectedParent in childToParent[curWordId]!!) {
                ret = selectedParent == childId || createsLoopParent(selectedParent, childId)

                // break on single loop occurance and return
                if (ret) {
                    break
                }
            }
        }
        return ret
    }

    /**
     * Tests whether a parent->child addition creates an illegal loop.
     * Recursive.
     * @param parentId topmost parent ID to check against
     * @param curWordId ID of current word being checked against
     * @return true if illegal due to loop, false otherwise
     */
    private fun createsLoopChild(parentId: Int, curWordId: Int): Boolean {
        var ret = false

        // test base parent ID against all children of current word
        // and of all subsequent children down the chain
        for (childId in getChildren(curWordId)) {
            ret = parentId == childId && createsLoopChild(parentId, childId)

            // break on single loop occurance and return
            if (ret) {
                break
            }
        }
        return ret
    }

    fun setBufferParent(_bufferParent: Int) {
        bufferParent = _bufferParent
    }

    fun setBufferChild(_bufferChild: Int) {
        bufferChild = _bufferChild
    }

    fun getBufferExtParent(): EtymologyExternalParent {
        return bufferExtParent
    }

    fun insertBufferExtParent() {
        addExternalRelation(bufferExtParent, bufferChild)
        bufferExtParent = EtymologyExternalParent()
    }

    /**
     * Tests whether child word has parent word in its etymology
     * @param childId id of child word
     * @param parId id of parent word
     * @return true if in etymology
     */
    fun childHasParent(childId: Int, parId: Int): Boolean {
        var ret = false
        if (childToParent.containsKey(childId)) {
            val myList: List<Int> = childToParent[childId]!!
            ret = myList.contains(parId)
            if (!ret) {
                for (newChild in myList) {
                    ret = childHasParent(newChild, parId)
                    if (ret) {
                        break
                    }
                }
            }
        }
        return ret
    }

    /**
     * Tests whether child word has external parent word in its etymology
     * @param childId id of child word
     * @param parId unique external id of parent word
     * @return true if in etymology
     */
    fun childHasExtParent(childId: Int, parId: String): Boolean {
        return (childToExtParent.containsKey(childId)
                && childToExtParent[childId]!!.containsKey(parId))
    }

    /**
     * Inserts buffer values and clears buffer
     */
    fun insert() {
        try {
            addRelation(bufferParent, bufferChild)
            // Do NOT set these to 0. This relies on the parent buffer persisting.
        } catch (e: IllegalLoopException) {
            // do nothing. These will have been eliminated at the time of archiving.
        }
    }

    inner class IllegalLoopException(message: String?) :
        Exception(message)

    /**
     * Tests whether word has any etymological relevance
     * @param word
     * @return true if parents or children to this word
     */
    fun hasEtymology(word: ConWord): Boolean =
        (childToParent.containsKey(word.id) // if word has parents
                || parentToChild.containsKey(word.id) // if word has children
                || childToExtParent.containsKey(word.id) && childToExtParent[word.id]?.isNotEmpty() ?: false)

}
