package com.polyglotandroid.core

/**
 * This contains various constant values in PolyGlotAndroid
 */
class PGUtil {
    companion object {
        // constants
        private val VERSION_HIERARCHY: Map<String, Int>? = null
        val BUILD_DATE_TIME: String? = null
        const val DICTIONARY_XID = "dictionary"
        const val PG_VERSION_XID = "PolyGlotVersion"
        const val DICTIONARY_SAVE_DATE = "DictSaveDate"
        const val MAIN_MENU_IMAGE = "/assets/org/DarisaDesigns/ImageAssets/PolyGlotBG.png"
        const val POLYGLOT_FILE_SUFFIX = "pgd"
        const val VERSION_LOCATION = "/assets/org/DarisaDesigns/version"
        val PG_VERSION: String? = null
        const val IS_BETA = false
        const val HELP_FILE_ARCHIVE_LOCATION = "/assets/org/DarisaDesigns/readme.zip"
        const val EXAMPLE_LANGUAGE_ARCHIVE_LOCATION = "/assets/org/DarisaDesigns/exlex.zip"
        const val HELP_FILE_NAME = "readme.html"
        const val SWADESH_LOCATION = "/assets/org/DarisaDesigns/swadesh/"
        const val BUILD_DATE_TIME_LOCATION = "/assets/org/DarisaDesigns/buildDate"
        val SWADESH_LISTS =
            arrayOf("Original_Swadesh", "Modern_Swadesh")

        // properties on words
        const val LEXICON_XID = "lexicon"
        const val WORD_XID = "word"
        const val LOCAL_WORD_XID = "localWord"
        const val CONWORD_XID = "conWord"
        const val WORD_POS_ID_XID = "wordTypeId"
        const val WORD_ID_XID = "wordId"
        const val WORD_PRONUNCIATION_OVERRIDE_XID = "wordProcOverride"
        const val WORD_DEFINITION_XID = "definition"
        const val WORD_AUTO_DECLENSION_OVERRIDE_XID = "autoDeclOverride"
        const val WORD_PRONUNCIATION_XID = "pronunciation"
        const val WORD_RULES_OVERRIDE_XID = "wordRulesOverride"
        const val WORD_CLASS_COLLECTION_XID = "wordClassCollection"
        const val WORD_CLASS_AND_VALUE_XID = "wordClassification"
        const val WORD_CLASS_TEXT_VALUE_COLLECTION_XID = "wordClassTextValueCollection"
        const val WORD_CLASS_TEXT_VALUE_XID = "wordClassTextValue"
        const val WORD_ETYMOLOGY_NOTES_XID = "wordEtymologyNotes"

        // properties for types/parts of speech
        const val POS_COLLECTION_XID = "partsOfSpeech"
        const val POS_XID = "class"
        const val POS_NAME_XID = "className"
        const val POS_ID_XID = "classId"
        const val POS_NOTES_XID = "classNotes"
        const val POS_PRONUNCIATION_MANDATORY_XID = "pronunciationMandatoryClass"
        const val POS_DEFINITION_MANDATORY_XID = "definitionMandatoryClass"
        const val POS_PATTERN_XID = "classPattern"
        const val POS_GLOSS_XID = "classGloss"

        // Language properties
        const val LANGUAGE_PROPERTIES_XID = "languageProperties"
        const val FONT_CON_XID = "fontCon"
        const val FONT_LOCAL_XID = "fontLocal"
        const val LANGUAGE_PROP_LANGUAGE_NAME_XID = "languageName"
        const val LANGUAGE_PROP_FONT_SIZE_XID = "fontSize"
        const val LANGUAGE_PROP_FONT_STYLE_XID = "fontStyle"
        const val LANGUAGE_PROP_LOCAL_FONT_SIZE_XID = "localFontSize"
        const val LANGUAGE_PROP_ALPHA_ORDER_XID = "alphaOrder"
        const val LANGUAGE_PROP_TYPE_MANDATORY_XID = "languagePropTypeMandatory"
        const val LANGUAGE_PROP_LOCAL_MANDATORY_XID = "languagePropLocalMandatory"
        const val LANGUAGE_PROP_WORD_UNIQUE_XID = "languagePropWordUniqueness"
        const val LANGUAGE_PROP_LOCAL_UNIQUE_XID = "languagePropLocalUniqueness"
        const val LANGUAGE_PROP_IGNORE_CASE_XID = "languagePropIgnoreCase"
        const val LANGUAGE_PROP_DISABLE_PRONUNCIATION_REGEX = "languagePropDisablePronunciationRegex"
        const val LANGUAGE_PROP_ENFORCE_RTL_XID = "languagePropEnforceRTL"
        const val LANGUAGE_PROP_AUTHOR_COPYRIGHT_XID = "languagePropAuthorCopyright"
        const val LANGUAGE_PROP_LOCAL_NAME_XID = "languagePropLocalLanguageName"
        const val LANGUAGE_PROP_USE_LOCAL_LEXICON_XID = "languagePropUseLocalLexicon"
        const val LANGUAGE_PROP_KERNING_VALUE_XID = "languagePropKerningValue"
        const val LANGUAGE_PROP_OVERRIDE_REGEX_FONT_XID = "languagePropOverrideRegexFont"
        const val LANGUAGE_PROP_USE_SIMPLIFIED_CONJ = "languagePropUseSimplifiedConjugations"

        // character replacement pair values
        // TODO: Figure out what these abbreviations mean or ask upstream and extend it
        const val LANG_PROP_CHAR_REP_CONTAINER_XID = "langPropCharRep"
        const val LANG_PROP_CHAR_REP_NODE_XID = "langPropCharRepNode"
        const val LANG_PROP_CHAR_REP_CHAR_XID = "langPropCharRepCharacter"
        const val LANG_PROP_CHAR_REP_VAL_XID = "langPropCharRepValue"

        // declension properties
        val DECLENSION_COLLECTION_XID: String? = "declensionCollection"
        const val DECLENSION_XID = "declensionNode"
        const val DECLENSION_ID_XID = "declensionId"
        const val DECLENSION_TEXT_XID = "declensionText"
        const val DECLENSION_COMBINED_DIMENSION_XID = "combinedDimensionId"
        const val DECLENSION_NOTES_XID = "declensionNotes"
        const val DECLENSION_IS_TEMPLATE_XID = "declensionTemplate"
        const val DECLENSION_RELATED_ID_XID = "declensionRelatedId"
        const val DECLENSION_IS_DIMENSIONLESS_XID = "declensionDimensionless"

        // dimensional declension properties
        const val DIMENSION_NODE_XID = "dimensionNode"
        const val DIMENSION_ID_XID = "dimensionId"
        const val DIMENSION_NAME_XID = "dimensionName"

        // pronunciation properties
        const val PRONUNCIATION_ETYMOLOGY_COLLECTION_XID = "pronunciationEtymologyCollection"
        const val PRONUNCIATION_GUIDE_XID = "pronunciationGuide"
        const val PRONUNCIATION_GUIDE_BASE_XID = "pronunciationGuideBase"
        const val PRONUNCIATION_GUIDE_PHONOLOGY_XID = "pronunciationGuidePhonology"
        const val PRONUNCIATION_GUIDE_RECURSIVE_XID = "pronunciationGuideRecursive"

        // romanization properties
        const val ROMANIZATION_GUIDE_XID = "romanizationGuide"
        const val ROMANIZATION_GUIDE_ENABLED_XID = "romanizationGuideEnabled"
        const val ROMANIZATION_GUIDE_NODE_XID = "romanizationGuideNode"
        const val ROMANIZATION_GUIDE_BASE_XID = "romanizationGuideBase"
        const val ROMANIZATION_GUIDE_PHONOLOGY_XID = "romanizationGuidePhonology"
        const val ROMANIZATION_GUIDE_RECURSIVEE_XID = "romanizationGuideRecursive"

        // family properties
        // Originally these are thesNode etc but idk why
        const val FAMILY_NODE_XID = "familyNode"
        const val FAMILY_NOTES_XID = "familyNotes"
        const val FAMILY_NAME_XID = "familyName"
        const val FAMILY_WORD_XID = "familyWord"

        // auto-declension generation properties
        const val DECLENSION_GENERATION_RULE_XID = "declensionGenerationRule"
        const val DECLENSION_GENERATION_RULE_TYPE_XID = "declensionGenerationRuleTypeId"
        const val DECLENSION_GENERATION_RULE_COMB_XID = "declensionGenerationRuleComb"
        const val DECLENSION_GENERATION_RULE_REGEX_XID = "declensionGenerationRuleRegex"
        const val DECLENSION_GENERATION_RULE_NAME_XID = "declensionGenerationRuleName"
        const val DECLENSION_GENERATION_RULE_INDEX_XID = "declensionGenerationRuleIndex"
        const val DECLENSION_GENERATION_RULE_APPLY_TO_CLASSES_XID = "declensionGenerationRuleApplyToClasses"
        const val DECLENSION_GENERATION_RULE_APPLY_TO_CLASS_VALUE_XID = "declensionGenerationRuleApplyToClassValue"

        // auto-declension transform properties
        const val DECLENSION_GENERATION_TRANS_XID = "declensionGenerationTrans"
        const val DECLENSION_GENERATION_TRANS_REGEX_XID = "declensionGenerationTransRegex"
        const val DECLENSION_GENERATION_TRANS_REPLACE_XID = "declensionGenerationTransReplace"

        // constructed declension dimension properties
        const val DECLENSION_COMBINED_FORM_SECTION_XID = "decCombinedFormSection"
        const val DECLENSION_COMBINED_FORM_XID = "decCombinedForm"
        const val DECLENSION_COMBINED_ID_XID = "decCombinedId"
        const val DECLENSION_COMBINED_SUPPRESS_XID = "decCombinedSuppress"

        // properties for logographs
        const val LOGO_ROOT_NOTE_XID = "logoRootNode"
        const val LOGOGRAPHS_COLLECTION_XID = "logoGraphsCollection"
        const val LOGO_STROKES_XID = "logoStrokes"
        const val LOGO_NOTES_XID = "logoNotes"
        const val LOGO_IS_RADICAL_XID = "logoIsRadical"
        const val LOGO_RADICAL_LIST_XID = "logoRadicalList"
        const val LOGO_READING_LIST_XID = "logoReading"
        const val LOGOGRAPH_VALUE_XID = "logoGraphValue"
        const val LOGOGRAPH_ID_XID = "logoGraphId"
        const val LOGOGRAPH_NODE_XID = "LogoGraphNode"
        const val LOGO_WORD_RELATION_XID = "LogoWordRelation"
        const val LOGO_RELATION_COLLECTION_XID = "LogoRelationsCollection"

        // properties for the grammar dictionary
        const val GRAMMAR_SECTION_XID = "grammarCollection"
        const val GRAMMAR_CHAPTER_NODE_XID = "grammarChapterNode"
        const val GRAMMAR_CHAPTER_NAME_XID = "grammarChapterName"
        const val GRAMMAR_SECTIONS_LIST_XID = "grammarSectionsList"
        const val GRAMMAR_SECTION_NODE_XID = "grammarSectionNode"
        const val GRAMMAR_SECTION_NAME_XID = "grammarSectionName"
        const val GRAMMAR_SECTION_RECORDING_XID = "grammarSectionRecordingXID"
        const val GRAMMAR_SECTION_TEXT_XID = "grammarSectionText"

        // properties for word classes
        const val CLASSES_NODE_XID = "wordGrammarClassCollection"
        const val CLASS_XID = "wordGrammarClassNode"
        const val CLASS_ID_XID = "wordGrammarClassID"
        const val CLASS_NAME_XID = "wordGrammarClassName"
        const val CLASS_APPLY_TYPES_XID = "wordGrammarApplyTypes"
        const val CLASS_IS_FREE_TEXT_XID = "wordGrammarIsFreeTextField"
        const val CLASS_VALUES_COLLECTION_XID = "wordGrammarClassValuesCollection"
        const val CLASS_VALUES_NODE_XID = "wordGrammarClassValueNode"
        const val CLASS_VALUE_NAME_XID = "wordGrammarClassValueName"
        const val CLASS_VALUE_ID_XID = "wordGrammarClassValueId"

        // etymology constants
        const val ETYMOLOGY_COLLECTION_XID = "EtymologyCollection"
        const val ETYMOLOGY_INT_RELATION_NODE_XID = "EtymologyInternalRelation"
        const val ETYMOLOGY_INT_CHILD_XID = "EtymologyInternalChild"
        const val ETYMOLOGY_CHILD_EXTERNALS_XID = "EtymologyChildToExternalsNode"
        const val ETYMOLOGY_EXTERNAL_WORD_NODE_XID = "EtymologyExternalWordNode"
        const val ETYMOLOGY_EXTERNAL_WORD_VALUE_XID = "EtymologyExternalWordValue"
        const val ETYMOLOGY_EXTERNAL_WORD_ORIGIN_XID = "EtymologyExternalWordOrigin"
        const val ETYMOLOGY_EXTERNAL_WORD_DEFINITION_XID = "EtymologyExternalWordDefinition"

        // to do Node constants
        const val TODO_LOG_XID = "ToDoLog"
        const val TODO_NODE_XID = "ToDoNodeHead"
        const val TODO_NODE_DONE_XID = "ToDoNodeDone"
        const val TODO_NODE_LABEL_XID = "ToDoNodeLabel"

        // constants for PolyGlot options found in PolyGlot.ini
        const val OPTIONS_NUM_LAST_FILES = 5
        const val OPTIONS_LAST_FILES = "LastFiles"
        const val OPTIONS_SCREEN_POS = "ScreenPositions"
        const val OPTIONS_SCREENS_SIZE = "ScreenSizes"
        const val OPTIONS_SCREENS_OPEN = "ScreensUp"
        const val OPTIONS_AUTO_RESIZE = "OptionsResize"
        const val OPTIONS_MENU_FONT_SIZE = "OptionsMenuFontSize"
        const val OPTIONS_NIGHT_MODE = "OptionsNightMode"
        const val OPTIONS_REVERSIONS_COUNT = "OptionsReversionCount"
        const val OPTIONS_TODO_DIVIDER_LOCATION = "ToDoDividerLocation"

        const val RTL_CHARACTER = "\u202e"
        const val LTR_MARKER = "\u202c"
        const val IMAGE_ID_ATTRIBUTE = "imageIDAttribute"
        const val TRUE = "T"
        const val FALSE = "F"
        const val DISPLAY_NAME = "PolyGlot"
    }
}