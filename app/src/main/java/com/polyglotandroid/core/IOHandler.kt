package com.polyglotandroid.core

import java.io.File

class IOHandler {
    companion object {
        /**
         * Get filename from [fullPath]
         */
        fun getFilenameFromPath(fullPath: String): String? {
            val file = File(fullPath)
            return file.name
        }
    }
}
