package com.polyglotandroid.core.managers

import com.polyglotandroid.core.DictCore
import com.polyglotandroid.core.PGUtil
import com.polyglotandroid.core.nodes.*
import org.w3c.dom.Document
import org.w3c.dom.Element
import java.util.function.Function


class DeclensionManager(private val core: DictCore) {

    // Integer is ID of related word, list is list of declension nodes
    private val dList: MutableMap<Int, MutableList<DeclensionNode>> =
        HashMap()

    // Integer is ID of related type, list is list of declensions for this type
    private val dTemplates: MutableMap<Int, MutableList<DeclensionNode>> =
        HashMap()

    // If specific combined declensions require additional settings in the future,
    // change the boolean here to an object which will store them
    private val combSettings: MutableMap<String, Boolean> =
        HashMap()
    private var topId = 0
    var isBufferDecTemp = false
    var bufferRelId = -1

    /**
     * gets current declension node buffer object
     *
     * @return buffer node object
     */
    var buffer = DeclensionNode(-1)
        private set
    private val generationRules: MutableList<DeclensionGenerationRule> = ArrayList()
    private var ruleBuffer: DeclensionGenerationRule = DeclensionGenerationRule()
    fun isCombinedDeclensionSuppressed(_combId: String, _typeId: Int): Boolean {
        val storeId = "$_typeId,$_combId"
        return if (!combSettings.containsKey(storeId)) {
            false
        } else combSettings[storeId]!!
    }

    fun setCombinedDeclensionSuppressed(
        _combId: String,
        _typeId: Int,
        _suppress: Boolean
    ) {
        val storeId = "$_typeId,$_combId"
        if (!combSettings.containsKey(storeId)) {
            combSettings[storeId] = _suppress
        } else {
            combSettings.replace(storeId, _suppress)
        }
    }

    /**
     * This sets the suppression data raw. Should only be used when loading from a file
     * @param _completeId complete, raw ID of data
     * @param _suppress suppression value
     */
    fun setCombinedDeclensionSuppressedRaw(
        _completeId: String,
        _suppress: Boolean
    ) {
        combSettings[_completeId] = _suppress
    }

    /**
     * Gets list of all deprecated autogeneration rules
     *
     * @param typeId type to get deprecated values for
     * @return list of all deprecated gen rules
     */
    fun getAllDepGenerationRules(typeId: Int): List<DeclensionGenerationRule> {
        val ret: MutableList<DeclensionGenerationRule> = ArrayList()
        val typeRules = getAllCombinedIds(typeId)
        val ruleMap: MutableMap<String, Int> = HashMap()

        // creates searchable map of extant combination IDs
        for (curPair in typeRules) {
            ruleMap[curPair.combinedId] = 0
        }
        var highestIndex = 0
        for (curRule in generationRules) {
            val curRuleIndex: Int = curRule.index
            highestIndex = if (highestIndex > curRuleIndex) highestIndex else curRuleIndex
        }
        for (curRule in generationRules) {
            // adds to return value only if rule matches ID, and is orphaned
            if (curRule.index == 0) {
                highestIndex++
                curRule.index = highestIndex
            }
            if (curRule.typeId == typeId
                && !ruleMap.containsKey(curRule.combinationId)
            ) {
                ret.add(curRule)
            }
        }
        ret.sort()
        return ret
    }

    /**
     * Gets current declension rule buffer
     *
     * @return current declension rule buffer
     */
    fun getRuleBuffer(): DeclensionGenerationRule {
        return ruleBuffer
    }

    /**
     * inserts current rule buffer and sets to blank value
     */
    fun insRuleBuffer() {
        addDeclensionGenerationRule(ruleBuffer)
        ruleBuffer = DeclensionGenerationRule()
    }

    /**
     * add a declension generation rule to the list
     *
     * @param newRule rule to add
     */
    fun addDeclensionGenerationRule(newRule: DeclensionGenerationRule) {
        generationRules.add(newRule)
    }

    /**
     * delete all rules of a particular typeID from rule set
     *
     * @param typeId ID of type to wipe
     */
    fun wipeDeclensionGenerationRules(typeId: Int) {
        val rulesList: List<DeclensionGenerationRule> = ArrayList(generationRules)
        for (rule in rulesList) {
            if (rule.typeId == typeId) {
                generationRules.remove(rule)
            }
        }
    }

    /**
     * Deletes rule based on unique regex value
     *
     * @param delRule rule to delete
     */
    fun deleteDeclensionGenerationRule(delRule: DeclensionGenerationRule) {
        generationRules.remove(delRule)
    }

    /**
     * get list of all declension rules for a particular type
     *
     * @param typeId id of part of speech to collect all rules for (does not account for class filtering)
     * @return list of rules
     */
    fun getDeclensionRulesForType(typeId: Int): List<DeclensionGenerationRule> {
        val ret: MutableList<DeclensionGenerationRule> = ArrayList()
        val itRules: List<DeclensionGenerationRule> = generationRules
        var missingId = 0 //used for missing index values (index system bolton)
        for (curRule in itRules) {
            if (curRule.index == 0 || curRule.index == missingId) {
                missingId++
                curRule.index = missingId
            } else {
                missingId = curRule.index
            }
            if (curRule.typeId == typeId) {
                ret.add(curRule)
            }
        }
        ret.sort()

        // ensure that all rules cave continguous IDs before returning
        var i = 1
        for (curRule in ret) {
            curRule.index = i
            i++
        }
        return ret
    }

    /**
     * get list of all declension rules for a given word based on word type and word class values
     *
     * @param word word to get rules for (takes into account word type (PoS) & classes/class values it has
     * @return list of rules
     */
    fun getDeclensionRules(word: ConWord?): List<DeclensionGenerationRule> {
        val ret: MutableList<DeclensionGenerationRule> = ArrayList()
        val itRules: List<DeclensionGenerationRule> = generationRules
        var missingId = 0 //used for missing index values (index system bolton)
        for (curRule in itRules) {
            if (curRule.index == 0 || curRule.index == missingId) {
                missingId++
                curRule.index = missingId
            } else {
                missingId = curRule.index
            }
            if (curRule.doesRuleApplyToWord(word)) {
                ret.add(curRule)
            }
        }
        ret.sort()

        // ensure that all rules cave continguous IDs before returning
        var i = 1
        for (curRule in ret) {
            curRule.index = i
            i++
        }
        return ret
    }

    /**
     * Generates the new form of a declined/conjugated word based on rules for
     * its type
     *
     * @param word to transform
     * @param combinedId combined ID of word form to create
     * @param base base word string
     * @return new word value if exists, empty string otherwise
     * @throws java.lang.Exception on bad regex
     */
    @Throws(Exception::class)
    fun declineWord(
        word: ConWord?,
        combinedId: String?,
        base: String
    ): String {
        var base = base
        val typeRules: Iterator<DeclensionGenerationRule> =
            getDeclensionRules(word).iterator()
        var ret = ""
        while (typeRules.hasNext()) {
            val curRule: DeclensionGenerationRule = typeRules.next()

            // skip all entries not applicable to this particular combined word ID
            if (!curRule.combinationId
                    .equals(combinedId) || !curRule.doesRuleApplyToWord(word)
            ) {
                continue
            }

            // apply transforms within rule if rule matches current base
            if (base.matches(curRule.regex)) {
                val transforms: List<DeclensionGenerationTransform> =
                    curRule.transformations
                for (curTrans in transforms) {
                    try {
                        base = base.replace(curTrans.regex.toRegex(), curTrans.replaceText)
                    } catch (e: Exception) {
                        throw Exception(
                            "Unable to create declension/conjugation "
                                    + "due to malformed regex (modify in Parts of Speech->Autogeneration): "
                                    + e.localizedMessage
                        )
                    }
                    ret = base
                }
            }
        }
        return ret
    }

    val declensionMap: Map<Int, MutableList<DeclensionNode>>
        get() = dList

    fun addDeclensionToWord(
        wordId: Int,
        declensionId: Int,
        declension: DeclensionNode
    ) {
        addDeclension(wordId, declensionId, declension, dList)
    }

    fun deleteDeclensionFromWord(wordId: Int, declensionId: Int?) {
        deleteDeclension(wordId, declensionId, dList)
    }

    fun updateDeclensionWord(
        wordId: Int,
        declensionId: Int,
        declension: DeclensionNode
    ) {
        updateDeclension(wordId, declensionId, declension, dList)
    }

    /**
     * sets all declensions to deprecated state
     * @param typeId ID of type to deprecate declensions for
     */
    fun deprecateAllDeclensions(typeId: Int?) {
        val decIt: Iterator<Map.Entry<Int, List<DeclensionNode>>> =
            dList.entries.iterator()
        while (decIt.hasNext()) {
            val curEntry =
                decIt.next()
            val curList = curEntry.value

            // only run for declensions of words with particular type
            if (!core.wordCollection.getNodeById(curEntry.key).getWordTypeId()
                    .equals(typeId)
            ) {
                continue
            }
            val nodeIt = curList.iterator()
            while (nodeIt.hasNext()) {
                val curNode = nodeIt.next()
                curNode.combinedDimId = "D" + curNode.combinedDimId
            }
        }
    }

    /**
     * Gets a particular declension template of a particular word type
     *
     * @param typeId the type which contains the declension in question
     * @param declensionId the declension within the type to retrieve
     * @return the object representing the declension
     */
    fun getDeclension(typeId: Int, declensionId: Int?): DeclensionNode? {
        var ret: DeclensionNode? = null
        val decList =
            dTemplates[typeId] as List<DeclensionNode>?

        // only search farther if declension itself actually exists
        if (decList != null) {
            val decIt = decList.iterator()
            while (decIt.hasNext()) {
                val curNode = decIt.next()
                if (curNode.id.equals(declensionId)) {
                    ret = curNode
                    break
                }
            }
        }
        return ret
    }

    /**
     * Clears all declensions from word
     *
     * @param wordId ID of word to clear of all declensions
     */
    fun clearAllDeclensionsWord(wordId: Int) {
        clearAllDeclensions(wordId, dList)
    }

    /**
     * get list of all labels and combined IDs of all declension combinations
     * for a type
     *
     * @param typeId ID of type to fetch combined IDs for
     * @return list of labels and IDs
     */
    fun getAllCombinedIds(typeId: Int): List<DeclensionPair> {
        val dimensionalDeclensionNodes =
            getDimensionalDeclensionListTemplate(typeId)
        val singletonDeclensionNodes =
            getSingletonDeclensionList(typeId, dTemplates)
        val ret =
            getAllCombinedDimensionalIds(0, ",", "", dimensionalDeclensionNodes)
        ret.addAll(getAllSingletonIds(singletonDeclensionNodes))
        return ret
    }

    /**
     * get list of all labels and combined IDs of dimensional declension combinations
     * for a type
     *
     * @param typeId ID of type to fetch combined IDs for
     * @return list of labels and IDs
     */
    fun getDimensionalCombinedIds(typeId: Int): List<DeclensionPair> {
        val dimensionalDeclensionNodes =
            getDimensionalDeclensionListTemplate(typeId)
        return getAllCombinedDimensionalIds(0, ",", "", dimensionalDeclensionNodes)
    }

    /**
     * get list of all labels and combined IDs of singleton declension combinations
     * for a type
     *
     * @param typeId ID of type to fetch combined IDs for
     * @return list of labels and IDs
     */
    fun getSingletonCombinedIds(typeId: Int): List<DeclensionPair> {
        val singletonDeclensionNodes =
            getSingletonDeclensionList(typeId, dTemplates)
        return getAllSingletonIds(singletonDeclensionNodes)
    }

    fun getAllSingletonIds(declensionList: List<DeclensionNode>): List<DeclensionPair> {
        val ret: MutableList<DeclensionPair> = ArrayList()
        for (curNode in declensionList) {
            val curPair = DeclensionPair(curNode.combinedDimId, curNode.value)
            ret.add(curPair)
        }
        return ret
    }

    /**
     * Gets the location of a dimension's id in a dimensionalID string
     * @param typeId part of speech associated with dimension
     * @param node dimension to find
     * @return locational index within dimensional ids (-1 if no value found)
     */
    fun getDimensionTemplateIndex(typeId: Int, node: DeclensionNode): Int {
        var ret = -1
        if (dTemplates.containsKey(typeId)) {
            val declensionValues: List<DeclensionNode> = dTemplates[typeId]!!
            ret = declensionValues.indexOf(node)

            // must loop through due to inclusion of singleton declensions here
            var numSingleton = 0
            for (testNode in declensionValues) {
                if (testNode.dimensionless) {
                    numSingleton++
                }
                if (node.id.equals(testNode.id)) {
                    break
                }
            }
            ret -= numSingleton
        }
        return ret
    }

    /**
     * Gets a declension node based on positional index (rather than ID)
     * @param typeId
     * @param index
     * @return
     */
    fun getDeclentionTemplateByIndex(typeId: Int, index: Int): DeclensionNode {
        return dTemplates[typeId]!![index]
    }

    /**
     * Same as above, but SKIPS indecies of singleton declensions
     * @param typeId
     * @param index
     * @return null if none found
     */
    fun getDimensionalDeclentionTemplateByIndex(typeId: Int, index: Int): DeclensionNode? {
        var ret: DeclensionNode? = null
        val nodes: List<DeclensionNode> = dTemplates[typeId]!!
        var curIndex = 0
        for (node in nodes) {
            if (node.dimensionless) {
                continue
            } else if (curIndex == index) {
                ret = node
                break
            }
            curIndex++
        }
        return ret
    }

    /**
     * recursive method to calculate value of overridden method
     *
     * @param depth current depth in calculation
     * @param curId current combined ID
     * @param curLabel current constructed label
     * @param declensionList list of template declensions for type
     * @return list of currently constructed labels and ids
     */
    private fun getAllCombinedDimensionalIds(
        depth: Int,
        curId: String,
        curLabel: String,
        declensionList: List<DeclensionNode>
    ): MutableList<DeclensionPair> {
        val ret: MutableList<DeclensionPair> = ArrayList()

        // for the specific case that a word with no declension patterns has a deprecated declension
        if (declensionList.isEmpty()) {
            return ret
        }
        if (depth >= declensionList.size) {
            ret.add(DeclensionPair(curId, curLabel))
        } else {
            val curNode = declensionList[depth]
            val dimensions: Collection<DeclensionDimension> = curNode.dimensions
            val dimIt = dimensions.iterator()
            while (dimIt.hasNext()) {
                val curDim = dimIt.next()
                ret.addAll(
                    getAllCombinedDimensionalIds(
                        depth + 1,
                        curId + curDim.id.toString() + ",",
                        curLabel + (if (curLabel.isEmpty()) "" else " ") + curDim.value,
                        declensionList
                    )
                )
            }
        }
        return ret
    }
    // TODO: Do I need this at all? Can I have ONLY the full pull, rather than including dimensional?
    /**
     * Fetches list of declined/conjugated wordforms for a given word. Only pulls dimensional values. Singletons like
     * gerunds are not included
     * Note: This DOES include deprecated wordforms! Be aware!
     * @param wordId
     * @return
     */
    fun getDimensionalDeclensionListWord(wordId: Int): List<DeclensionNode> {
        return getDimensionalDeclensionList(wordId, dList)
    }

    /**
     * Gets list of dimensional template values. Does not pull singletons such as gerunds.
     * @param typeId
     * @return
     */
    fun getDimensionalDeclensionListTemplate(typeId: Int): List<DeclensionNode> {
        return getDimensionalDeclensionList(typeId, dTemplates)
    }

    /**
     * Gets full list of dimensional template values including singletons such as gerunds.
     * @param typeId
     * @return
     */
    fun getFullDeclensionListTemplate(typeId: Int): List<DeclensionNode> {
        return getFullDeclensionList(typeId, dTemplates)
    }

    fun addDeclensionToTemplate(
        typeId: Int,
        declensionId: Int,
        declension: DeclensionNode
    ): DeclensionNode {
        return addDeclension(typeId, declensionId, declension, dTemplates)
    }

    fun addDeclensionToTemplate(typeId: Int, declension: String): DeclensionNode {
        return addDeclension(typeId, declension, dTemplates)
    }

    fun deleteDeclensionFromTemplate(typeId: Int, declensionId: Int?) {
        deleteDeclension(typeId, declensionId, dTemplates)
    }

    fun updateDeclensionTemplate(
        typeId: Int,
        declensionId: Int,
        declension: DeclensionNode
    ) {
        updateDeclension(typeId, declensionId, declension, dTemplates)
    }

    fun getDeclensionTemplate(typeId: Int, templateId: Int?): DeclensionNode? {
        val searchList =
            dTemplates[typeId] as List<DeclensionNode>
        val search: Iterator<*> = searchList.iterator()
        var ret: DeclensionNode? = null
        while (search.hasNext()) {
            val test = search.next() as DeclensionNode
            if (test.id == templateId) {
                ret = test
                break
            }
        }
        return ret
    }

    /**
     * Clears all declensions from word
     *
     * @param typeId ID of word to clear of all declensions
     */
    fun clearAllDeclensionsTemplate(typeId: Int) {
        clearAllDeclensions(typeId, dTemplates)
    }

    fun setBufferId(_bufferId: Int?) {
        buffer.id = _bufferId!!
    }

    var bufferDecText: String?
        get() = buffer.value
        set(_bufferDecText) {
            buffer.value = _bufferDecText!!
        }

    var bufferDecNotes: String?
        get() = buffer.notes
        set(_bufferDecNotes) {
            buffer.notes = _bufferDecNotes!!
        }

    fun insertBuffer() {
        if (isBufferDecTemp) {
            this.addDeclensionToTemplate(bufferRelId, buffer.id, buffer)
        } else {
            addDeclensionToWord(bufferRelId, buffer.id, buffer)
        }
    }

    fun clearBuffer() {
        buffer = DeclensionNode(-1)
        isBufferDecTemp = false
        bufferRelId = -1
    }

    private fun addDeclension(
        typeId: Int,
        declension: String,
        idToDecNodes: MutableMap<Int, MutableList<DeclensionNode>>
    ): DeclensionNode {
        val wordList: MutableList<DeclensionNode>
        topId++
        if (idToDecNodes.containsKey(typeId)) {
            wordList = idToDecNodes[typeId] as MutableList<DeclensionNode>
        } else {
            wordList = ArrayList()
            idToDecNodes[typeId] = wordList
        }
        val addNode = DeclensionNode(topId)
        addNode.value = declension
        wordList.add(addNode)
        return addNode
    }

    /**
     * Adds declension to related object (type or word)
     *
     * @param relId ID of related object
     * @param declensionId ID of declension to be created
     * @param declension declension node to be created
     * @param list list to add node to (word list or type list)
     * @return declension node created
     */
    private fun addDeclension(
        relId: Int,
        declensionId: Int,
        declension: DeclensionNode,
        list: MutableMap<Int, MutableList<DeclensionNode>>
    ): DeclensionNode {
        var declensionId = declensionId
        val wordList: MutableList<DeclensionNode>
        if (declensionId == -1) {
            declensionId = topId + 1
        }
        deleteDeclensionFromWord(relId, declensionId)
        if (list.containsKey(relId)) {
            wordList = list[relId] as MutableList<DeclensionNode>
        } else {
            wordList = ArrayList()
            list[relId] = wordList
        }
        val addNode = DeclensionNode(declensionId)
        addNode.setEqual(declension)
        wordList.add(addNode)
        if (declensionId > topId) {
            topId = declensionId
        }
        return addNode
    }

    /**
     * Gets stored declension for a word from combined dimensional Id of declension.
     * This does NOT generate a new declension, and is primarily of use with overridden
     * values and language files which do not use autodeclension.
     *
     * @param wordId the id of the root word
     * @param dimId the combined dim Id of the dimension
     * @return The declension node if found, null if otherwise
     */
    fun getDeclensionByCombinedId(wordId: Int, dimId: String): DeclensionNode? {
        var ret: DeclensionNode? = null
        if (dList.containsKey(wordId)) {
            for (test in dList[wordId] as List<DeclensionNode>) {
                if (dimId == test.combinedDimId) {
                    ret = test
                    break
                }
            }
        }
        return ret
    }

    fun getCombNameFromCombId(typeId: Int, combId: String): String {
        var ret = ""
        val it =
            getDimensionalDeclensionListTemplate(typeId).iterator()
        val splitIds = combId.split(",").toTypedArray()
        var i = 0
        while (it.hasNext()) {
            val curNode = it.next()
            val dimId = splitIds[i + 1].toInt()
            val dimIt: Iterator<DeclensionDimension> =
                curNode.dimensions.iterator()
            var curDim: DeclensionDimension? = null
            while (dimIt.hasNext()) {
                curDim = dimIt.next()
                if (curDim.id.equals(dimId)) {
                    break
                }
            }
            if (curDim != null) {
                ret += " " + curDim.value
            }
            i++
        }
        return ret.trim { it <= ' ' }
    }

    fun deleteDeclension(
        typeId: Int,
        declensionId: Int?,
        list: MutableMap<Int, MutableList<DeclensionNode>>
    ) {
        if (list.containsKey(typeId)) {
            val copyTo: MutableList<DeclensionNode> = ArrayList()
            val copyFrom =
                (list[typeId] as List<DeclensionNode>).iterator()
            while (copyFrom.hasNext()) {
                val curNode = copyFrom.next()
                if (curNode.id.equals(declensionId)) {
                    continue
                }
                copyTo.add(curNode)
            }
            list.remove(typeId)

            // if unpopulated, allow to not exist. Cleaner.
            if (copyTo.size > 0) {
                list[typeId] = copyTo
            }
        }
    }

    private fun updateDeclension(
        typeId: Int,
        declensionId: Int,
        declension: DeclensionNode,
        list: MutableMap<Int, MutableList<DeclensionNode>>
    ) {
        if (list.containsKey(typeId)) {
            val copyTo: MutableList<DeclensionNode> = ArrayList()
            val copyFrom =
                (list[typeId] as List<DeclensionNode>).iterator()
            while (copyFrom.hasNext()) {
                val curNode = copyFrom.next()
                if (curNode.id.equals(declensionId)) {
                    val modified = DeclensionNode(declensionId)
                    modified.setEqual(declension)
                    copyTo.add(modified)
                    continue
                }
                copyTo.add(curNode)
            }
            list.remove(typeId)
            list[typeId] = copyTo
        }
    }

    /**
     * Clears all declensions from word
     *
     * @param wordId ID of word to clear of all declensions
     */
    private fun clearAllDeclensions(wordId: Int, list: Map<*, *>) {
        if (list.containsKey(wordId)) {
            list.remove(wordId)
        }
    }

    /**
     * Retrieves all dimensional declensions based on related ID and the list to be pulled from. The list can either
     * be the templates (related via typeId) or actual words, related by wordId
     * @param relatedId ID of related value
     * @param valueMap list of relations to search through
     * @return
     */
    private fun getDimensionalDeclensionList(
        relatedId: Int,
        valueMap: Map<Int, MutableList<DeclensionNode>>
    ): List<DeclensionNode> {
        val ret: MutableList<DeclensionNode> = ArrayList()
        if (valueMap.containsKey(relatedId)) {
            val allNodes: List<DeclensionNode> = valueMap[relatedId]!!
            for (curNode in allNodes) {
                // dimensionless nodes
                if (!curNode.dimensionless) {
                    ret.add(curNode)
                }
            }
        }
        return ret
    }

    /**
     * Public version of private method directly below.
     * Retrieves all singleton declensions based on related ID and the list to be pulled from. The list can either
     * be the templates (related via typeId) or actual words, related by wordId
     * @param relatedId ID of related value
     * @return
     */
    fun getSingletonDeclensionList(relatedId: Int): List<DeclensionNode> {
        return getSingletonDeclensionList(relatedId, dTemplates)
    }

    /**
     * Retrieves all singleton declensions based on related ID and the list to be pulled from. The list can either
     * be the templates (related via typeId) or actual words, related by wordId
     * @param relatedId ID of related value
     * @param list list of relations to search through
     * @return
     */
    private fun getSingletonDeclensionList(
        relatedId: Int,
        list: Map<Int, MutableList<DeclensionNode>>
    ): List<DeclensionNode> {
        val ret: MutableList<DeclensionNode> = ArrayList()
        if (list.containsKey(relatedId)) {
            val allNodes: List<DeclensionNode> = list[relatedId]!!
            for (curNode in allNodes) {
                // dimensionless nodes
                if (curNode.dimensionless) {
                    ret.add(curNode)
                }
            }
        }
        return ret
    }

    /**
     * Returns full list of declensions irrespective of whether they are dimensional or not. Will return singletons
     * such as gerunds.
     * @param relatedId ID of related value
     * @param list list of relations to search through
     * @return
     */
    private fun getFullDeclensionList(
        relatedId: Int,
        list: Map<Int, MutableList<DeclensionNode>>
    ): List<DeclensionNode> {
        var ret: List<DeclensionNode> = ArrayList()
        if (list.containsKey(relatedId)) {
            ret = list[relatedId] as List<DeclensionNode>
        }
        return ret
    }

    fun getFullDeclensionListWord(wordId: Int): List<DeclensionNode> {
        return getFullDeclensionList(wordId, dList)
    }

    /**
     * Gets a word's declensions, with their combined dim Ids as the keys
     * DOES NOT GENERATE DECLENSIONS THAT ARE SET TO AUTOGENERATE, BUT HAVE
     * NOT YET BEEN SAVED.
     * Note: This returns deprecated wordforms as well as current ones.
     * @param wordId word to get declensions of
     * @return map of all declensions in a word (empty if none)
     */
    fun getWordDeclensions(wordId: Int): Map<String, DeclensionNode> {
        val ret: MutableMap<String, DeclensionNode> = HashMap()
        val decs =
            getDimensionalDeclensionListWord(wordId).iterator()
        while (decs.hasNext()) {
            val curNode = decs.next()
            ret[curNode.combinedDimId] = curNode
        }
        return ret
    }

    /**
     * Removes all declensions contained in decMap from word with wordid
     * @param wordId ID of word to clear values from
     * @param removeVals values to clear from word
     */
    fun removeDeclensionValues(
        wordId: Int,
        removeVals: Collection<DeclensionNode>
    ) {
        val wordList =
            dList[wordId] as MutableList<DeclensionNode>
        for (remNode in removeVals) {
            wordList.remove(remNode)
        }
    }

    /**
     * Writes all declension information to XML document
     *
     * @param doc Document to write to
     * @param rootElement root element of document
     */
    fun writeXML(doc: Document, rootElement: Element) {
        var declensionSet: Set<Map.Entry<Int, List<DeclensionNode>>>
        val declensionCollection: Element = doc.createElement(PGUtil.DECLENSION_COLLECTION_XID)
        rootElement.appendChild(declensionCollection)

        // record declension templates
        declensionSet = dTemplates.entries
        for ((relatedId, value) in declensionSet) {
            for (curNode in value) {
                curNode.writeXMLTemplate(
                    doc,
                    declensionCollection,
                    relatedId
                )
            }
        }

        // record word declensions
        declensionSet = declensionMap.entries
        for ((relatedId, value) in declensionSet) {
            for (curNode in value) {
                curNode.writeXMLTemplate(
                    doc,
                    declensionCollection,
                    relatedId
                )
            }
        }

        // record declension autogeneration rules
        for (curRule in generationRules) {
            curRule.writeXML(
                doc,
                declensionCollection
            )
        }

        // record combined form settings
        val combinedForms: Element = doc.createElement(PGUtil.DECLENSION_COMBINED_FORM_SECTION_XID)
        rootElement.appendChild(combinedForms)
        combSettings.entries.stream()
            .map(
                Function<Map.Entry<String, Boolean>, Any> { pairs: Map.Entry<String, Boolean> ->
                    val curCombForm: Element = doc.createElement(PGUtil.decCombinedFormXID)
                    var curAttrib: Element
                    // This section will have to be slightly rewritten if the combined settings become more complex
                    curAttrib = doc.createElement(PGUtil.decCombinedIdXID)
                    curAttrib.appendChild(doc.createTextNode(pairs.key))
                    curCombForm.appendChild(curAttrib)
                    curAttrib = doc.createElement(PGUtil.decCombinedSurpressXID)
                    curAttrib.appendChild(doc.createTextNode(if (pairs.value) PGUtil.True else PGUtil.False))
                    curCombForm.appendChild(curAttrib)
                    curCombForm
                }
            )
            .forEachOrdered { curCombForm: Any? ->
                combinedForms.appendChild(curCombForm)
            }
    }

    /**
     * This copies a list of rules to the bottom of the list of all declension templates for a given part of speech
     * that share a declension (decId) with the value defined by dimId
     *
     * NOTE: Only applies to dimensional declensions.Singletons must be copied to manually.
     *
     * @param typeId Part of speech to target
     * @param decId declension dimension to target
     * @param dimId dimension value to target
     * @param rules rules to be copied
     * @param selfCombId The combined ID of the form this was initially called from (do not copy duplicate of rule to self)
     */
    fun copyRulesToDeclensionTemplates(
        typeId: Int,
        decId: Int, dimId: Int,
        rules: List<DeclensionGenerationRule?>,
        selfCombId: String
    ) {
        val allNodes =
            getDimensionalDeclensionListTemplate(typeId)
        val decList: List<DeclensionPair> =
            getAllCombinedDimensionalIds(0, ",", "", allNodes)
        for (decPair in decList) {
            // only copy rule if distinct from base word form && it matches the dimensional value matches
            if (decPair.combinedId != selfCombId && combDimIdMatches(
                    decId,
                    dimId,
                    decPair.combinedId
                )
            ) {
                for (rule in rules) {
                    // insert rule
                    val newRule = DeclensionGenerationRule()
                    newRule.setEqual(rule, false)
                    newRule.typeId = typeId
                    newRule.combinationId = decPair.combinedId
                    newRule.index = 0
                    addDeclensionGenerationRule(newRule)

                    // call get rules for type (will automatically assign next highest index to rule
                    getAllDepGenerationRules(typeId)
                }
            }
        }
    }
    //deleteRulesFromDeclensionTemplates
    /**
     * This copies a list of rules to the bottom of the list of all declension templates for a given part of speech
     * that share a declension (decId) with the value defined by dimId
     *
     * NOTE: Only applies to dimensional declensions.Singletons must be copied to manually.
     *
     * @param typeId Part of speech to target
     * @param decId declension dimension to target
     * @param dimId dimension value to target
     * @param rulesToDelete rules to be deleted
     */
    fun deleteRulesFromDeclensionTemplates(
        typeId: Int,
        decId: Int, dimId: Int,
        rulesToDelete: List<DeclensionGenerationRule?>
    ) {
        for (rule in getDeclensionRulesForType(typeId)) {
            if (combDimIdMatches(decId, dimId, rule.combinationId)) {
                for (ruleDelete in rulesToDelete) {
                    if (rule.valuesEqual(ruleDelete)) {
                        deleteDeclensionGenerationRule(rule)
                    }
                }
            }
        }
    }

    /**
     * Deletes ALL instances of a rule within a given word type
     * @param typeId part of speech to clear
     * @param rulesToDelete rules in this pos to delete
     */
    fun bulkDeleteRuleFromDeclensionTemplates(
        typeId: Int,
        rulesToDelete: List<DeclensionGenerationRule?>
    ) {
        for (rule in getDeclensionRulesForType(typeId)) {
            for (ruleDelete in rulesToDelete) {
                if (rule.valuesEqual(ruleDelete)) {
                    deleteDeclensionGenerationRule(rule)
                }
            }
        }
    }

    private fun combDimIdMatches(decId: Int, dimId: Int, combDimId: String): Boolean {
        val strIds = combDimId.split(",").toTypedArray()
        val strId = strIds[decId + 1] // account for leading comma
        val dimValId = strId.toInt()
        return dimValId == dimId
    }

    fun getDeclensionLabel(typeId: Int, decId: Int): String {
        return dTemplates[typeId]!![decId].value
    }

    fun getDeclensionValueLabel(typeId: Int, decId: Int, decValId: Int): String {
        return dTemplates[typeId]!![decId].getDeclensionDimensionById(decValId)!!.value
    }

    /**
     * On load of older pgt files, must be called to maintain functionality of declension rules
     */
    fun setAllDeclensionRulesToAllClasses() {
        for (curRule in generationRules) {
            curRule.addClassToFilterList(
                -1,
                -1
            )
        }
    }

    /**
     * Returns all saved yet deprecated wordforms of a word
     * @param word
     * @return
     */
    fun getDeprecatedForms(word: ConWord): Map<String, DeclensionNode> {
        val ret: MutableMap<String, DeclensionNode> = HashMap()

        // first get all values that exist for this word
        for (node in getFullDeclensionListWord(word.id)) {
            ret[node.combinedDimId] = node
        }

        // then remove all values which match existing combined type ids
        for (pair in getAllCombinedIds(word.typeId)) {
            ret.remove(pair.combinedId)
        }
        return ret
    }

    /**
     * Returns true if given word has deprecated wordforms
     * @param word
     * @return
     */
    fun wordHasDeprecatedForms(word: ConWord): Boolean {
        return !getDeprecatedForms(word).isEmpty()
    }

}
