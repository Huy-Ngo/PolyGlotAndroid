package com.polyglotandroid.core.collections

import android.os.Build
import androidx.annotation.RequiresApi
import com.polyglotandroid.core.DictCore
import com.polyglotandroid.core.PGUtil
import com.polyglotandroid.core.WebInterface
import com.polyglotandroid.core.nodes.*
import org.w3c.dom.Document
import org.w3c.dom.Element
import java.util.*
import java.util.function.Consumer
import java.util.function.Predicate
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class ConWordCollection(_core: DictCore) : DictionaryCollection<ConWord?>() {
    private var bufferNode: ConWord
    private val splitChar = ","
    private val core: DictCore
    private val allConWords: MutableMap<String, Int>
    private val allLocalWords: MutableMap<String, Int>

    /**
     * Used to determine if lists should currently return in local order (this
     * is almost never used for anything but sorting. There is no setter.)
     *
     * @return whether to sort by local value
     */
    var isLocalOrder = false
        private set

    /**
     * inserts current buffer word to conWord list based on id; blanks out
     * buffer
     *
     * @param _id
     * @return
     * @throws Exception
     */
    @Throws(Exception::class)
    fun insert(_id: Int?): Int {
        val ret: Int
        val insWord = ConWord()
        insWord.core = core
        insWord.setEqual(bufferNode)
        insWord.id = _id!!
        bufferNode.setParent(this)
        bufferNode.core = core
        ret = super.insert(_id, bufferNode)
        balanceWordCounts(insWord, true)
        bufferNode = ConWord()
        (bufferNode as ConWord).core = core
        return ret
    }

    /**
     * Gets all words that are illegal in some way
     *
     * @return an iterator full of all illegal conwords
     */
    fun illegalFilter(): List<ConWord?> {
        val retList: MutableList<ConWord?> = ArrayList()
        nodeMap.values.stream()
            .filter(Predicate { word: ConWord -> !word.isWordLegal() })
            .forEach(
                Consumer { word: ConWord? -> retList.add(word) }
            )
        for (`object` in nodeMap.values) {
            val curWord = `object` as ConWord
            if (!curWord.isWordLegal()) {
                retList.add(curWord)
            }
        }
        Collections.sort(retList)
        return retList
    }

    /**
     * Checks whether word is legal and returns error reason if not
     *
     * @param word word to check legality of
     * @return Conword with any illegal entries saved as word values
     */
    override fun testWordLegality(word: ConWord): ConWord {
        val ret = ConWord()
        var pronunciation = ""
        try {
            pronunciation = word.pronunciation
        } catch (e: Exception) {
            // IOHandler.writeErrorLog(e);
            ret.definition =
                "Pronunciation cannot be generated, likely due to malformed regex in pronunciation menu."
        }
        if (word.value.length == 0) {
            ret.value = core.conLabel().toString() + " word value cannot be blank."
        }
        if (word.getWordTypeId() === 0 && core.getPropertiesManager().isTypesMandatory()) {
            ret.typeError = "Types set to mandatory."
        }
        if (word.localWord.isEmpty() && core.getPropertiesManager().isLocalMandatory()) {
            ret.localWord = core.localLabel().toString() + " word set to mandatory."
        }
        if (core.getPropertiesManager().isWordUniqueness() && core.getWordCollection()
                .containsWord(word.value)
        ) {
            ret.value = (ret.value + (if (ret.value.isEmpty()) "" else "\n")
                    + core.conLabel() + " words set to enforced unique: this conword exists elsewhere.")
        }
        if (core.getPropertiesManager()
                .isLocalUniqueness() && word.localWord.isNotEmpty() && core.getWordCollection()
                .containsLocalMultiples(word.localWord)
        ) {
            ret.localWord = (ret.localWord + (if (ret.localWord.isEmpty()) "" else "\n")
                    + core.localLabel() + " words set to enforced unique: this local exists elsewhere.")
        }
        val wordType: PartOfSpeechNode = core.getTypes().getNodeById(word.getWordTypeId())
        ret.definition = ret.definition + if (ret.definition.length == 0) "" else "\n"
        if (wordType != null) {
            val typeRegex: String = wordType.getPattern()
            if (wordType.isProcMandatory() && pronunciation.isEmpty() && !word.isProcOverride()) {
                ret.definition = (ret.definition + (if (ret.definition.isEmpty()) "" else "\n")
                        + "Pronunciation required for " + wordType.getValue() + " words.")
            }
            if (typeRegex.length != 0 && !word.value.matches(typeRegex)) {
                ret.definition = (ret.definition + (if (ret.definition.isEmpty()) "" else "\n")
                        + "Word does not match enforced pattern for type: " + word.wordTypeDisplay + ".")
                ret.setProcOverride(true)
            }
            if (wordType.isDefMandatory() && word.definition.isEmpty()) {
                ret.definition = (ret.definition + (if (ret.definition.isEmpty()) "" else "\n")
                        + "Definition required for " + wordType.getValue() + " words.")
            }
        }
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
    fun insert(): Int {
        val ret: Int
        (bufferNode as ConWord).setParent(this)
        (bufferNode as ConWord).core = core
        ret = super.insert(bufferNode)
        balanceWordCounts(bufferNode as ConWord, true)
        bufferNode = ConWord()
        (bufferNode as ConWord).core = core
        return ret
    }

    /**
     * Gets count of conwords in dictionary
     *
     * @return number of conwords in dictionary
     */
    val wordCount: Int
        get() = nodeMap.size

    /**
     * Tests whether collection contains a particular local word
     *
     * @param local string value to search for
     * @return whether multiples of local word exists in collection
     */
    fun containsLocalMultiples(local: String): Boolean {
        var ret = false
        if (allLocalWords.containsKey(local)) {
            ret = allLocalWords[local]!! > 1
        }
        return ret
    }

    /**
     * Tests whether collection contains a particular conword
     *
     * @param word string value to search for
     * @return whether multiples of conword exists in the collection
     */
    fun containsWord(word: String): Boolean {
        var ret = false
        if (allConWords.containsKey(word)) {
            ret = allConWords[word]!! > 1
        }
        return ret
    }

    /**
     * Balances count of conwords and localwords (string values)
     *
     * @param insWord word to factor into counts
     * @param additive true if adding, false if removing
     */
    private fun balanceWordCounts(insWord: ConWord, additive: Boolean) {
        var curCount = 0
        if (allConWords.containsKey(insWord.value)) {
            val tmp = allConWords[insWord.value]
            if (tmp != null) {
                curCount = tmp
            }
        }
        allConWords.remove(insWord.value)
        allConWords[insWord.value] = curCount + if (additive) 1 else -1
        curCount = 0
        if (allLocalWords.containsKey(insWord.localWord)) {
            val tmp = allLocalWords[insWord.localWord]
            if (tmp != null) {
                curCount = tmp
            }
        }
        allLocalWords.remove(insWord.localWord)
        allLocalWords[insWord.localWord] = curCount + if (additive) 1 else -1
    }

    /**
     * Balances word counts when modifying word value or local word MUST BE RUN
     * BEFORE PERSISTING NEW VALUES TO WORD
     *
     * @param id id of word to modify
     * @param wordVal new conword value
     * @param wordLoc new local word value
     */
    fun extertalBalanceWordCounts(
        id: Int?,
        wordVal: String?,
        wordLoc: String?
    ) {
        val oldWord = getNodeById(id)
        val newWord = ConWord()
        newWord.value = wordVal!!
        newWord.localWord = wordLoc!!
        balanceWordCounts(oldWord, false)
        balanceWordCounts(newWord, true)
    }

    /**
     * Tests whether a value exists in the dictionary currently
     *
     * @param word value to search for
     * @return true if exists, false otherwise
     */
    fun testWordValueExists(word: String): Boolean {
        return allConWords.containsKey(word) && allConWords[word]!! > 0
    }

    /**
     * Tests whether a value exists in the dictionary currently
     *
     * @param local value to search for
     * @return true if exists, false otherwise
     */
    fun testLocalValueExists(local: String): Boolean {
        return allLocalWords.containsKey(local) && allLocalWords[local]!! > 0
    }

    /**
     * Deletes word and balances all dependencies
     *
     * @param _id ID of word to delete
     * @throws Exception
     */
    @Throws(Exception::class)
    fun deleteNodeById(_id: Int?) {
        val deleteWord = getNodeById(_id)
        balanceWordCounts(deleteWord, false)
        super.deleteNodeById(_id)
        core.getDeclensionManager().clearAllDeclensionsWord(_id)
    }

    @Throws(Exception::class)
    fun modifyNode(_id: Int?, _modNode: ConWord) {
        // do bookkeepingfor word counts
        val oldWord = getNodeById(_id)
        balanceWordCounts(oldWord, false)
        balanceWordCounts(_modNode, true)
        _modNode.core = core
        super.modifyNode(_id, _modNode)
    }

    /**
     * Performs all actions of superclass, and additionally sets core value of
     * words
     *
     * @param _id same as super
     * @param _buffer same as super
     * @return same as super
     * @throws Exception same as super
     */
    @Throws(Exception::class)
    protected fun insert(_id: Int?, _buffer: ConWord): Int {
        _buffer.core = core
        _buffer.setParent(this)
        return super.insert(_id, _buffer)
    }

    /**
     * recalculates all non-overridden pronunciations
     *
     * @throws java.lang.Exception
     */
    @Throws(Exception::class)
    fun recalcAllProcs() {
        val words = wordNodes
        for (curWord in words) {
            // only runs if word's pronunciation not overridden
            if (!curWord.isProcOverride()) {
                curWord.pronunciation = core.getPronunciationMgr().getPronunciation(curWord.value)
                modifyNode(curWord.id, curWord)
            }
        }
    }

    /**
     * Returns list of words in descending list of synonym match
     *
     * @param _match The string value to match for
     * @return list of matching words
     */
    fun getSuggestedTransWords(_match: String): List<ConWord> {
        val localEquals: MutableList<ConWord> = ArrayList()
        val localContains: MutableList<ConWord> = ArrayList()
        val definitionContains: MutableList<RankedObject> = ArrayList()
        val allWords: Iterator<Map.Entry<Int, ConWord>> =
            nodeMap.entries.iterator()

        // on empty, return empty list
        if (_match.isEmpty()) {
            return localEquals
        }
        var curEntry: Map.Entry<Int, ConWord>
        var curWord: ConWord

        // cycles through all words, searching for matches
        while (allWords.hasNext()) {
            curEntry = allWords.next()
            curWord = curEntry.value
            var word = curWord.value
            var compare = _match
            var definition = curWord.definition

            // on ignore case, force all to lowercase
            if (core.getPropertiesManager().isIgnoreCase()) {
                word = word.toLowerCase()
                compare = compare.toLowerCase()
                definition = definition.toLowerCase()
            }
            if (word == compare) {
                // local word equility is the highest ranking match
                localEquals.add(curWord)
            } else if (word.contains(compare)) {
                // local word contains value is the second highest ranking match
                localContains.add(curWord)
            } else if (definition.contains(compare)) {
                // definition contains is ranked third, and itself raked inernally
                // by match position
                definitionContains.add(RankedObject(curWord, definition.indexOf(compare)))
            }
        }
        definitionContains.sort()

        // concatinate results
        val ret: ArrayList<ConWord> = ArrayList()
        ret.addAll(localEquals)
        ret.addAll(localContains)

        // must add through iteration here
        val it: Iterator<RankedObject> = definitionContains.iterator()
        while (it.hasNext()) {
            val curObject: RankedObject = it.next()
            val curDefMatch = curObject.getHolder() as ConWord
            ret.add(curDefMatch)
        }
        return ret
    }

    /**
     * Uses conword passed as parameter to filter on the entire dictionary of
     * words, based on attributes set on the parameter. Returns iterator of all
     * words that match. As a note: the conword value of the filter parameter is
     * matched not only against the values of all conwords in the dictionary,
     * but also their conjugations/declensions
     *
     * @param _filter A conword object containing filter values
     * @return an list of conwords which match the given search
     * @throws Exception on filtering error
     */
    @Throws(Exception::class)
    fun filteredList(_filter: ConWord): List<ConWord> {
        val retValues = ConWordCollection(core)
        retValues.alphaOrder = alphaOrder
        val filterList: Iterator<Map.Entry<Int, ConWord>> =
            nodeMap.entries.iterator() as Iterator<Map.Entry<Int, ConWord>>
        var curEntry: Map.Entry<Int, ConWord>
        var curWord: ConWord
        // definition search should always ignore case
        _filter.definition = _filter.definition.toLowerCase()

        // set filter to lowercase if ignoring case
        if (core.getPropertiesManager().isIgnoreCase()) {
            _filter.definition = _filter.definition.toLowerCase()
            _filter.localWord = _filter.localWord.toLowerCase()
            _filter.value = _filter.value.toLowerCase()
            _filter.pronunciation = _filter.pronunciation.toLowerCase()
        }
        while (filterList.hasNext()) {
            curEntry = filterList.next()
            curWord = curEntry.value
            try {
                // definition should always ignore case
                val definition: String =
                    FormattedTextHelper.getTextBody(curWord.definition).toLowerCase()
                val type: Int = curWord.getWordTypeId()
                var local: String
                var proc: String

                // if set to ignore case, set up caseless matches, normal otherwise
                if (core.getPropertiesManager().isIgnoreCase()) {
                    local = curWord.localWord.toLowerCase()
                    proc = curWord.pronunciation.toLowerCase()
                } else {
                    local = curWord.localWord
                    proc = curWord.pronunciation
                }

                // each filter test split up to minimize compares
                // definition
                if (!_filter.definition.trim { it <= ' ' }.isEmpty()) {
                    var cont = true
                    for (def1 in _filter.definition.split(splitChar)
                        .toTypedArray()) {
                        if (definition.contains(def1)) {
                            cont = false
                            break
                        }
                    }
                    if (cont) {
                        continue
                    }
                }

                // type (exact match only)
                if (_filter.getWordTypeId() !== 0
                    && type != _filter.getWordTypeId()
                ) {
                    continue
                }

                // local word
                if (_filter.localWord.trim { it <= ' ' }.isNotEmpty()) {
                    var cont = true
                    for (loc1 in _filter.localWord.split(splitChar)
                        .toTypedArray()) {
                        if (local.contains(loc1)) {
                            cont = false
                            break
                        }
                    }
                    if (cont) {
                        continue
                    }
                }

                // con word
                if (_filter.value.trim { it <= ' ' }.isNotEmpty()) {
                    var cont = true
                    for (val1 in _filter.value.split(splitChar).toTypedArray()) {
                        if (matchHeadAndDeclensions(val1, curWord)) {
                            cont = false
                            break
                        }
                    }
                    if (cont) {
                        continue
                    }
                }

                // pronunciation
                if (_filter.pronunciation.trim { it <= ' ' }.isNotEmpty()) {
                    var cont = true
                    for (proc1 in _filter.pronunciation.split(splitChar)
                        .toTypedArray()) {
                        if (proc.contains(proc1)) {
                            cont = false
                        }
                    }
                    if (cont) {
                        continue
                    }
                }

                // etymological root
                val parent = _filter.filterEtyParent
                if (parent != null) {
                    if (parent is ConWord) {
                        if (parent.id != -1 && !core.etymologyManager
                                .childHasParent(curWord.id, parent.id)
                        ) {
                            continue
                        }
                    }
                    if (parent is EtymologyExternalParent) {
                        val parExt: EtymologyExternalParent = parent
                        if (parExt.id !== -1 && !core.etymologyManager
                                .childHasExtParent(curWord.id, parExt.uniqueId)
                        ) {
                            continue
                        }
                    }
                }
                retValues.bufferWord = curWord
                retValues.insert(curWord.id)
            } catch (e: Exception) {
                // IOHandler.writeErrorLog(e);
                throw Exception("FILTERING ERROR: " + e.message)
            }
        }
        return retValues.wordNodes
    }

    /**
     * Tests whether matchText matches the headword of the passed word, or any
     * declensions/conjugations of the word.
     *
     * @param matchText Text to match.
     * @param word Word within which to search for matches
     * @return true if match, false otherwise
     */
    private fun matchHeadAndDeclensions(matchText: String, word: ConWord): Boolean {
        var ret = false
        val ignoreCase: Boolean = core.getPropertiesManager().isIgnoreCase()
        val head = if (ignoreCase) word.value.toLowerCase() else word.value
        if (matchText.trim { it <= ' ' }.isEmpty()
            || head.matches(matchText)
            || head.startsWith(matchText)
        ) {
            ret = true
        }
        val type: PartOfSpeechNode = core.getTypes().getNodeById(word.getWordTypeId())
        if (type != null && !ret) {
            val typeId: Int = type.id
            val decIt: Iterator<DeclensionPair> =
                core.getDeclensionManager().getAllCombinedIds(typeId).iterator()
            while (!ret && decIt.hasNext()) {
                // silently skip erroring entries. Too cumbersone to deal with during a search
                try {
                    val curPair = decIt.next()
                    val declension: String = core.getDeclensionManager()
                        .declineWord(word, curPair.combinedId, word.value)
                    if (!declension.trim { it <= ' ' }.isEmpty()
                        && (declension.matches(matchText)
                                || declension.startsWith(matchText))
                    ) {
                        ret = true
                    }
                } catch (e: Exception) {
                    // do nothing (see above comment)
                    // IOHandler.writeErrorLog(e);
                }
            }
        }
        return ret
    }

    fun getNodeById(_id: Int?): ConWord {
        return super.getNodeById(_id) as ConWord
    }

    /**
     * wipes current word buffer
     */
    fun clear() {
        bufferNode = ConWord()
        (bufferNode as ConWord).core = core
    }

    var bufferWord: ConWord
        get() = bufferNode as ConWord
        set(bufferWord) {
            this.bufferNode = bufferWord
            if (bufferWord.core == null) {
                bufferWord.core = core
            }
        }

    /**
     * returns iterator of nodes with their IDs as the entry key (ordered)
     *
     * @return
     */
    val wordNodes: ArrayList<ConWord?>
        get() {
            val retList: ArrayList<ConWord?> = ArrayList(nodeMap.values)
            retList.sort()
            return retList
        }// create new temp word for purposes of dictionary creation// cycle through and create copies of words with multiple local values

    /**
     * gets and returns iterator of all words based on alphabetical order of
     * localwords on the entries. Respects default alpha order.
     *
     * @return
     */
    val nodesLocalOrder: List<ConWord>
        get() {
            val cycleList: ArrayList<ConWord?> = ArrayList(nodeMap.values)
            val retList: MutableList<ConWord> = ArrayList()

            // cycle through and create copies of words with multiple local values
            for (word in cycleList) {
                val localPre = word.localWord
                if (localPre.contains(",")) {
                    val allLocals = localPre.split(",").toTypedArray()

                    // create new temp word for purposes of dictionary creation
                    for (curLocal in allLocals) {
                        val ins = ConWord()
                        ins.core = core
                        if (word != null) {
                            ins.setEqual(word)
                        }
                        ins.localWord = curLocal
                        ins.setParent(this)
                        retList.add(ins)
                    }
                } else {
                    retList.add(word)
                }
            }
            isLocalOrder = true
            retList.sort()
            isLocalOrder = false
            return retList
        }

    /**
     * Inserts new word into dictionary
     *
     * @param _addWord word to be inserted
     * @return ID of newly inserted word
     * @throws Exception
     */
    @Throws(Exception::class)
    fun addWord(_addWord: ConWord?): Int {
        val ret: Int
        bufferNode.setEqual(_addWord)
        ret = insert()
        return ret
    }

    /**
     * Writes all word information to XML document
     *
     * @param doc Document to write to
     * @param rootElement root element of document
     */
    @RequiresApi(Build.VERSION_CODES.N)
    fun writeXML(doc: Document, rootElement: Element) {
        val wordLoop = wordNodes
        val lexicon: Element = doc.createElement(PGUtil.LEXICON_XID)
        for (curWord in wordLoop.stream()) {
            if (curWord == null) continue
            val wordNode: Element = doc.createElement(PGUtil.WORD_XID)
            var wordValue: Element = doc.createElement(PGUtil.WORD_ID_XID)
            val wordId = curWord.id
            wordValue.appendChild(doc.createTextNode(wordId.toString()))
            wordNode.appendChild(wordValue)
            wordValue = doc.createElement(PGUtil.LOCAL_WORD_XID)
            wordValue.appendChild(doc.createTextNode(curWord.localWord))
            wordNode.appendChild(wordValue)
            wordValue = doc.createElement(PGUtil.CONWORD_XID)
            wordValue.appendChild(doc.createTextNode(curWord.value))
            wordNode.appendChild(wordValue)
            wordValue = doc.createElement(PGUtil.WORD_POS_ID_XID)
            wordValue.appendChild(doc.createTextNode(curWord.typeId.toString()))  // FIXME: typeID should become posID
            wordNode.appendChild(wordValue)
            try {
                wordValue = doc.createElement(PGUtil.WORD_PRONUNCIATION_XID)
                wordValue
                    .appendChild(doc.createTextNode(curWord.pronunciation))
                wordNode.appendChild(wordValue)
            } catch (e: Exception) {
                // Do nothing. Users are made aware of this issue elsewhere.
                // IOHandler.writeErrorLog(e);
            }
            wordValue = doc.createElement(PGUtil.WORD_DEFINITION_XID)
            wordValue.appendChild(doc.createTextNode(WebInterface.archiveHTML(curWord.definition)))
            wordNode.appendChild(wordValue)
            wordValue = doc.createElement(PGUtil.WORD_PRONUNCIATION_OVERRIDE_XID)
            wordValue.appendChild(doc.createTextNode(if (curWord.pronunciationOverride) PGUtil.TRUE else PGUtil.FALSE))
            wordNode.appendChild(wordValue)
            wordValue = doc.createElement(PGUtil.WORD_AUTO_DECLENSION_OVERRIDE_XID)
            wordValue.appendChild(doc.createTextNode(if (curWord.autoDeclensionOverride) PGUtil.TRUE else PGUtil.FALSE))
            wordNode.appendChild(wordValue)
            wordValue = doc.createElement(PGUtil.WORD_RULES_OVERRIDE_XID)
            wordValue.appendChild(doc.createTextNode(if (curWord.rulesOverride) PGUtil.TRUE else PGUtil.FALSE))
            wordNode.appendChild(wordValue)
            wordValue = doc.createElement(PGUtil.WORD_CLASS_COLLECTION_XID)
            for ((key, value) in curWord.getClassValues()) {
                val classVal: Element = doc.createElement(PGUtil.WORD_CLASS_AND_VALUE_XID)
                classVal.appendChild(doc.createTextNode(key.toString() + "," + value))
                wordValue.appendChild(classVal)
            }
            wordNode.appendChild(wordValue)
            wordValue = doc.createElement(PGUtil.WORD_CLASS_TEXT_VALUE_COLLECTION_XID)
            for ((key, value) in curWord.getClassTextValues()) {
                val classVal: Element = doc.createElement(PGUtil.WORD_CLASS_TEXT_VALUE_XID)
                classVal.appendChild(doc.createTextNode(key.toString() + "," + value))
                wordValue.appendChild(classVal)
            }
            wordNode.appendChild(wordValue)
            wordValue = doc.createElement(PGUtil.WORD_ETYMOLOGY_NOTES_XID)
            wordValue.appendChild(doc.createTextNode(curWord.etymologyNote))
            wordNode.appendChild(wordValue)
            lexicon.appendChild(wordNode)
        }
        rootElement.appendChild(lexicon)
    }

    /**
     * Call this to wipe out the values of all deprecated
     * conjugations/declensions for a particular part of speech in the
     * dictionary
     *
     * @param typeId ID of word type to clear values from
     */
    @RequiresApi(Build.VERSION_CODES.N)
    fun clearDeprecatedDeclensions(typeId: Int?) {
        val dm: DeclensionManager = core.getDeclensionManager()
        val comTypeDecs: MutableMap<Int, List<DeclensionPair>> =
            HashMap()

        // iterates over every word
        Predicate { word: ConWord ->
            word.typeId == typeId
        }.let {
            Consumer { node: ConWord ->
                val word = node
                val curDeclensions: List<DeclensionPair>

                // ensure I'm only generating declension patterns for any given part of speech only once
                if (comTypeDecs.containsKey(word.typeId)) {
                    curDeclensions = comTypeDecs[word.typeId]!!
                } else {
                    curDeclensions = dm.getAllCombinedIds(word.typeId)
                    comTypeDecs[word.typeId] = curDeclensions
                }

                // retrieves all stored declension values for word
                val decMap: MutableMap<String, DeclensionNode> =
                    dm.getWordDeclensions(word.id)

                // removes all legitimate declensions from map
                curDeclensions.forEach(Consumer { curPair: DeclensionPair ->
                    decMap.remove(
                        curPair.combinedId
                    )
                })

                // wipe remaining values from word
                dm.removeDeclensionValues(word.id, decMap.values)
            }.let { it1 ->
                nodeMap.values.stream()
                    .filter(it)
                    .forEach(it1)
            }
        }
    }

    /**
     * if a value is deleted from a class, this must be called. This cycles through all
     * words and eliminates instances where the given class/value combo appear
     * @param classId class from which value was deleted
     * @param valueId value deleted
     */
    fun classValueDeleted(classId: Int, valueId: Int) {
        for (value in nodeMap.values) {
            if (value.wordHasClassValue(classId, valueId)) {
                value.setClassValue(classId, -1)
            }
        }
    }

    fun notFoundNode(): Any {
        val notFound = ConWord()
        notFound.value = "WORD NOT FOUND"
        notFound.core = core
        notFound.definition = "WORD NOT FOUND"
        notFound.localWord = "WORD NOT FOUND"
        notFound.pronunciation = "WORD NOT FOUND"
        return notFound
    }

    /**
     * Gets a list of display word nodes. Set up specifically for display rather
     * than programmatic or logical consumption
     * @return
     */
    val wordNodesDisplay: List<ConWordDisplay>
        get() = toDisplayList(wordNodes)

    /**
     * Converts a list of words into a display list and reorders if appropriate
     * @param wordList List of words to convert to display list
     * @return
     */
    fun toDisplayList(wordList: ArrayList<ConWord?>): List<ConWordDisplay> {
        val ret: MutableList<ConWordDisplay> = ArrayList()
        for (conWord in wordList) {
            ret.add(ConWordDisplay(conWord, core))
        }
        if (core.getPropertiesManager().isUseLocalWordLex()) {
            ret.sort()
        }
        return ret
    }

    /**
     * Wrapper class of ConWord that allows for more display options in menus
     * Separated to eliminate possibility of display logic interfering with program logic
     */
    inner class ConWordDisplay(val conWord: ConWord?, private val core: DictCore) :
        Comparable<ConWordDisplay?> {

        override fun toString(): String {
            val ret: String = if (core.getPropertiesManager().isUseLocalWordLex()) {
                conWord!!.localWord
            } else {
                conWord.toString()
            }
            return if (ret.isEmpty()) " " else ret
        }

        /**
         * Respects language property to display/sort lexicon by local words
         * value set for this.
         * @param _compare
         * @return
         */
        override operator fun compareTo(_compare: ConWordDisplay): Int {
            val myLocalWord = conWord!!.localWord
            val compareLocalWord = _compare.conWord!!.localWord
            val ret: Int
            ret = if (core.getPropertiesManager().isUseLocalWordLex()) {
                if (core.getPropertiesManager().isIgnoreCase()) {
                    myLocalWord.toLowerCase().compareTo(compareLocalWord.toLowerCase())
                } else {
                    myLocalWord.compareTo(compareLocalWord)
                }
            } else {
                conWord.compareTo(_compare.conWord)
            }
            return ret
        }

        override fun equals(other: Any?): Boolean {
            var ret = other != null
            if (ret) {
                ret = other is ConWordDisplay
                if (ret) {
                    val compWord = (other as ConWordDisplay?)!!.conWord
                    ret = compWord?.id != null && conWord != null
                    if (ret && compWord != null) {
                        ret = conWord!!.id == compWord.id
                    }
                }
            }
            return ret
        }

        override fun hashCode(): Int {
            var hash = 3
            hash = 29 * hash + conWord.hashCode()
            return hash
        }

    }

    companion object {
        /**
         * Formats in HTML to a plain font to avoid conlang font
         *
         * @param toPlain text to make plain
         * @param core
         * @return text in plain tag
         */
        fun formatPlain(toPlain: String, core: DictCore): String {
            val defaultFont =
                "face=\"${core.getPropertiesManager().getFontLocal().getFamily().toString()}\""
            return "<font $defaultFont>$toPlain</font>"
        }

        /**
         * Formats in HTML to a conlang font
         *
         * @param toCon text to make confont
         * @param core
         * @return text in plain tag
         */
        fun formatCon(toCon: String, core: DictCore): String {
            val defaultFont =
                "face=\"" + core.getPropertiesManager().getFontCon().getFamily().toString() + "\""
            return "<font $defaultFont>$toCon</font>"
        }
    }

    init {
        bufferNode = ConWord()
        (bufferNode as ConWord).core = _core
        allConWords = HashMap()
        allLocalWords = HashMap()
        core = _core
    }

    override fun testWordLegality(n: ConWord?): ConWord {
        TODO("Not yet implemented")
    }

    override fun externalBalanceWordCounts(id: Int, _value: String, localWord: String) {
        TODO("Not yet implemented")
    }
}
