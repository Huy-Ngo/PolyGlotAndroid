package com.polyglotandroid.core

import org.xml.sax.helpers.DefaultHandler

class CustHandler : DefaultHandler() {
    var errorLog = ""
    var warningLog = ""
}

