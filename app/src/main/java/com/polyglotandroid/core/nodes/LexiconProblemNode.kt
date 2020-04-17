package com.polyglotandroid.core.nodes

class LexiconProblemNode(var problemWord: ConWord, var description: String) :
    Comparable<LexiconProblemNode?> {
    override fun toString(): String {
        return problemWord.value
    }

    override operator fun compareTo(other: LexiconProblemNode?): Int =
        if (other != null)
            problemWord.compareTo(other.problemWord)
        else 1
}
