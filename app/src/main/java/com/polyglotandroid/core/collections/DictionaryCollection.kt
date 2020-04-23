package com.polyglotandroid.core.collections

import com.polyglotandroid.core.customControls.AlphaMap
import com.polyglotandroid.core.nodes.DictNode
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


abstract class DictionaryCollection<N> {
    protected var alphaOrder: AlphaMap<String, Int>? = null
    protected val nodeMap: MutableMap<Int?, N?> = HashMap()
    var buffer: N? = null
        protected set
    private var highestNodeId = 1

    /**
     * Clears value of collection's current buffer
     *
     */
    abstract fun clear()

    /**
     * Returns an error type node with not-found information of an appropriate type
     * @return nout-found node
     */
    abstract fun notFoundNode(): Any?

    @Throws(Exception::class)
    fun addNode(_addType: DictNode?): Int {
        clear()
        (buffer as DictNode?)!!.setEqual(_addType!!)
        return insert(buffer)
    }

    /**
     * @param _id ID of node to replace
     * @param _modNode Node to replace prior word with
     * @throws Exception Throws exception when ID matches no node in collection
     */
    @Throws(Exception::class)
    fun modifyNode(_id: Int, _modNode: N) {
        if (!nodeMap.containsKey(_id)) {
            throw Exception(
                "No node with id: " + _id.toString()
                        + "; cannot modify value."
            )
        }
        if (_id < 1) {
            throw Exception("Id can never be less than 1.")
        }
        val myNode = _modNode as DictNode
        myNode.id = _id
        myNode.alphaOrder = alphaOrder
        nodeMap.remove(_id)
        nodeMap[myNode.id] = _modNode
    }

    /**
     * Tests whether object in collection exists by object's ID
     * @param objectId id of object to test for
     * @return true if exists, false otherwise
     */
    fun exists(objectId: Int?): Boolean {
        return nodeMap.containsKey(objectId)
    }

    /**
     * Returns node by ID if exists, "NOT FOUND" node otherwise
     * @param _id
     * @return
     */
    open fun getNodeById(_id: Int?): Any? {
        return if (!nodeMap.containsKey(_id)) {
            notFoundNode()
        } else {
            nodeMap[_id]
        }
    }

    /**
     * @param _id ID to delete
     * @throws Exception if no ID exists as listed
     */
    @Throws(Exception::class)
    open fun deleteNodeById(_id: Int) {
        if (!nodeMap.containsKey(_id)) {
            throw Exception(
                "Word with ID: " + _id.toString()
                        + " not found."
            )
        }
        nodeMap.remove(_id)
    }

    fun setAlphaOrder(_alphaOrder: AlphaMap<String, Int>?) {
        alphaOrder = _alphaOrder
    }

    /**
     * Simply inserts buffer as it currently exists
     * @return ID of inserted buffer
     * @throws Exception
     */
    @Throws(Exception::class)
    protected open fun insert(): Int? {
        return insert(buffer)
    }

    /**
     * Inserts buffer node, applying next logical ID to node
     * @param _buffer buffer to insert
     * @return ID of inserted buffer
     * @throws Exception if unable to insert node to nodemap
     */
    @Throws(Exception::class)
    protected fun insert(_buffer: N?): Int {
        highestNodeId++
        return this.insert(highestNodeId, _buffer)
    }

    /**
     * Inserts given buffer node to nodemap
     * @param _id ID to apply to buffer
     * @param _buffer buffer to be inserted
     * @return ID of inserted buffer
     * @throws Exception if unable to insert
     */
    @Throws(Exception::class)
    protected fun insert(_id: Int?, _buffer: N?): Int {
        val myBuffer = _buffer as DictNode?
        myBuffer!!.id = _id!!
        myBuffer.alphaOrder = this.alphaOrder!!
        if (nodeMap.containsKey(_id)) {
            throw Exception("Duplicate ID " + _id.toString() + " for collection object: " + myBuffer.value)
        }
        if (_id < 1) {
            throw Exception("Collection node ID may never be zero or less.")
        }
        nodeMap[_id] = _buffer
        highestNodeId = if (_id > highestNodeId) _id else highestNodeId
        return _id
    }

    /**
     * Returns randomly selected nodes from the collection
     * @param numRandom number of nodes to select
     * @return Either the number of nodes requested, or the total number in the collection (if not enough)
     */
    fun getRandomNodes(numRandom: Int): List<N?> {
        return getRandomNodes(numRandom, 0)
    }

    /**
     * Returns randomly selected nodes from the collection, excluding a selected value
     * @param numRandom number of nodes to select
     * @param exclude ID of element to exclude
     * @return Either the number of nodes requested, or the total number in the collection (if not enough)
     */
    fun getRandomNodes(numRandom: Int, exclude: Int?): List<N?> {
        var numRandom = numRandom
        val ret: MutableList<N?> = ArrayList()
        val allValues: MutableList<N?> = ArrayList(nodeMap.values)
        if (nodeMap.containsKey(exclude)) {
            allValues.remove(nodeMap[exclude])
        }

        // randomize order...
        allValues.shuffle(Random(System.nanoTime()))

        // can't return more than exist in the collection
        numRandom = if (numRandom > allValues.size) allValues.size else numRandom
        // select from list to return
        for (i in 0 until numRandom) {
            ret.add(allValues[i])
        }
        return ret
    }
}