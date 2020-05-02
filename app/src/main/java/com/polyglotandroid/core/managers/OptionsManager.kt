package com.polyglotandroid.core.managers

import com.polyglotandroid.core.DictCore
import com.polyglotandroid.core.IOHandler
import java.util.*
import javax.swing.UIManager
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


/**
 * This contains, loads and saves the options for PolyGlot
 * @UI_handler
 */
class OptionsManager(private var core: DictCore) {
    /**
     * @return the animateWindows
     */
    /**
     * @param animateWindows the animateWindows to set
     */
    var isAnimateWindows = false
    var isNightMode = false
    private val lastFiles: MutableList<String> = ArrayList()
    private val screenPos: MutableMap<String, Point> = HashMap()
    private val screenSize: MutableMap<String, Dimension> = HashMap()
    private val screensUp: MutableList<String> = ArrayList()
    var menuFontSize = 0.0
        get() = if (field == 0.0) PGTUtil.defaultFontSize else field
        set(_size) {
            field = _size
            setDefaultJavaFontSize(_size)
        }
    var maxReversionCount: Int = PGTUtil.defaultMaxRollbackVersions
        set(maxRollbackVersions) {
            field = maxRollbackVersions
            core.reversionManager.trimReversions()
        }
    var toDoBarPosition = -1

    private fun setDefaultJavaFontSize(size: Double) {
        val keys: Enumeration<*> = UIManager.getDefaults().keys()
        while (keys.hasMoreElements()) {
            val key = keys.nextElement()
            val newFont: Font = UIManager.getFont(key)
            if (newFont != null) {
                UIManager.put(key, newFont.deriveFont(size.toFloat()))
            }
        }
    }

    /**
     * returns map of all screen positions
     * @return actual map object (modifying WILL change persistent values)
     */
    val screenPositions: Map<String, Any>
        get() = screenPos

    /**
     * returns map of all screen sizes
     * @return actual map object (modifying WILL change persistent values)
     */
    val screenSizes: Map<String, Any>
        get() = screenSize

    /**
     * Records screen up at time of program closing
     * @param screen name of screen to be recorded as being up
     */
    fun addScreenUp(screen: String) {
        if (!screen.isEmpty() && !screensUp.contains(screen)) {
            screensUp.add(screen)
        }
    }

    /**
     * Retrieve screens up at time of last close
     * @return list of screens up
     */
    val lastScreensUp: List<String>
        get() = screensUp

    /**
     * Adds or replaces screen position of a window
     * @param screen name of window
     * @param position position of window
     */
    fun setScreenPosition(screen: String, position: Point) {
        if (screenPos.containsKey(screen)) {
            screenPos.replace(screen, position)
        } else {
            screenPos[screen] = position
        }
    }

    /**
     * Adds or replaces screen size of a window
     * @param screen name of window
     * @param dimension size of window
     */
    fun setScreenSize(screen: String, dimension: Dimension) {
        if (screenSize.containsKey(screen)) {
            screenSize.replace(screen, dimension)
        } else {
            screenSize[screen] = dimension
        }
    }

    /**
     * Retrieves last screen position of screen
     * @param screen screen to return position for
     * @return last position of screen. Null otherwise.
     */
    fun getScreenPosition(screen: String): Point? {
        var ret: Point? = null
        if (screenPos.containsKey(screen)) {
            ret = screenPos[screen]
        }
        return ret
    }

    /**
     * Retrieves last screen size of screen
     * @param screen screen to return size for
     * @return last size of screen (stored in
     * a Point). Null otherwise.
     */
    fun getScreenSize(screen: String): Dimension? {
        var ret: Dimension? = null
        if (screenSize.containsKey(screen)) {
            ret = screenSize[screen]
        }
        return ret
    }

    /**
     * Retrieves list of last opened files for PolyGlot
     *
     * @return
     */
    fun getLastFiles(): List<String> {
        return lastFiles
    }

    /**
     * Pushes a recently opened file (if appropriate) into the recent files list
     *
     * @param file full path of file
     */
    fun pushRecentFile(file: String) {
        if (!lastFiles.isEmpty()
            && lastFiles.contains(file)
        ) {
            lastFiles.remove(file)
            lastFiles.add(file)
            return
        }
        while (lastFiles.size > PGTUtil.optionsNumLastFiles) {
            lastFiles.removeAt(0)
        }
        lastFiles.add(file)
    }

    /**
     * Loads all option data from ini file, if none, ignore. One will be created
     * on exit.
     *
     * @throws IOException on failure to open existing file
     */
    @Throws(Exception::class)
    fun loadIni() {
        IOHandler.loadOptionsIni(core)
    }

    fun setCore(_core: DictCore) {
        core = _core
    }

}