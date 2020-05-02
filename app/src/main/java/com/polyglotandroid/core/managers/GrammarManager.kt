package com.polyglotandroid.core.managers

/**
 * Grammar manager for PolyGlot organizes and stores all grammar data
 */
class GrammarManager {
    private val chapters: MutableList<GrammarChapNode> = ArrayList()
    private val soundMap: MutableMap<Int, ByteArray>
    private var buffer: GrammarChapNode

    /**
     * Fetches buffer chapter node
     * @return buffer chapter node
     */
    fun getBuffer(): GrammarChapNode {
        return buffer
    }

    /**
     * Inserts current buffer node to chapter list and clears buffer
     */
    fun insert() {
        chapters.add(buffer)
        clear()
    }

    /**
     * clears chapter buffer
     */
    fun clear() {
        buffer = GrammarChapNode(this)
    }

    fun getChapters(): List<GrammarChapNode> {
        return chapters
    }

    /**
     * Adds new chapter to index
     * @param newChap new chapter to add
     */
    fun addChapter(newChap: GrammarChapNode) {
        chapters.add(newChap)
    }

    fun getSoundMap(): Map<Int, ByteArray> {
        return soundMap
    }

    /**
     * Adds new chapter at particular index position
     * @param newChap chapter to add
     * @param index location to add chapter at
     */
    fun addChapterAtIndex(newChap: GrammarChapNode, index: Int) {
        if (index > chapters.size) {
            chapters.add(newChap)
        } else {
            chapters.add(index, newChap)
        }
    }

    /**
     * removes given node from chapter list
     * @param remove chapter to remove
     */
    fun removeChapter(remove: GrammarChapNode) {
        chapters.remove(remove)
    }
    /**
     * builds and returns new grammar node
     */
    /**
     * Adds or changes a grammar recording.
     * @param id ID of sound to replace. -1 if newly adding
     * @param newRecord New wave recording
     * @return ID of sound replaced/created, -1 if null passed in
     */
    fun addChangeRecording(id: Int, newRecord: ByteArray?): Int {
        var ret = id
        if (newRecord == null) {
            return -1
        }
        if (ret == -1) {
            ret = 0
            while (soundMap.containsKey(ret)) {
                ret++
            }
            soundMap[ret] = newRecord
        } else {
            soundMap.remove(ret)
            soundMap[ret] = newRecord
        }
        return ret
    }

    @Throws(Exception::class)
    fun getRecording(id: Int): ByteArray? {
        var ret: ByteArray? = null
        if (id != -1) {
            ret = if (soundMap.containsKey(id)) {
                soundMap[id]
            } else {
                throw Exception("Unable to retrieve related recording with ID: $id")
            }
        }
        return ret
    }

    /**
     * Creates a new grammar section node
     * @return new section node
     */
    val newSection: GrammarSectionNode
        get() = GrammarSectionNode(this)

    /**
     * Writes all Grammar information to XML document
     * @param doc Document to write to
     * @param rootElement root element of document
     */
    fun writeXML(doc: Document, rootElement: Element) {
        val grammarRoot: Element = doc.createElement(PGTUtil.grammarSectionXID)
        rootElement.appendChild(grammarRoot)
        for (chapter in chapters) {
            chapter.writeXML(doc, grammarRoot)
        }
    }

    init {
        soundMap = HashMap()
        buffer = GrammarChapNode(this)
    }
}
