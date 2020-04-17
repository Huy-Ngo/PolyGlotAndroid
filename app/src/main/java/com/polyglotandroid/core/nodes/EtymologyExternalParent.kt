package com.polyglotandroid.core.nodes

/**
 * This node represents an external etymological parent
 */
class EtymologyExternalParent(_id: Int) : DictNode(_id) {
    private var externalLanguage = ""
    private var definition = ""
    var externalWord: String?
        get() = value
        set(externalWord) {
            value = externalWord!!
        }

    val uniqueId: String
        get() = value + externalLanguage

    override fun toString(): String {
        return value + if (externalLanguage.isEmpty()) "" else " ($externalLanguage)"
    }

    override fun equals(other: Any?): Boolean =
        if (other == null || other !is EtymologyExternalParent)
            false
        else this.externalWord == other.externalWord && this.definition == other.definition &&
                this.externalLanguage == other.externalLanguage

    @Throws(ClassCastException::class)
    override fun setEqual(_node: DictNode) {
        if (_node is EtymologyExternalParent) {
            definition = _node.definition
            externalLanguage = _node.externalLanguage
        } else {
            throw ClassCastException(
                "Type: "
                        + _node.javaClass.canonicalName
                        + " cannot be explicitly converted to "
                        + javaClass.canonicalName + "."
            )
        }
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + externalLanguage.hashCode()
        result = 31 * result + definition.hashCode()
        return result
    }
}