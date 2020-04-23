package com.polyglotandroid.core.collections

import com.polyglotandroid.core.DictCore
import com.polyglotandroid.core.nodes.ConWord
import com.polyglotandroid.core.nodes.PEntry
import com.polyglotandroid.core.nodes.WordClass
import java.util.function.Consumer


class WordClassCollection(_core: DictCore) : DictionaryCollection<WordClass?>() {
    private var comboCache: MutableList<List<PEntry<Int, Int>>>? =
        null
    private val core: DictCore
    val allWordClasses: List<WordClass>
        get() {
            val retList: List<WordClass> = ArrayList(nodeMap.values)
            return retList.sorted()
        }

    override fun clear() {
        bufferNode = WordClass()
    }

    /**
     * Inserts and blanks current buffer node
     *
     * @return inserted Id
     * @throws java.lang.Exception
     */
    @Throws(Exception::class)
    public override fun insert(): Int? {
        val ret: Int
        ret = if (bufferNode.getId() > 0) {
            this.insert(bufferNode.getId(), bufferNode)
        } else {
            super.insert(bufferNode)
        }
        bufferNode = WordClass()
        return ret
    }

    fun getClassesForType(classId: Int): List<WordClass> {
        val ret: MutableList<WordClass> = ArrayList()
        nodeMap.values.forEach(Consumer { prop: WordClass ->
            val curProp = prop
            if (curProp.appliesToType(classId)
                || curProp.appliesToType(-1)
            ) { // -1 is class "all"
                ret.add(curProp)
            }
        })
        Collections.sort(ret)
        return ret
    }

    /**
     * Writes all word class information to XML document
     *
     * @param doc Document to write to
     * @param rootElement root element of document
     */
    fun writeXML(doc: Document, rootElement: Element) {
        // element containing all classes
        val wordClasses: Element = doc.createElement(PGTUtil.ClassesNodeXID)

        // creates each class
        nodeMap.values.forEach(Consumer { curClass: WordClass ->
            curClass.writeXML(
                doc,
                wordClasses
            )
        })
        rootElement.appendChild(wordClasses)
    }

    /**
     * Gets random assortment of word class combinations based. Number of
     * combinations limited by parameters and by number of combinations
     * available
     *
     * @param numRandom number of entries to return
     * @return randomly generated combinations of word classes
     */
    fun getRandomPropertyCombinations(numRandom: Int): List<List<PEntry<Int, Int>>> {
        return getRandomPropertyCombinations(numRandom, null)
    }

    /**
     * Gets random assortment of word class combinations based. Number of
     * combinations limited by parameters and by number of combinations
     * available. a value can be excluded
     *
     * @param numRandom number of entries to return
     * @param excludeWord word with class properties to exclude (quiz generation
     * purposes)
     * @return randomly generated combinations of word classes
     */
    fun getRandomPropertyCombinations(
        numRandom: Int,
        excludeWord: ConWord?
    ): List<List<PEntry<Int, Int>>> {
        val ret: MutableList<List<PEntry<Int, Int>>> =
            ArrayList()
        var offset = 0
        Collections.shuffle(comboCache, Random(System.nanoTime()))
        if (comboCache != null && comboCache!!.size > 0) {
            var i = 0
            while (i - offset < numRandom && i + offset < comboCache!!.size) {
                if (propCombEqual(
                        comboCache!![i + offset],
                        ArrayList(excludeWord!!.getClassValues()!!)
                    )
                ) {
                    offset++
                    i++
                    continue
                }
                ret.add(comboCache!![i + offset])
                i++
            }
        }
        return ret
    }

    private fun propCombEqual(
        a: List<PEntry<Int, Int>>,
        b: List<Map.Entry<Int, Int>>
    ): Boolean {
        var ret = true
        if (a.size == b.size) {
            for (aEntry in a) {
                var aRet = false
                for (bEntry in b) {
                    if (aEntry == bEntry) {
                        aRet = true
                        break
                    }
                }
                ret = ret && aRet
            }
        } else {
            ret = false
        }
        return ret
    }

    /**
     * builds cache of every word class combination
     */
    fun buildComboCache() {
        comboCache = ArrayList()
        if (!nodeMap.isEmpty()) {
            ArrayList(nodeMap.values)?.let {
                buildComboCacheInternal(
                    0, it,
                    ArrayList()
                )
            }
        }
    }

    private fun buildComboCacheInternal(
        depth: Int,
        props: ArrayList<WordClass?>,
        curList: List<PEntry<Int, Int>>
    ) {
        val curProp = props[depth]
        for (curVal in curProp.getValues()) {
            val newList = ArrayList(curList)
            newList.add(PEntry(curProp.id, curVal.id))

            // if at max depth, cease recursion
            if (depth == props.size - 1) {
                comboCache!!.add(newList)
            } else {
                buildComboCacheInternal(depth + 1, props, newList)
            }
        }
    }

    /**
     * Call this after done with any functionality that uses the combo cache.
     * This must be cleared manually, as there is no predictive way to know that
     * the cache is finished with
     */
    fun clearComboCache() {
        comboCache = null
    }

    /**
     * returns true if the class/value ids given match up to existing values
     * returns false otherwise
     *
     * @param classId ID of word class to test
     * @param valId ID of value within word class to test
     * @return true if pair exists
     */
    fun isValid(classId: Int?, valId: Int?): Boolean {
        var ret = true
        if (!nodeMap.containsKey(classId)) {
            ret = false
        } else {
            if (!nodeMap[classId]!!.isValid(valId!!)) {
                ret = false
            }
        }
        return ret
    }

    /**
     * if a value is deleted from a class, this must be called. It tells the lexicon collection to cycle through all
     * words and eliminate instances where the given class/value combo appear
     * @param classId class from which value was deleted
     * @param valueId value deleted
     */
    fun classValueDeleted(classId: Int, valueId: Int) {
        core.getWordCollection().classValueDeleted(classId, valueId)
    }

    override fun notFoundNode(): Any? {
        val emptyClass = WordClass()
        emptyClass.value = "CLASS NOT FOUND"
        return emptyClass
    }

    init {
        bufferNode = WordClass()
        core = _core
    }

    override var bufferNode: WordClass
        get() = TODO("Not yet implemented")
        set(value) {}
}
