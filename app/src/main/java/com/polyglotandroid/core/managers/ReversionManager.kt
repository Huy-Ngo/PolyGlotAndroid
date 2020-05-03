package com.polyglotandroid.core.managers

import com.polyglotandroid.core.DictCore
import com.polyglotandroid.core.nodes.ReversionNode
import java.time.Instant
import kotlin.collections.ArrayList


/**
 * This keeps track of reversion versions of a language and handles their interaction/rollbacks with the larger
 * system.
 * @author DThompson
 */
class ReversionManager(private val core: DictCore) {
    private var reversionList: MutableList<ReversionNode> = ArrayList()

    /**
     * Adds a version to the beginning of the list. Truncates if versions greater than max value set in options
     * Max versions set to 0 means no limit to backup saves
     * @param addVersion byte array of raw XML of language file
     * @param saveTime The time at which this was saved
     */
    fun addVersion(addVersion: ByteArray?, saveTime: Instant?) {
        val reversion = saveTime?.let { ReversionNode(addVersion!!, it) }
        if (reversion != null) {
            reversionList.add(0, reversion)
        }
        trimReversions()
    }

    /**
     * Adds a version to the end of the list. (used when loading from file)
     * @param addVersion byte array of raw XML of language file
     */
    fun addVersionToEnd(addVersion: ByteArray?) {
        val reg = ReversionNode(addVersion!!)
        reversionList.add(reg)
    }

    fun getReversionList(): Array<ReversionNode> {
        reversionList.sort()
        return reversionList.toTypedArray()
    }

    val maxReversionsCount: Int
        get() = core.optionsManager.getMaxReversionCount()

    /**
     * Trims reversions down to the max number allowed in the options
     */
    fun trimReversions() {
        val maxVersions: Int = core.optionsManager.getMaxReversionCount()
        if (reversionList.size > maxVersions && maxVersions != 0) {
            reversionList = reversionList.subList(0, maxVersions)
        }
    }

}