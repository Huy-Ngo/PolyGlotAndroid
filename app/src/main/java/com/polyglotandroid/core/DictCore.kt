package com.polyglotandroid.core

import com.polyglotandroid.core.collections.ConWordCollection


class DictCore (_polyGlot: PolyGlot) {
    val version = "2.5"
    val wordCollection: ConWordCollection = ConWordCollection(this)
    val typeCollection: TypeCollection = TypeCollection(this)
    val declensionMgr: DeclensionManager = DeclensionManager(this)
    val propertiesManager: PropertiesManager? = null
    val pronunciationManager: PronunciationManager? = null
    val romMgr: RomanizationManager? = null
    val famManager: FamilyManager? = null

    // LogoCollection logoCollection; //logographs not currently printed to PDF
    val grammarManager: GrammarManager? = null
    val optionsManager: OptionsManager? = null
    val wordPropCollection: WordClassCollection? = null
    val imageCollection: ImageCollection? = null
    val etymologyManager: EtymologyManager? = null
    val visualStyleManager: VisualStyleManager? = null
    val reversionManager: ReversionManager? = null
    val toDoManager: ToDoManager? = null
    val clipBoard: Any? = null
    val curLoading = false
    val versionHierarchy: Map<String, Int> = HashMap()
    val lastSaveTime: Instant = Instant.MIN


    fun getTypes(): Any {
        TODO("not yet implemented")
    }

    val types: Any
    val polyGlot: PolyGlot = _polyGlot
    var wordCollection: ConWordCollection
    var propertiesManager: PropertiesManager
    // TODO: other properties
    init {
        try {
            wordCollection = ConWordCollection(this)
        }
    }
}