package com.polyglotandroid.core.managers

import com.polyglotandroid.core.DictCore
import com.polyglotandroid.core.PGUtil
import com.polyglotandroid.core.nodes.PronunciationNode
import org.w3c.dom.Document
import org.w3c.dom.Element


class PronunciationManager(private val core: DictCore) {
    var isRecurse: Boolean = false
    var pronunciations = arrayListOf<PronunciationNode>()

    /**
     * Returns pronunciation of a given word
     *
     * @param base word to find pronunciation of
     * @return pronunciation string. If no perfect match found, empty string
     * returned
     * @throws java.lang.Exception on malformed regex statements encountered
     */
    @Throws(Exception::class)
    fun getPronunciation(base: String): String {
        val spaceDelimited: Array<String> =
            base.trim { it <= ' ' }.split(" ").toTypedArray()
        var ret: String = ""
        for (fragment: String in spaceDelimited) {
            ret += " " + getPronunciationInternal(fragment)
        }
        return ret.trim { it <= ' ' }
    }

    @Throws(Exception::class)
    fun getPronunciationInternal(base: String): String {
        var ret: String = ""

        // -base.length() fed as initial depth to ensure that longer words cannot be artificaially labeled as breaking max depth
        val pronunciationCycle: List<PronunciationNode> =
            getPronunciationElements(base, -base.length)
        for (currentPronunciation: PronunciationNode in pronunciationCycle) {
            ret += currentPronunciation.pronunciation
        }
        return ret
    }

    /**
     * Returns pronunciation elements of word
     *
     * @param base word to find pronunciation elements of
     * @return elements of pronunciation for word. Empty if no perfect match
     * found
     * @throws java.lang.Exception if malformed regex expression encountered
     */
    @Throws(Exception::class)
    fun getPronunciationElements(base: String): List<PronunciationNode> {
        // -base.length() fed as initial depth to ensure that longer words cannot be artificaially labeled as breaking max depth
        return getPronunciationElements(base, -base.length)
    }

    val toolLabel: String
        get() {
            return "Pronuncation Manager"
        }

    /**
     * returns pronunciation objects of a given word
     *
     * @param base word to find pronunciation objects of
     * @param isFirst set to true if first iteration.
     * @return pronunciation object list. If no perfect match found, empty
     * string returned
     */
    @Throws(Exception::class)
    private fun getPronunciationElements(
        base: String,
        depth: Int
    ): List<PronunciationNode> {
        val ret: MutableList<PronunciationNode> = ArrayList()
        val finder: Iterator<PronunciationNode> = getPronunciations().iterator()
        if (depth > PGUtil.maxProcRecursion) {
            throw Exception("Max recursions for $toolLabel exceeded.")
        }

        // return blank for empty string
        if (base.isEmpty() || !finder.hasNext()) {
            return ret
        }

        // split logic here to use recursion, string comparison, or regex matching
        if (isRecurse) {
            // when using recursion, only a single node can be returned, inherently.
            var retStr: String = base
            val retNode: PronunciationNode = PronunciationNode()
            while (finder.hasNext()) {
                val curNode: PronunciationNode = finder.next()
                retStr = retStr.replace(curNode.value.toRegex(), curNode.pronunciation)
            }
            retNode.pronunciation = retStr
            ret.add(retNode)
        } else if (core.getPropertiesManager().isDisableProcRegex()) {
            while (finder.hasNext()) {
                val curNode: PronunciationNode = finder.next()
                var pattern: String = curNode.value
                // do not overstep string
                if (pattern.length > base.length) {
                    continue
                }

                // capture string to compare based on pattern length
                var comp: String = base.substring(0, curNode.value.length)
                if (core.getPropertiesManager().isIgnoreCase()) {
                    comp = comp.toLowerCase()
                    pattern = pattern.toLowerCase()
                }
                if ((comp == pattern)) {
                    val temp: List<PronunciationNode> = getPronunciationElements(
                        base.substring(pattern.length, base.length),
                        depth + 1
                    )

                    // if lengths are equal, success! return. If unequal and no further match found-failure
                    if (pattern.length == base.length || !temp.isEmpty()) {
                        ret.add(curNode)
                        ret.addAll(temp)
                        break
                    }
                }
            }
        } else {
            while (finder.hasNext()) {
                val curNode: PronunciationNode = finder.next()
                var pattern: String = curNode.value
                // skip if set as starting characters, but later in word
                if (pattern.startsWith("^") && depth != 0) {
                    continue
                }

                // original pattern
                val origPattern: String = pattern

                // make pattern a starting pattern if not already, if it is already, allow it to accept following strings
                if (!pattern.startsWith("^")) {
                    pattern = "^($pattern).*"
                } else {
                    pattern = "^(" + pattern.substring(1) + ").*"
                }
                val findString: Pattern = Pattern.compile(pattern)
                val matcher: Matcher = findString.matcher(base)
                if (matcher.matches()) {
                    val leadingChars: String = matcher.group(1)

                    // if a user has entered an empty pattern... just continue.
                    if (leadingChars.isEmpty()) {
                        continue
                    }
                    val temp: List<PronunciationNode> = getPronunciationElements(
                        base.substring(leadingChars.length, base.length),
                        depth + 1
                    )
                    try {
                        if (leadingChars.length == base.length || !temp.isEmpty()) {
                            val finalNode: PronunciationNode = PronunciationNode()
                            finalNode.setEqual(curNode)
                            finalNode.pronunciation =
                                leadingChars.replace(origPattern.toRegex(), curNode.pronunciation)
                            ret.add(finalNode)
                            ret.addAll(temp)
                            break
                        }
                    } catch (e: IndexOutOfBoundsException) {
                        throw Exception(
                            ("The pronunciation pair " + curNode.value + "->"
                                    + curNode.pronunciation + " is generating a regex error. Please correct."
                                    + "\nError: " + e.localizedMessage + e.javaClass.name)
                        )
                    }
                }
            }
        }
        return ret
    }

    /**
     * Writes all pronunciation information to XML document
     *
     * @param doc Document to write to
     * @param rootElement root element of document
     */
    fun writeXML(doc: Document, rootElement: Element) {
        val collection: Element = doc.createElement(PGUtil.ETYMOLOGY_COLLECTION_XID)
        rootElement.appendChild(collection)
        val recurseNode: Element = doc.createElement(PGUtil.PRONUNCIATION_GUIDE_RECURSIVE_XID)
        recurseNode.appendChild(doc.createTextNode(if (isRecurse) PGUtil.TRUE else PGUtil.FALSE))
        collection.appendChild(recurseNode)
        for (pronunciation in this.pronunciations) {
            pronunciation.writeXML(doc, collection)
        }
    }

    /**
     * Returns true if there are any pronunciation rules defined
     * @return
     */
    val isInUse: Boolean
        get() {
            return !pronunciations.isEmpty()
        }

}
