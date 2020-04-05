package com.polyglotandroid.core.nodes

import android.os.Build
import androidx.annotation.RequiresApi

class DeclensionGenerationRule(typeId: Int = -1, combinationId: String = "")
    : Comparable<DeclensionGenerationRule> {
    var typeId = 0
    val index = -1
    var combinationId: String? = null
    var regex = ""
    var name = ""
    val transformations: ArrayList<DeclensionGenerationTransform> = ArrayList()
    val applyToClasses: Map<Int, Int> = HashMap()
    var transformBuffer: DeclensionGenerationTransform = DeclensionGenerationTransform()
    private var debugString = ""

    /**
     * Inserts current transform buffer, then sets to blank
     */
    fun insertTransformBuffer() {
        addTransform(transformBuffer)
        transformBuffer = DeclensionGenerationTransform()
    }

    /**
     * Gets all entry pairs for classes/class values this rule applies to
     * @return
     */
    fun getApplicableClasses(): Array<Map.Entry<Int, Int>?> {
        val classValues: Array<Map.Entry<Int, Int>> = applyToClasses.entries.toTypedArray()
        val ret: Array<Map.Entry<Int, Int>?> =
            arrayOfNulls(classValues.size)
        for (i in classValues.indices) {
            ret[i] = classValues[i]
        }
        return ret
    }

    /**
     * Set declension generation rules to passed value, copying sub-nodes, copy from [rule].
     * If [setTypeAndCombination] is set to true, copy the `typeId` and `combinationId` from
     * the original, if set to false, skip the value.
     */
    @RequiresApi(Build.VERSION_CODES.N)
    fun setEqual(rule: DeclensionGenerationRule, setTypeAndCombination: Boolean) {
        if (setTypeAndCombination) {
            typeId = rule.typeId
            combinationId = rule.combinationId
        }
        name = rule.name
        regex = rule.regex
        transformations.clear()
        rule.transformations.stream().map { copyFrom ->
            val copyTo = DeclensionGenerationTransform()
            copyTo.setEqual(copyFrom)
            copyTo
        }.forEachOrdered { copyTo -> transformations.add(copyTo) }
        for ((key, value) in rule.applyToClasses.entrySet()) {
            this.addClassToFilterList(key, value)
        }
    }

    /***
     * Checks if [other] is equal to this rule. The combination ID is NOT accounted for,
     * as this is used when checking equality across rules set to different
     * declensions. ID is also not accounted for, as this is an identity value.
     * It also does not apply to the applyToClasses values, as this is similar
     * to the first requirement.
     */
    fun valuesEqual(other: Any): Boolean  =
        if (this !== other) {
            if (other is DeclensionGenerationRule)
                typeId == other.typeId &&
                        regex == other.regex &&
                        name == other.name &&
                        transformations == other.transformations
            else
                false
        } else true

    /**
     * Adds [transformation] to rule
     */
    fun addTransform(transformation: DeclensionGenerationTransform) {
        transformations.add(transformation)
    }

    override fun compareTo(other: DeclensionGenerationRule): Int {
        val BEFORE = -1
        val EQUAL = 0
        val AFTER = 1
        val compIndex: Int = other.index
        val ret: Int
        ret = when {
            index > compIndex -> AFTER
            index == compIndex -> EQUAL
            else -> BEFORE
        }
        return ret
    }

    fun doesRuleApplyToClassValue(
        classId: Int,
        valueId: Int,
        overrideDefault: Boolean
    ): Boolean {
        var ret = false

        // if test for universal inclusion (-1 == include all classes and values)
        if (classId != -1 && applyToClasses.containsKey(-1) && !overrideDefault) {
            ret = true
        } else if (applyToClasses.containsKey(classId)) {
            ret = applyToClasses[classId] == valueId
        }
        return ret
    }

    fun doesRuleApplyToWord(word: ConWord): Boolean {
        if (word.core == null) {
            throw NullPointerException("Words without populated dictionary cores cannot be tested.")
        }
        var ret = false
        val wordTypeHasClasses = word.core.wordClassCollection
            .getClassesForType(word.typeId).length !== 0
        val wordTypeId: Int = word.typeId
        debugString = "Rule: $name\n"

        // if -1 present in this rule, apply to all. Otherwise test against word classes. Skips mismatching PoS
        if (typeId == wordTypeId && (!wordTypeHasClasses || applyToClasses.containsKey(-1))) {
            ret = true
        } else if (typeId == wordTypeId) {
            ret = true

            // if a word does not match all of the entries in the required classes, reject
            for ((classId, value) in applyToClasses) {
                if (!word.wordHasClassValue(classId, value)) {
                    debugString += "    Word's class does not match filter values for rule. Rule will not be applied.\n"
                    ret = false
                    break
                }
            }
        } else {
            debugString += (("    Rule PoS "
                    + word.core.types.getNodeById(typeId).getValue()
                    ) + " does not match word PoS "
                    + word.core.types.getNodeById(wordTypeId).getValue()
                .toString() + "\n")
        }

        // test word against regex
        if (ret && word.value.matches(regex)) {
            debugString += """    value: ${word.value} matches regex: "$regex". Rule will be applied."""
        } else if (ret) {
            debugString += """    value: ${word.value} does not match regex: "$regex". Rule will not be applied."""
            ret = false
        }
        return ret
    }

}