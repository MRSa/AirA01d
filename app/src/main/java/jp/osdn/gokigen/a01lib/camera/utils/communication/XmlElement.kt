package jp.osdn.gokigen.a01lib.camera.utils.communication

import android.util.Log
import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import java.io.StringReader

class XmlElement(var tagName: String = "")
{
    var value: String = ""
    var parent: XmlElement? = null
        private set

    private val childElements = mutableListOf<XmlElement>()
    private val attributes = mutableMapOf<String, String>()

    // 子要素の追加（親子関係を自動設定）
    private fun addChild(child: XmlElement)
    {
        child.parent = this
        childElements.add(child)
    }

    // 特定のタグ名を持つ最初の子要素を返す（見つからない場合はnull）
    fun findChild(name: String): XmlElement? = childElements.find { it.tagName == name }

    // 特定のタグ名を持つ全子要素を返す
    fun findChildren(name: String): List<XmlElement> = childElements.filter { it.tagName == name }

    fun getAttribute(name: String, defaultValue: String? = null): String? {
        return attributes[name] ?: defaultValue
    }

    companion object {
        private val TAG = XmlElement::class.java.simpleName

        fun parse(xmlStr: String): XmlElement? {
            return try {
                val parser = Xml.newPullParser().apply {
                    setInput(StringReader(xmlStr))
                }
                parse(parser)
            } catch (e: Exception) {
                Log.e(TAG, "XML Parse Error", e)
                null
            }
        }

        private fun parse(parser: XmlPullParser): XmlElement?
        {
            var root: XmlElement? = null
            var current: XmlElement? = null

            try
            {
                var eventType = parser.eventType
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    when (eventType) {
                        XmlPullParser.START_TAG -> {
                            val newElement = XmlElement(parser.name)

                            // 属性の読み込み
                            for (i in 0 until parser.attributeCount) {
                                newElement.attributes[parser.getAttributeName(i)] = parser.getAttributeValue(i)
                            }

                            if (root == null) {
                                root = newElement
                            } else {
                                current?.addChild(newElement)
                            }
                            current = newElement
                        }
                        XmlPullParser.TEXT -> {
                            current?.value = parser.text ?: ""
                        }
                        XmlPullParser.END_TAG -> {
                            current = current?.parent
                        }
                    }
                    eventType = parser.next()
                }
            }
            catch (e: Exception)
            {
                Log.e(TAG, "Error during parsing", e)
                return null
            }
            return root
        }
    }
}
