package com.polyglotandroid.core.nodes

import com.polyglotandroid.core.IOHandler
import com.polyglotandroid.core.PGUtil
import org.w3c.dom.Document
import org.w3c.dom.Node
import org.xml.sax.SAXException
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException


/**
 * A node representing one prior state of a language XML file
 */
class ReversionNode : Comparable<ReversionNode?> {
    val value: ByteArray
    private var saveTime: Instant

    constructor(_value: ByteArray) {
        value = _value
        saveTime = Instant.MIN
        populateTimeFromDoc()
    }

    constructor(_value: ByteArray, _saveTime: Instant) {
        value = _value
        saveTime = _saveTime
    }

    /**
     * Isolates lengthy process in individual thread
     */
    private fun populateTimeFromDoc() {
        object : Thread() {
            override fun run() {
                saveTime = lastSaveTimeFromRawDoc
            }
        }.start()
    }

    private val lastSaveTimeFromRawDoc: Instant
        private get() {
            var ret: Instant
            try {
                val `is`: InputStream = ByteArrayInputStream(value)
                val dbFactory: DocumentBuilderFactory = DocumentBuilderFactory.newInstance()
                val dBuilder: DocumentBuilder = dbFactory.newDocumentBuilder()
                val doc: Document
                doc = dBuilder.parse(`is`)
                doc.getDocumentElement().normalize()
                val timeNode: Node = doc.getElementsByTagName(PGUtil.DICTIONARY_SAVE_DATE).item(0)
                ret = if (timeNode != null) {
                    Instant.parse(timeNode.getTextContent())
                } else {
                    Instant.MIN
                }
            } catch (e: SAXException) {
                IOHandler.writeErrorLog(e)
                ret = Instant.MIN
            } catch (e: IOException) {
                IOHandler.writeErrorLog(e)
                ret = Instant.MIN
            } catch (e: ParserConfigurationException) {
                IOHandler.writeErrorLog(e)
                ret = Instant.MIN
            }
            return ret
        }

    override fun toString(): String {
        var ret = "saved: "
        if (saveTime != Instant.MIN) {
            val formatter: DateTimeFormatter =
                DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss").withZone(ZoneId.systemDefault())
            ret += formatter.format(saveTime)
        } else {
            ret += "<UNKNOWN TIME>"
        }
        return ret
    }

    override fun compareTo(other: ReversionNode?): Int {
        // returns in reverse order
        return -saveTime.compareTo(other.saveTime)
    }
}