package com.polyglotandroid.core.nodes

import com.polyglotandroid.core.DictCore
import com.polyglotandroid.core.collections.ConWordCollection
import java.lang.ClassCastException
import java.lang.Exception

class ConWord(core: DictCore) : DictNode() {
    override var value: String = ""
        set(_value) {
            if (parentCollection != null)
                parentCollection.externalBalanceWordCounts(id, _value, localWord)
            super.value = _value // TODO: Add RTL handling later
        }
    var localWord: String = ""
    var typeId: Int = 0
    var definition: String = ""
    var pronunciation: String = ""
        @Throws(Exception::class)
        get() {
            var ret: String = field
            if (!pronunciationOverride && core != null) {
                val gen: String = core.PronunciationManager.getPronunciation(value)
                if (!gen.isEmpty())
                    ret = gen
            }
            return ret
        }
    var etymologyNote: String = ""
    var pronunciationOverride: Boolean = false
    var autoDeclensionOverride: Boolean = false
    var rulesOverride: Boolean = false
    // FIXME: Initiate these two properties properly
    private var core: DictCore? = null
        set(value) {
            if (value != null)
                parentCollection = value.wordCollection
            else
                parentCollection = null
            field = value
        }
    var parentCollection: ConWordCollection? = null
    private val classValues: HashMap<Int, Int> = hashMapOf()
    private val classTextValues: HashMap<Int, String> = hashMapOf()
    var filterEtyParent: Any? = Object()
    var typeError: String = ""


    fun isWordLegal(): Boolean {
        val checkValue: ConWord = parentCollection.testWordLegality(this)
        val checkPronunciation: String = try {
            checkValue.pronunciation
        } catch (e: Exception) {
            "Regex error: ${e.localizedMessage}"
        }
        return checkValue.value.isEmpty() &&
                checkValue.definition.isEmpty() &&
                checkValue.localWord.isEmpty() &&
                checkPronunciation.isEmpty() &&
                checkValue.typeError.isEmpty()
    }

    fun getClassTextValue(classId: Int): String? =
        if (classTextValues.containsKey(classId)) classTextValues[classId]
        else ""
    // TODO: setClassTextValue

    fun wordHasClassValue(classId: Int, valueId: Int): Boolean =
        classValues.containsKey(classId) && classValues[classId] == valueId

    fun setClassValue(classId: Int, valueId: Int) {
        classValues.remove(classId)
        if (valueId != -1) {
            classValues[classId] = valueId
        }
    }

    @Throws(ClassCastException::class)
    override fun setEqual(_node: DictNode) {
        if (_node !is ConWord)
            throw ClassCastException("Object not of type ConWord")

        val word: ConWord =_node
        // If core is initialized the wrong way, handle exception
        this.value = word.value
        this.localWord = word.localWord
        this.typeId = word.typeId
        this.definition = word.definition
        this.pronunciation = word.pronunciation
        this.id = word.id
    }
    override fun equals(other: Any?): Boolean {
        var ret = false
        if (this === other) {
            ret = true
        } else if (other != null && other is ConWord) {
            ret = value == other.value
            ret = ret && localWord == other.localWord
            ret = ret && typeId == other.typeId
            ret = ret && definition == other.definition // FIXME: WebInterface.archiveHTML: necessary?
            ret = ret && pronunciation == other.pronunciation
            ret = ret && etymologyNote == other.etymologyNote
            ret = ret && pronunciationOverride == other.pronunciationOverride
            ret = ret && autoDeclensionOverride == other.autoDeclensionOverride
            ret = ret && rulesOverride == other.rulesOverride
            ret = ret && classValues == other.classValues
            ret = ret && classTextValues == other.classTextValues
        }
        return ret
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + localWord.hashCode()
        result = 31 * result + typeId
        result = 31 * result + definition.hashCode()
        result = 31 * result + pronunciation.hashCode()
        result = 31 * result + etymologyNote.hashCode()
        result = 31 * result + pronunciationOverride.hashCode()
        result = 31 * result + autoDeclensionOverride.hashCode()
        result = 31 * result + rulesOverride.hashCode()
        result = 31 * result + core.hashCode()
        result = 31 * result + parentCollection.hashCode()
        result = 31 * result + classValues.hashCode()
        result = 31 * result + (filterEtyParent?.hashCode() ?: 0)
        result = 31 * result + typeError.hashCode()
        return result
    }

    // TODO: toString, handling RTL
    val wordTypeDisplay: String
    get() {
        var ret: String = "<TYPE NOT FOUND>"
        if (typeId != 0) {
            ret = try {
                core.getTypes().getNodeById(typeId).getValue() // FIXME: Implement these funcs
            } catch (e: Exception) {
                IOHandler.writeErrorLog(e) // FIXME: Implement this class
                typeId = 0
            }
        }
        return ret
    }
    // TODO: clean up code
}