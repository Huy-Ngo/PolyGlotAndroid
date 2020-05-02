package com.polyglotandroid.core.nodes

import com.polyglotandroid.core.PGUtil
import org.w3c.dom.Document
import org.w3c.dom.Element

class ToDoNode(var parent: ToDoNode?, private var value: String, _isDone: Boolean) {
    var isDone = false
    private val children: MutableList<ToDoNode> = ArrayList()
    fun allChildrenDone(): Boolean {
        var ret = true
        for (curNode in children) {
            ret = ret && curNode.isDone && curNode.allChildrenDone()
        }
        return ret
    }

    /**
     * Adds a new to-do child with [childValue] to this node
     */
    fun addChild(childValue: String): ToDoNode {
        val newChild = ToDoNode(this, childValue, false)
        children.add(newChild)
        return newChild
    }

    fun addChild(child: ToDoNode) {
        child.parent = this
        children.add(child)
    }

    /**
     * Deletes node
     */
    fun delete() {
        if (parent != null) {
            parent!!.deleteChild(this)
        }
    }

    /**
     * moves node up unless already at top
     */
    fun moveUp() {
        if (parent != null) {
            parent!!.moveChildUp(this)
        }
    }

    /**
     * Moves node down unless already at bottom
     */
    fun moveDown() {
        if (parent != null) {
            parent!!.moveChildDown(this)
        }
    }

    /**
     * Deletes [child] from list if it is present
     */
    protected fun deleteChild(child: ToDoNode) {
        if (children.contains(child)) {
            children.remove(child)
        }
    }

    /**
     * moves [child] up one unless child is at top
     */
    protected fun moveChildUp(child: ToDoNode) {
        if (children.contains(child)) {
            val index = children.indexOf(child)
            if (index > 0) {
                children.removeAt(index)
                children.add(index - 1, child)
            }
        }
    }

    fun setValue(_value: String) {
        value = _value
    }

    /**
     * Moves [child] down one unless it is at the bottom
     */
    protected fun moveChildDown(child: ToDoNode) {
        if (children.contains(child)) {
            val index = children.indexOf(child)
            if (index < children.size - 1) {
                children.removeAt(index)
                children.add(index + 1, child)
            }
        }
    }

    fun getChildren(): List<ToDoNode> {
        return children
    }

    override fun toString(): String {
        return value
    }

    fun hasChildren(): Boolean {
        return !children.isEmpty()
    }

    /**
     * Writes XML value of element to rootElement of given doc
     * @param doc
     * @param rootElement
     */
    fun writeXML(doc: Document, rootElement: Element) {
        val writeNode: Element = doc.createElement(PGUtil.TODO_NODE_XID)
        val nodeDone: Element = doc.createElement(PGUtil.TODO_NODE_DONE_XID)
        val nodeLabel: Element = doc.createElement(PGUtil.TODO_NODE_LABEL_XID)
        // TODO: Implement color
        //Element nodeColor = doc.createElement(PGUtil.ToDoNodeColorXID);
        nodeDone.appendChild(doc.createTextNode(if (isDone) PGUtil.TRUE else PGUtil.FALSE))
        writeNode.appendChild(nodeDone)
        nodeLabel.appendChild(doc.createTextNode(value))
        writeNode.appendChild(nodeLabel)
        for (child in children) {
            child.writeXML(
                doc,
                writeNode
            )
        }
        rootElement.appendChild(writeNode)
    }

    init {
        isDone = _isDone
    }
}
