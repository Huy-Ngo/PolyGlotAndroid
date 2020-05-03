package com.polyglotandroid.core

import org.xml.sax.SAXException
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class IOHandler {
    companion object {
        /**
         * Get filename from [fullPath]
         */
        fun getFilenameFromPath(fullPath: String): String? {
            val file = File(fullPath)
            return file.name
        }

        fun writeErrorLog(exception: SAXException, comment: String = "") {
            var curContents = ""
            var errorMessage: String =
                DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss").format(LocalDateTime.now())
            errorMessage += "-" + exception.localizedMessage
                .toString() + "-" + exception.getClass().getName()
            var rootCause: Throwable = ExceptionUtils.getRootCause(exception)
            rootCause = rootCause ?: exception
            errorMessage += """
                
                ${ExceptionUtils.getStackTrace(rootCause)}
                """.trimIndent()

            if (!comment.isEmpty()) {
                errorMessage = "$comment:\n$errorMessage"
            }

            val errorLog = File(
                PGUtil.getErrorDirectory().getAbsolutePath()
                    .toString() + File.separator + PGUtil.ERROR_LOG_FILE
            )

            try {
                if (errorLog.exists()) {
                    Scanner(errorLog).useDelimiter("\\Z").use({ logScanner ->
                        curContents = if (logScanner.hasNext()) logScanner.next() else ""
                        val length = curContents.length
                        val newLength = length + errorMessage.length
                        if (newLength > PGUtil.MAX_LOG_CHARACTERS) {
                            curContents =
                                curContents.substring(newLength - PGUtil.MAX_LOG_CHARACTERS)
                        }
                    })
                }
                BufferedWriter(FileWriter(errorLog)).use({ writer ->
                    val output: String =
                        getSystemInformation().toString() + "\n" + curContents + errorMessage + "\n"
                    println("Writing error to: " + errorLog.absolutePath)
                    writer.write(output)
                })
            } catch (e: IOException) {
                // Fail silently. This fails almost exclusively due to being run in write protected folder, caught elsewhere
                // do not log to written file for obvious reasons (causes further write failure)
                // WHY DO PEOPLE INSTALL THIS TO WRITE PROTECTED FOLDERS AND SYSTEM32. WHY.
                // IOHandler.writeErrorLog(e);
            }

        }
    }
}
