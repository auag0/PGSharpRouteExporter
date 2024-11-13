package io.github.auag0.pgsharprouteexporter.utils

import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import java.io.StringReader

object SPParser {
    private const val TAG_STRING = "string"
    private const val TAG_LONG = "long"
    private const val TAG_INT = "int"
    private const val TAG_BOOLEAN = "boolean"
    private const val TAG_FLOAT = "float"
    private const val TAG_SET = "set"
    private const val ATTRIBUTE_NAME = "name"
    private const val ATTRIBUTE_VALUE = "value"
    private const val VALUE_NULL = "null"

    fun parseXmlText(xmlText: String): Map<String, Any?> {
        val xmlPullParser: XmlPullParser = Xml.newPullParser()
        xmlPullParser.setInput(StringReader(xmlText))

        val items: MutableMap<String, Any?> = mutableMapOf()
        var key = ""
        var stringSet: MutableSet<String>? = null

        while (xmlPullParser.next() != XmlPullParser.END_DOCUMENT) {
            when (xmlPullParser.eventType) {
                XmlPullParser.START_TAG -> {
                    val tagName = xmlPullParser.name ?: continue

                    if (tagName == TAG_SET) {
                        stringSet = mutableSetOf()
                    } else if (stringSet != null && tagName == TAG_STRING) {
                        stringSet.add(xmlPullParser.nextText())
                    }

                    val value = xmlPullParser.getAttributeValue(null, ATTRIBUTE_VALUE)
                    key = xmlPullParser.getAttributeValue(null, ATTRIBUTE_NAME) ?: continue

                    val itemValue = when (tagName) {
                        TAG_BOOLEAN -> value.toBooleanStrictOrNull()
                        TAG_FLOAT -> value.toFloatOrNull()
                        TAG_INT -> value.toIntOrNull()
                        TAG_LONG -> value.toLongOrNull()
                        TAG_STRING -> xmlPullParser.nextText() ?: VALUE_NULL
                        else -> null
                    }
                    if (itemValue != null) {
                        items[key] = itemValue
                    }
                }

                XmlPullParser.END_TAG -> {
                    if (xmlPullParser.name == TAG_SET) {
                        if (stringSet != null) {
                            items[key] = stringSet
                        }
                    }
                    stringSet = null
                }
            }
        }
        return items
    }
}