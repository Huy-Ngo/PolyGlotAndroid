package com.polyglotandroid.core

import com.polyglotandroid.core.managers.OptionsManager
import java.awt.Desktop
import javax.swing.InputMap
import javax.swing.JFileChooser
import javax.swing.JOptionPane
import javax.swing.KeyStroke
import javax.swing.SwingUtilities
import javax.swing.UIDefaults
import javax.swing.UIManager
import javax.swing.filechooser.FileNameExtensionFilter
import javax.swing.text.DefaultEditorKit


/**
 * Starts up PolyGlot and does testing for OS/platform that would be inappropriate elsewhere
 * @UI_handler
 */
class PolyGlot(val overrideProgramPath: String) {
    val optionsManager: OptionsManager = OptionsManager()
    /**
     * Retrieves object held in clipboard, even if null, regardless of type
     *
     * @return contents of clipboard
     */
    /**
     * Clipboard can be used to hold any object
     *
     * @param c object to hold
     */
    var clipBoard: Any? = null
    private var uiDefaults: UIDefaults? = null
    fun getNewCore(rootWindow: ScrMainMenu?): DictCore {
        val ret: DictCore = DictCore(this)
        ret.setRootWindow(rootWindow)
        return ret
    }

    /**
     * Retrieves working directory of PolyGlot
     *
     * @return current working directory
     */
    val workingDirectory: File
        get() = if (overrideProgramPath.isEmpty()) PGTUtil.getDefaultDirectory() else File(
            overrideProgramPath
        )

    @Throws(IOException::class)
    fun saveOptionsIni() {
        IOHandler.writeOptionsIni(workingDirectory.getAbsolutePath(), optionsManager)
    }

    fun getUiDefaults(): UIDefaults? {
        return uiDefaults
    }

    fun refreshUiDefaults() {
        uiDefaults = VisualStyleManager.generateUIOverrides(optionsManager.isNightMode)
    }

    companion object {
        /**
         * @param args the command line arguments: open file path (blank if none), in chunks if spaces in path
         */
        @JvmStatic
        fun main(args: Array<String>) {
            try {
                // FIXME: No need to check OS since we know it's running on Android
                if (PGTUtil.IS_OSX) {
                    // set program icon
                    Taskbar.getTaskbar().setIconImage(PGTUtil.POLYGLOT_ICON.getImage())
                }
                System.setProperty("apple.laf.useScreenMenuBar", "true")
                System.setProperty("apple.awt.application.name", PGTUtil.DISPLAY_NAME)
                System.setProperty(
                    "com.apple.mrj.application.apple.menu.about.name",
                    PGTUtil.DISPLAY_NAME
                )
                setupNimbus()
                setupCustomUI()
                conditionalBetaSetup()
                setupOSSpecificCutCopyPaste()
                java.awt.EventQueue.invokeLater(Runnable {

                    // catch all top level application killing throwables (and bubble up directly to ensure reasonable behavior)
                    try {
                        var s: ScrMainMenu? = null
                        if (canStart()) {
                            try {
                                // separated due to serious nature of Throwable vs Exception
                                val polyGlot: PolyGlot = PolyGlot("")
                                val core: DictCore = DictCore(polyGlot)
                                try {
                                    IOHandler.loadOptionsIni(
                                        polyGlot.optionsManager,
                                        polyGlot.workingDirectory.getAbsolutePath()
                                    )
                                } catch (ex: Exception) {
                                    IOHandler.writeErrorLog(ex)
                                    InfoBox.error(
                                        "Options Load Error",
                                        ("Unable to load options file or file corrupted:\n"
                                                + ex.getLocalizedMessage()),
                                        core.getRootWindow()
                                    )
                                    IOHandler.deleteIni(
                                        polyGlot.workingDirectory.getAbsolutePath()
                                    )
                                }
                                s = ScrMainMenu(core)
                                s.checkForUpdates(false)
                                s.setVisible(true)

                                // runs additional integration if on OSX system
                                if (PGTUtil.IS_OSX) {
                                    val desk: Desktop = Desktop.getDesktop()
                                    val staticScr: ScrMainMenu? = s
                                    desk.setQuitHandler({ e: QuitEvent?, response: QuitResponse? -> staticScr.dispose() })
                                    desk.setPreferencesHandler({ e: PreferencesEvent? -> staticScr.showOptions() })
                                    desk.setAboutHandler({ e: AboutEvent? ->
                                        ScrAbout.run(
                                            DictCore(
                                                polyGlot
                                            )
                                        )
                                    })
                                    desk.setPrintFileHandler({ e: PrintFilesEvent? -> staticScr.printToPdf() })
                                }

                                // if a recovery file exists, query user for action
                                var recovery: File? =
                                    IOHandler.getTempSaveFileIfExists(polyGlot.workingDirectory)
                                if (recovery != null) {
                                    if (InfoBox.yesNoCancel(
                                            "Recovery File Detected",
                                            "PolyGlot appears to have shut down mid save. Would you like to recover the file?",
                                            s
                                        ) === JOptionPane.YES_OPTION
                                    ) {
                                        val chooser: JFileChooser = JFileChooser()
                                        chooser.setDialogTitle("Recover Dictionary To")
                                        val filter: FileNameExtensionFilter =
                                            FileNameExtensionFilter("PolyGlot Dictionaries", "pgd")
                                        chooser.setFileFilter(filter)
                                        chooser.setApproveButtonText("Recover")
                                        chooser.setCurrentDirectory(polyGlot.workingDirectory)
                                        var fileName: String
                                        if (chooser.showOpenDialog(s) == JFileChooser.APPROVE_OPTION) {
                                            fileName = chooser.getSelectedFile().getAbsolutePath()
                                            if (!fileName.toLowerCase()
                                                    .endsWith(PGTUtil.POLYGLOT_FILE_SUFFIX)
                                            ) {
                                                fileName += "." + PGTUtil.POLYGLOT_FILE_SUFFIX
                                            }
                                            val copyTo: File = File(fileName)
                                            try {
                                                IOHandler.copyFile(
                                                    recovery.toPath(),
                                                    copyTo.toPath(),
                                                    true
                                                )
                                                if (copyTo.exists()) {
                                                    s.setFile(copyTo.getAbsolutePath())
                                                    s.openLexicon(true)
                                                    recovery.delete()
                                                    InfoBox.info(
                                                        "Success!",
                                                        "Language successfully recovered!",
                                                        s
                                                    )
                                                } else {
                                                    throw IOException("File not copied.")
                                                }
                                            } catch (e: IOException) {
                                                InfoBox.error(
                                                    "Recovery Problem",
                                                    (("Unable to recover file due to: "
                                                            + e.getLocalizedMessage()
                                                            ).toString() + ". Recovery file exists at location: "
                                                            + recovery.toPath()
                                                        .toString() + ". To attempt manual recovery, add .pgd suffix to file name and open with PolyGlot by hand."),
                                                    s
                                                )
                                            }
                                        } else {
                                            InfoBox.info(
                                                "Recovery Cancelled",
                                                "Recovery Cancelled. Restart PolyGlot to be prompted again.",
                                                s
                                            )
                                        }
                                    } else {
                                        if (InfoBox.yesNoCancel(
                                                "Delete Recovery File",
                                                "Delete the recovery file, then?",
                                                s
                                            ) === JOptionPane.YES_OPTION
                                        ) {
                                            recovery.delete()
                                        }
                                        recovery = null
                                    }
                                }

                                // open file if one is provided via arguments (but only if no recovery file- that takes precedence)
                                if (args.size > 0 && recovery == null) {
                                    var filePath: String? = ""

                                    // file paths with spaces in their names are broken into multiple arguments. This is a best guess. (multiple spaces could exist)
                                    // TODO: Remove once this is fixed in Java
                                    for (pathChunk: String in args) {
                                        filePath += " " + pathChunk
                                    }

                                    // arguments passed in by the OS choke on special charaters as of Java 14 release (jpackage issue, probably)
                                    // TODO: Remove once this is fixed in Java
                                    if (File(filePath).exists()) {
                                        s.setFile(filePath)
                                    } else {
                                        InfoBox.warning(
                                            "File Path Error",
                                            "Please retry opening this file by clicking File->Open from the menu.",
                                            null
                                        )
                                    }
                                    s.openLexicon(true)
                                }
                            } catch (e: ArrayIndexOutOfBoundsException) {
                                IOHandler.writeErrorLog(
                                    e,
                                    "Problem with top level PolyGlot arguments."
                                )
                                InfoBox.error(
                                    "Unable to start", ("Unable to open PolyGlot main frame: \n"
                                            + e.message + "\n"
                                            + "Problem with top level PolyGlot arguments."), null
                                )
                            } catch (e: Exception) { // split up for logical clarity... might want to differentiate
                                IOHandler.writeErrorLog(e)
                                InfoBox.error(
                                    "Unable to start",
                                    ("Unable to open PolyGlot main frame: \n"
                                            + e.message + "\n"
                                            + "Please contact developer (draquemail@gmail.com) for assistance."),
                                    null
                                )
                                if (s != null) {
                                    s.dispose()
                                }
                            }
                        }
                    } catch (t: Throwable) {
                        InfoBox.error(
                            "PolyGlot Error",
                            "A serious error has occurred: " + t.getLocalizedMessage(),
                            null
                        )
                        IOHandler.writeErrorLog(t)
                        throw t
                    }
                })
            } catch (e: Exception) {
                IOHandler.writeErrorLog(e, "Startup Exception")
                InfoBox.error(
                    "PolyGlot Error",
                    "A serious error has occurred: " + e.localizedMessage,
                    null
                )
                throw e
            }
        }

        /**
         * Displays beta message if appropriate (beta builds have warning text within lib folder)
         * Sets version to display as beta
         */
        private fun conditionalBetaSetup() {
            if (PGTUtil.IS_BETA && !PGTUtil.isInJUnitTest()) { // This requires user interaction and is not covered by the test
                InfoBox.warning(
                    "BETA BUILD",
                    "This is a pre-release, beta build of PolyGlot. Please use with care.\n\nBuild Date: " + PGTUtil.BUILD_DATE_TIME,
                    null
                )
            }
        }

        private fun setupNimbus() {
            try {
                for (info: LookAndFeelInfo in javax.swing.UIManager.getInstalledLookAndFeels()) {
                    if (("Nimbus" == info.getName())) {
                        javax.swing.UIManager.setLookAndFeel(info.getClassName())
                        break
                    }
                }
            } catch (e: ClassNotFoundException) {
                IOHandler.writeErrorLog(e)
            } catch (e: InstantiationException) {
                IOHandler.writeErrorLog(e)
            } catch (e: IllegalAccessException) {
                IOHandler.writeErrorLog(e)
            } catch (e: UnsupportedLookAndFeelException) {
                IOHandler.writeErrorLog(e)
            }
        }

        private fun setupCustomUI() {
            UIManager.put(
                "ScrollBarUI",
                "org.darisadesigns.polyglotlina.CustomControls.PScrollBarUI"
            )
            UIManager.put(
                "SplitPaneUI",
                "org.darisadesigns.polyglotlina.CustomControls.PSplitPaneUI"
            )
            UIManager.put("ToolTipUI", "org.darisadesigns.polyglotlina.CustomControls.PToolTipUI")
            UIManager.put("OptionPane.background", Color.WHITE)
            UIManager.put("Panel.background", Color.WHITE)
            UIManager.getLookAndFeelDefaults().put("Panel.background", Color.WHITE)
        }

        /**
         * Tests whether PolyGlot can start, informs user of startup problems.
         * @return
         */
        private fun canStart(): Boolean {
//        String startProblems = "";
            val ret: Boolean = true

            // Currently nothing left to test for... Leaving for sake of possible future utility

//        if (startProblems.length() != 0) {
//            InfoBox.error("Unable to start PolyGlot", startProblems, null);
//            ret = false;
//        }
            return ret
        }

        /**
         * enable cut/copy/paste/select all if running on a Mac, and any other
         * specific, text based bindings I might choose to add later
         */
        private fun setupOSSpecificCutCopyPaste() {
            if (System.getProperty("os.name")!!.startsWith("Mac")) {
                for (inputMap: String in PGTUtil.INPUT_MAPS) {
                    addTextBindings(inputMap, KeyEvent.META_DOWN_MASK)
                }
            }
        }

        /**
         * Adds copy/paste/cut/select all bindings to the input map provided
         *
         * @param UIElement the string representing a UI Element in UIManager
         * @param mask the mask to associate the binding with (command or control,
         * for Macs or PC/Linux boxes, respectively.)
         */
        private fun addTextBindings(UIElement: String, mask: Int) {
            SwingUtilities.invokeLater(Runnable {
                try {
                    val im: InputMap = UIManager.get(UIElement) as InputMap
                    im.put(
                        KeyStroke.getKeyStroke(
                            KeyEvent.VK_C,
                            Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx() or mask
                        ), DefaultEditorKit.copyAction
                    )
                    im.put(
                        KeyStroke.getKeyStroke(
                            KeyEvent.VK_V,
                            Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx() or mask
                        ), DefaultEditorKit.pasteAction
                    )
                    im.put(
                        KeyStroke.getKeyStroke(
                            KeyEvent.VK_X,
                            Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx() or mask
                        ), DefaultEditorKit.cutAction
                    )
                    im.put(
                        KeyStroke.getKeyStroke(
                            KeyEvent.VK_A,
                            Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx() or mask
                        ), DefaultEditorKit.selectAllAction
                    )
                    UIManager.put(UIElement, im)
                } catch (e: NullPointerException) {
                    IOHandler.writeErrorLog(e, "Unable to get input map for: " + UIElement)
                }
            })
        }

        /**
         * Creates and returns testing shell to be used in file veracity tests (IOHandler writing of files)
         * @return
         * @throws java.io.IOException
         */
        @get:Throws(IOException::class, Exception::class)
        val testShell: PolyGlot
            get() {
                return PolyGlot(Files.createTempDirectory("POLYGLOT").toFile().getAbsolutePath())
            }
    }

    init {
        IOHandler.loadOptionsIni(optionsManager, workingDirectory.getAbsolutePath())
        refreshUiDefaults()
    }
}