package com.polyglotandroid.core

import org.jsoup.Jsoup
import org.w3c.dom.Document
import org.xml.sax.InputSource
import java.io.IOException
import java.io.StringReader
import java.net.InetSocketAddress
import java.net.MalformedURLException
import java.net.Socket
import java.net.URL
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory

/**
 * This class handles all web communication to and from PolyGlotAndroid
 */
class WebInterface {
    companion object {
        /**
         * Checks for updates to PolyGlot and return The XML document retrieved
         * from the web and throws [Exception] if there is a problem.
         */
        @Throws(Exception::class)
        fun checkForUpdates(): Document? {
            var ret: Document? = null
            var xmlText = ""
            val url: URL
            try {
                url = URL(PGUtil.UPDATE_FILE_URL)
                url.openStream().use { `is` ->
                    Scanner(`is`).use { s ->
                        while (s.hasNext()) {
                            xmlText += s.nextLine()
                        }
                    }
                }
            } catch (e: MalformedURLException) {
                throw Exception("Server unavailable or not found.", e)
            } catch (e: IOException) {
                throw IOException(
                    "Update file not found or has been moved. Please check for updates manually at PolyGlot homepage.",
                    e
                )
            }
            if (xmlText.contains("<TITLE>Moved Temporarily</TITLE>")) {
                throw Exception("Update file not found or has been moved. Please check for updates manually at PolyGlot homepage.")
            }
            if (xmlText.isNotEmpty()) {
                val factory: DocumentBuilderFactory = DocumentBuilderFactory.newInstance()
                val builder: DocumentBuilder = factory.newDocumentBuilder()
                val `is` = InputSource(StringReader(xmlText))
                ret = builder.parse(`is`)
            }
            return ret
        }


        /**
         * Gets only text from a PTextPane's [html]
         */
        fun getTextFromHtml(html: String): String = Jsoup.parse(html).text()

        /**
         * Takes archived [html] and translates it into display HTML.
         * Replaces archival image references to temp image refs
         * @param core
         * @return unarchived html
         * @throws java.lang.Exception
         */
        @Throws(java.lang.Exception::class)
        fun unarchiveHTML(html: String, core: DictCore): String? {
            // pattern for finding archived images
            var html = html
            val pattern: Pattern = Pattern.compile("(<img src=\"[^>,_]+\">)")
            val matcher: Matcher = pattern.matcher(html)
            while (matcher.find()) {
                var regPath: String? = matcher.group(1)
                regPath = regPath.replace("<img src=\"", "")
                regPath = regPath.replace("\"", "")
                regPath = regPath.replace(">", "")
                html = try {
                    val imageId = regPath.toInt()
                    val image: ImageNode =
                        core.getImageCollection().getNodeById(imageId) as ImageNode
                    html.replace(
                        "<img src=\"$regPath\">",
                        "<img src=\"file:///" + image.getImagePath().toString() + "\">"
                    )
                } catch (e: IOException) {
                    throw java.lang.Exception("problem loading image : " + e.localizedMessage, e)
                } catch (e: NumberFormatException) {
                    throw java.lang.Exception("problem loading image : " + e.localizedMessage, e)
                }
            }
            return html
        }

        /**
         * Takes display HTML and translates it into archival HTML.
         * - Replaces actual image references with static, id based refs
         * @param html unarchived html
         * @return archivable html
         */
        fun archiveHTML(html: String): String? {
            // pattern for finding unarchived images
            var html = html
            val pattern =
                Pattern.compile("(<img src=\"[^>,_]+_[^>]+\">)")
            val matcher = pattern.matcher(html)
            while (matcher.find()) {
                var regPath = matcher.group(1)
                regPath = regPath.replace("<img src=\"file:///", "")
                regPath = regPath.replace("\"", "")
                regPath = regPath.replace(">", "")
                val fileName: String? = IOHandler.getFilenameFromPath(regPath)
                val arcPath = fileName.replaceFirst("_.*".toRegex(), "")
                html = html.replace("file:///$regPath", arcPath)
            }
            return html
        }

        /**
         * This cycles through the body of HTML and generates an ordered list of objects
         * representing all of the items in the HTML. Consumers are responsible for
         * identifying objects.
         * @param html HTML to extract from
         * @return
         * @throws java.io.IOException
         */
        @Throws(IOException::class)
        fun getElementsHTMLBody(html: String): List<Any>? {
            val ret: MutableList<Any> = ArrayList()
            var body = html.replace(".*<body>".toRegex(), "")
            body = body.replace("</body>.*".toRegex(), "")
            val pattern =
                Pattern.compile("([^<]+|<[^>]+>)") //("(<[^>]+>)");
            val matcher = pattern.matcher(body)

            // loops on unincumbered text and tags.
            while (matcher.find()) {
                val token = matcher.group(1)
                if (token.startsWith("<")) {
                    if (token.contains("<img src=\"")) {
                        val path =
                            token.replace("<img src=\"file:///", "").replace("\">", "")
                        ret.add(IOHandler.getImage(path))
                    } else {
                        // do nothing with unrecognized elements - might be upgraded later.
                    }
                } else {
                    // this is plaintext
                    val add = token.trim { it <= ' ' }
                    if (!add.isEmpty()) {
                        ret.add("$add ")
                    }
                }
            }
            return ret
        }

        /**
         * Tests current internet connection based on google
         * @return true if connected
         */
        fun isInternetConnected(): Boolean {
            val address = "www.google.com"
            val PORT = 80
            val TIMEOUT = 5000
            var ret = false
            try {
                Socket().use { soc -> soc.connect(InetSocketAddress(address, PORT), TIMEOUT) }
                ret = true
            } catch (e: IOException) {
                IOHandler.writeErrorLog(e, "Unable to reach: $address")
            }
            return ret
        }
    }
}