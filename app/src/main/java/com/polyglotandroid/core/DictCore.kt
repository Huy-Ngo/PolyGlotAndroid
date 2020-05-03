package com.polyglotandroid.core

import com.polyglotandroid.core.collections.ConWordCollection
import com.polyglotandroid.core.collections.PartOfSpeechCollection
import com.polyglotandroid.core.collections.WordClassCollection
import com.polyglotandroid.core.customControls.AlphaMap
import com.polyglotandroid.core.managers.*
import java.awt.Desktop
import java.awt.FontFormatException
import java.io.IOException
import java.time.Instant
import javax.swing.UIDefaults


/**
 * This is the core of PolyGlot. It manages the top level of all aspects of the program.
 */
class DictCore (_polyGlot: PolyGlot) {
    var polyGlot: PolyGlot? = null
    var wordCollection: ConWordCollection? = null
    var partOfSpeechCollection: PartOfSpeechCollection? = null
    var declensionManager: DeclensionManager? = null
    var propertiesManager: PropertiesManager? = null
    var pronunciationManager: PronunciationManager? = null
    var romMgr: RomanizationManager? = null
    var famManager: FamilyManager? = null

    //    var logoCollection: LogoCollection? = null
    var grammarManager: GrammarManager? = null
    var wordClassCollection: WordClassCollection? = null

    //    var imageCollection: ImageCollection? = null
    var etymologyManager: EtymologyManager? = null
    var reversionManager: ReversionManager? = null

    //    var toDoManager: ToDoManager? = null
//    var rootWindow: ScrMainMenu? = null
    var curLoading = false
    var lastSaveTime = Instant.MIN
    var curFileName = ""
    var isLanguageEmpty: Boolean = false
        get() = wordCollection.isEmpty()
                && partOfSpeechCollection.isEmpty()
                && declensionManager.isEmpty()
                && pronunciationManager.isEmpty()
                && romMgr.isEmpty()
                // && logoCollection.isEmpty()
                && grammarManager.isEmpty()
                && wordClassCollection.isEmpty()
    // && imageCollection.isEmpty()


    init {
        try {
            wordCollection = ConWordCollection(this)
            partOfSpeechCollection = PartOfSpeechCollection(this)
            declensionManager = DeclensionManager(this)
            propertiesManager = PropertiesManager(this)
            pronunciationManager = PronunciationManager(this)
            romMgr = RomanizationManager(this)
            famManager = FamilyManager(this)
//            logoCollection = LogoCollection(this)
            grammarManager = GrammarManager()
            wordClassCollection = WordClassCollection(this)
//            imageCollection = ImageCollection()
            etymologyManager = EtymologyManager(this)
            reversionManager = ReversionManager(this)
//            toDoManager = ToDoManager()
            var alphaOrder: AlphaMap<String, Int> = propertiesManager.alphaOrder
            wordCollection!!.setAlphaOrder(alphaOrder)
//            logoCollection.setAlphaOrder(alphaOrder)
//            rootWindow = null
//            PGUtil.validateVersion()
        } catch (e: Exception) {
            IOHandler.writeErrorLog(e)
            InfoBox.error(
                "CORE ERROR",
                "Error creating language core: " + e.localizedMessage,
                null
            )
        }
    }

    /**
     * Reloads main menu (force refresh of visual elements)
     */
    fun refreshMainMenu() {
        polyGlot.refreshUiDefaults()
        rootWindow.dispose(false)
        rootWindow = ScrMainMenu(this)
        rootWindow.setVisible(true)
        rootWindow.selectFirstAvailableButton()
    }

    fun getUiDefaults(): UIDefaults? {
        return polyGlot!!.getUiDefaults()
    }

    /**
     * Gets conlang name or CONLANG. Put on core because it's used a lot.
     *
     * @return either name of conlang or "Conlang"
     */
    fun conLabel(): String? {
        return if (propertiesManager!!.langName.isEmpty()) "Conlang" else propertiesManager!!.langName
    }

    /**
     * Gets options manager
     * @return
     */
    fun getOptionsManager(): OptionsManager? {
        return polyGlot!!.optionsManager
    }

    /**
     * Clipboard can be used to hold any object
     *
     * @param c object to hold
     */
    fun setClipBoard(c: Any?) {
        polyGlot!!.clipBoard = c
    }

    /**
     * Retrieves object held in clipboard, even if null, regardless of type
     *
     * @return contents of clipboard
     */
    fun getClipBoard(): Any? {
        return polyGlot!!.clipBoard
    }

    /**
     * Pushes save signal to main interface menu
     */
    fun coreOpen() {
        rootWindow.open()
    }

    /**
     * Pushes save signal to main interface menu
     *
     * @param performTest whether to prompt user to save
     */
    fun coreNew(performTest: Boolean) {
        rootWindow.newFile(performTest)
    }

    /**
     * Pushes signal to all forms to update their values from the core. Cascades
     * through windows and their children.
     */
    fun pushUpdate() {
        pushUpdateWithCore(this)
    }

    /**
     * Pushes signal to all forms to update their values from the core. Cascades
     * through windows and their children.
     * @param _core new core to push
     */
    fun pushUpdateWithCore(_core: DictCore?) {
        val stack =
            Thread.currentThread().stackTrace

        // prevent recursion (exclude check of top method, obviously)
        for (i in stack.size - 1 downTo 2) {
            val element = stack[i]
            if (element.methodName == "pushUpdateWithCore") {
                return
            }
        }

        // null root window indicates that this is a virtual dict core used for library analysis
        if (rootWindow != null) {
            rootWindow.updateAllValues(_core)
        }
    }

    /**
     * Returns root window of PolyGlot
     *
     * @return
     */
    fun getRootWindow(): ScrMainMenu? {
        return rootWindow
    }

    /**
     * Builds a report on the conlang. Potentially very computationally
     * expensive.
     */
    fun buildLanguageReport() {
        TODO("Not implemented yet")
    }

    /**
     * Reads from given file
     *
     * @param _fileName filename to read from
     * @param overrideXML override to where the XML should be loaded from
     * @throws java.io.IOException for unrecoverable errors
     * @throws IllegalStateException for recoverable errors
     */
    @Throws(IOException::class, IllegalStateException::class)
    fun readFile(_fileName: String, overrideXML: ByteArray? = null) {
        curLoading = true
        curFileName = _fileName
        var errorLog = ""
        var warningLog = ""

        // test file exists
        if (!IOHandler.fileExists(_fileName)) {
            throw IOException("File $_fileName does not exist.")
        }

        // inform user if file is not an archive
        if (!IOHandler.isFileZipArchive(_fileName)) {
            throw IOException("File $_fileName is not a valid PolyGlot archive.")
        }

        // load image assets first to allow referencing as dictionary loads
        try {
            IOHandler.loadImageAssets(imageCollection, _fileName)
        } catch (e: java.lang.Exception) {
            throw IOException("Image loading error: " + e.localizedMessage, e)
        }
        try {
            PFontHandler.setFontFrom(_fileName, this)
        } catch (e: IOException) {
            IOHandler.writeErrorLog(e)
            warningLog += e.getLocalizedMessage().toString() + "\n"
        } catch (e: FontFormatException) {
            IOHandler.writeErrorLog(e)
            warningLog += e.getLocalizedMessage().toString() + "\n"
        }
        try {
            val handler: CustHandler
            // if override XML value, load from that, otherwise pull from file
            if (overrideXML == null) {
                handler = IOHandler.getHandlerFromFile(_fileName, this)
                IOHandler.parseHandler(_fileName, handler)
            } else {
                handler = IOHandler.getHandlerFromByteArray(overrideXML, this)
                IOHandler.parseHandlerByteArray(overrideXML, handler)
            }
            errorLog += handler.errorLog
            warningLog += handler.warningLog
        } catch (e: ParserConfigurationException) {
            throw IOException(e.getMessage(), e)
        } catch (e: SAXException) {
            throw IOException(e.getMessage(), e)
        } catch (e: IOException) {
            throw IOException(e.getMessage(), e)
        }
        try {
            IOHandler.loadGrammarSounds(_fileName, grammarManager)
        } catch (e: java.lang.Exception) {
            IOHandler.writeErrorLog(e)
            warningLog += """
                ${e.localizedMessage}
                
                """.trimIndent()
        }
        try {
            logoCollection.loadRadicalRelations()
        } catch (e: java.lang.Exception) {
            IOHandler.writeErrorLog(e)
            warningLog += """
                ${e.localizedMessage}
                
                """.trimIndent()
        }
        try {
            IOHandler.loadLogographs(logoCollection, _fileName)
        } catch (e: java.lang.Exception) {
            IOHandler.writeErrorLog(e)
            warningLog += """
                ${e.localizedMessage}
                
                """.trimIndent()
        }
        try {
            IOHandler.loadReversionStates(reversionManager, _fileName)
        } catch (e: IOException) {
            IOHandler.writeErrorLog(e)
            warningLog += e.getLocalizedMessage().toString() + "\n"
        }
        curLoading = false
        if (!errorLog.trim { it <= ' ' }.isEmpty()) {
            throw IOException(errorLog)
        }
        check(warningLog.trim { it <= ' ' }.isEmpty()) { warningLog }

        // do not run in headless environments...
        if (rootWindow != null) {
            refreshMainMenu()
        }
    }

    /**
     * loads revision XML from revision byte array (does not support media revisions)
     * @param revision
     * @param fileName
     * @throws java.io.IOException
     */
    @Throws(IOException::class)
    fun revertToState(revision: ByteArray?, fileName: String?) {
        val revDict = DictCore(polyGlot!!)
        revDict.setRootWindow(rootWindow)
        revDict.readFile(fileName!!, revision)
        pushUpdateWithCore(revDict)
    }

    /**
     * Used for test loading reversion XMLs. Cannot successfully load actual revision into functioning DictCore
     * @param reversion
     * @return
     */
    fun testLoadReversion(reversion: ByteArray?): String? {
        val errorLog: String
        errorLog = try {
            val handler: CustHandler = IOHandler.getHandlerFromByteArray(reversion, this)
            IOHandler.parseHandlerByteArray(reversion, handler)
            handler.errorLog
            // errorLog += handler.getWarningLog(); // warnings may be disregarded here
        } catch (e: IOException) {
            IOHandler.writeErrorLog(e)
            e.localizedMessage
        } catch (e: ParserConfigurationException) {
            IOHandler.writeErrorLog(e)
            e.getLocalizedMessage()
        } catch (e: SAXException) {
            IOHandler.writeErrorLog(e)
            e.getLocalizedMessage()
        }

        // if no save time present, simply timestamp for current time (only relevant for first time revision log added)
        if (lastSaveTime === Instant.MIN) {
            lastSaveTime = Instant.now()
        }
        return errorLog
    }

    /**
     * Writes to given file
     *
     * @param _fileName filename to write to
     * @throws javax.xml.parsers.ParserConfigurationException
     * @throws javax.xml.transform.TransformerException
     * @throws java.io.FileNotFoundException
     */
    fun writeFile(_fileName: String) {
        TODO()
    }

    override fun equals(other: Any?): Boolean =
        if (other is DictCore)
            wordCollection == other.wordCollection &&
                    partOfSpeechCollection == other.partOfSpeechCollection &&
                    declensionManager == other.declensionManager &&
                    propertiesManager == other.propertiesManager &&
                    pronunciationManager == other.pronunciationManager &&
                    romMgr == other.romMgr &&
                    famManager == other.famManager &&
                    grammarManager == other.grammarManager &&
                    wordClassCollection == other.wordClassCollection &&
                    etymologyManager == other.etymologyManager
        else false

    override fun hashCode(): Int {
        var result = polyGlot?.hashCode() ?: 0
        result = 31 * result + (wordCollection?.hashCode() ?: 0)
        result = 31 * result + (partOfSpeechCollection?.hashCode() ?: 0)
        result = 31 * result + (declensionManager?.hashCode() ?: 0)
        result = 31 * result + (propertiesManager?.hashCode() ?: 0)
        result = 31 * result + (pronunciationManager?.hashCode() ?: 0)
        result = 31 * result + (romMgr?.hashCode() ?: 0)
        result = 31 * result + (famManager?.hashCode() ?: 0)
        result = 31 * result + (grammarManager?.hashCode() ?: 0)
        result = 31 * result + (wordClassCollection?.hashCode() ?: 0)
        result = 31 * result + (etymologyManager?.hashCode() ?: 0)
        result = 31 * result + (reversionManager?.hashCode() ?: 0)
        result = 31 * result + curLoading.hashCode()
        result = 31 * result + (lastSaveTime?.hashCode() ?: 0)
        result = 31 * result + curFileName.hashCode()
        return result
    }
}