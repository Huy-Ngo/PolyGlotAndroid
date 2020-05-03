package com.polyglotandroid.core.managers

import android.graphics.fonts.Font
import com.polyglotandroid.core.DictCore
import com.polyglotandroid.core.PGUtil
import com.polyglotandroid.core.customControls.AlphaMap
import java.io.IOException
import javax.swing.JLabel


/**
 * Contains and manages properties of given language
 * @UI_handler
 */
class PropertiesManager(_core: DictCore) {
    var overrideProgramPath = ""
    var conFont: Font? = null
    var conFontStyle = 0
    var conFontSize = 12.0
    var localFontSize = 12.0
    var alphaOrder: AlphaMap<String, Int>? = null
    var alphaPlainText = ""
    var langName = ""
    var localLangName = ""
    var copyrightAuthorInfo = ""
    var typesMandatory = false
    var localMandatory = false
    var wordUniqueness = false
    var localUniqueness = false
    var overrideRegexFont = false
    var ignoreCase = false
    var enableRomanization = false
    var disableProcRegex = false
    var enforceRTL = false
    var useLocalWordLex = false
    var cachedConFont: ByteArray? = null
    var cachedLocalFont: ByteArray? = null
    var charisUnicode: Font? = null
    var localFont: Font? = null  // FIXME: Look up Android package for Font to use it appropriately
    var charRep: Map<String, String> = HashMap()
    var core: DictCore? = null
    var kerningSpace = 0.0

    @Throws(IOException::class)
    fun PropertiesManager(_core: DictCore) {
        alphaOrder = AlphaMap()
        core = _core
        charisUnicode = JLabel().getFont()
    }

    /**
     * Gets replacement string for given character. Returns blank otherwise.
     * @param repChar character (string in case I decide to use this for something more complex) to be replaced
     * @return replacement string. empty if none exists.
     */
    fun getCharacterReplacement(repChar: String?): String? {
        return if (charRep.isNotEmpty() && charRep.containsKey(repChar))
            charRep[repChar]
        else
            ""
    }

    /**
     * Adds character/replacement set
     * @param character character to look for/be replaced in text
     * @param _replacement the string to replace the character with
     */
    fun addCharacterReplacement(
        character: String?,
        _replacement: String?
    ) {
        var replacement: String = PGUtil.stripRTL(_replacement)
        if (charRep.containsKey(character)) {
            charRep.replace(character, replacement)
        } else {
            charRep.put(character, replacement)
        }
    }

    /**
     * Deletes replacement varue for a character
     * @param character character for replacement varues to be wiped for
     */
    fun delCharacterReplacement(character: String?) {
        if (charRep.containsKey(character)) {
            charRep.remove(character)
        }
    }

    /**
     * Clears all character replacements
     */
    fun clearCharacterReplacement() {
        charRep.clear()
    }

    /**
     * Gets all character replacement pairs
     * @return iterator of map entries with two strings apiece
     */
    fun getAllCharReplacements(): ArrayList<Map.Entry<String?, String?>?>? {
        return ArrayList(charRep.entries)
    }

    fun AddEmptyRep() {
        charRep.put("", "")
    }

    /**
     * Gets unicode charis font. Defaults/hard coded to size 12
     *
     * @return
     */
    fun getFontMenu(): Font? {
        return charisUnicode.deriveFont(0, core!!.optionsManager.getMenuFontSize() as Float)
    }

    fun getFontLocal(): Font? {
        return getFontLocal(localFontSize)
    }

    fun getFontLocal(size: Double): Font? {
        if (localFont == null) {
            localFont = JLabel().getFont()
        }
        return localFont.deriveFont(0, size.toFloat())
    }

    fun setLocalFont(_localFont: Font) {
        setLocalFont(_localFont, localFontSize)
    }

    fun setLocalFont(
        _localFont: Font,
        size: Double
    ) {
        // null cached font if being set to new font
        if (localFont != null && !localFont.getFamily().equals(_localFont.getFamily())) {
            cachedLocalFont = null
        }
        localFont = _localFont
        localFontSize = size
    }
}