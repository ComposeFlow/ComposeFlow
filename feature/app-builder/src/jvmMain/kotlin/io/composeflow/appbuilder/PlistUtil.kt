package io.composeflow.appbuilder

import java.io.ByteArrayInputStream
import javax.xml.parsers.DocumentBuilderFactory

object PlistUtil {
    fun parsePlistToMap(plistContent: String): Map<String, String> {
        val resultMap = mutableMapOf<String, String>()

        // Prepare to parse the XML content
        val factory = DocumentBuilderFactory.newInstance()
        val builder = factory.newDocumentBuilder()
        val inputStream = ByteArrayInputStream(plistContent.toByteArray(Charsets.UTF_8))
        val document = builder.parse(inputStream)
        document.documentElement.normalize()

        // Get the <dict> element
        val dictElements = document.getElementsByTagName("dict")
        if (dictElements.length > 0) {
            val dictNode = dictElements.item(0)
            val childNodes = dictNode.childNodes

            var currentKey: String? = null

            // Iterate over the child nodes of <dict>
            for (i in 0 until childNodes.length) {
                val node = childNodes.item(i)
                when (node.nodeName) {
                    "key" -> {
                        currentKey = node.textContent
                    }

                    "string", "integer", "real" -> {
                        currentKey?.let {
                            resultMap[it] = node.textContent
                            currentKey = null
                        }
                    }

                    "true", "false" -> {
                        currentKey?.let {
                            resultMap[it] = node.nodeName // "true" or "false"
                            currentKey = null
                        }
                    }
                }
            }
        }
        return resultMap
    }
}
