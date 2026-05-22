package jp.osdn.gokigen.a01lib.camera.omds.status

import android.util.Log
import jp.osdn.gokigen.a01lib.camera.interfaces.ICameraStatus
import jp.osdn.gokigen.a01lib.camera.interfaces.ICameraStatus.CameraProperty
import jp.osdn.gokigen.a01lib.camera.utils.communication.SimpleHttpClient
import java.util.HashMap
import kotlin.collections.set

class OmdsCameraProperties(
    userAgent: String = "OlympusCameraKit",
    private val executeUrl : String = "http://192.168.0.10",
)
{
    private val headerMap: MutableMap<String, String> = HashMap()
    private val http = SimpleHttpClient()

    fun setStatus(key: CameraProperty, value: String)
    {
        try
        {
            when (key)
            {
                CameraProperty.TakeMode ->  sendSetPropertyRequest("takemode", value)
                CameraProperty.ShutterSpeed ->  sendSetPropertyRequest("shutspeedvalue", value)
                CameraProperty.Aperture ->  sendSetPropertyRequest("focalvalue", value)
                CameraProperty.ExposureCompensation ->  sendSetPropertyRequest("expcomp", value)
                CameraProperty.IsoSensitivity ->  sendSetPropertyRequest("isospeedvalue", value)
                CameraProperty.DriveMode ->  sendSetPropertyRequest("drivemode", value)
                CameraProperty.WhiteBalance ->  sendSetPropertyRequest("wbvalue", decideWhiteBalanceValue(value))
                CameraProperty.PictureEffect ->  sendSetPropertyRequest("colortone", value)
                else -> { }
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    fun setStatusString(propertyName: String, value: String)
    {
        try
        {
            sendSetPropertyRequest(propertyName, value)
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    fun getStatusList(key: CameraProperty): List<String>
    {
        try
        {
            Log.v(TAG, " getStatusListOmds($key)")
            return (when (key) {
                CameraProperty.TakeMode -> getPropertySelectionList(sendGetPropertyDescriptionRequest("takemode"), "takemode")
                CameraProperty.ShutterSpeed -> getPropertySelectionList(sendGetPropertyDescriptionRequest("shutspeedvalue"), "shutspeedvalue")
                CameraProperty.Aperture -> getPropertySelectionList(sendGetPropertyDescriptionRequest("focalvalue"), "focalvalue")
                CameraProperty.IsoSensitivity -> getPropertySelectionList(sendGetPropertyDescriptionRequest("isospeedvalue"), "isospeedvalue")
                CameraProperty.ExposureCompensation -> getPropertySelectionList(sendGetPropertyDescriptionRequest("expcomp"), "expcomp")
                CameraProperty.WhiteBalance -> getAvailableWhiteBalance(sendGetPropertyDescriptionRequest("wbvalue"))
                CameraProperty.PictureEffect -> getPropertySelectionList(sendGetPropertyDescriptionRequest("colortone"), "colortone")
                CameraProperty.DriveMode -> getPropertySelectionList(sendGetPropertyDescriptionRequest("drivemode"), "drivemode")
                else -> ArrayList()
            })
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        return (ArrayList())
    }

    fun getDescriptor(propertyName: String): ICameraStatus.CameraPropertyDescriptor
    {
        // ----- デフォルトの返却値（失敗時用）
        val defaultDescriptor = ICameraStatus.CameraPropertyDescriptor(
            propertyName = propertyName,
            attribute = "get",
            current = "",
            values = emptyList()
        )
        if (propertyName.isEmpty()) return defaultDescriptor // プロパティ名がなければエラー

        try
        {
            val response = sendGetPropertyDescriptionRequest(propertyName)  // 通信でディスクリプタを取得
            if (response.isEmpty()) return defaultDescriptor

            val propertyString = "<propname>$propertyName</propname>"
            val propertyIndex = response.indexOf(propertyString)

            // --- プロパティ名が応答に含まれているか（0以上）
            if (propertyIndex >= 0)
            {
                // --- 各タグの定義
                val attrStart = "<attribute>"
                val attrEnd = "</attribute>"
                val valStart = "<value>"
                val valEnd = "</value>"
                val enumStart = "<enum>"
                val enumEnd = "</enum>"

                // --- 「propertyIndex」以降からタグを探す
                val attrStartIndex = response.indexOf(attrStart, propertyIndex)
                val attrEndIndex = response.indexOf(attrEnd, propertyIndex)

                val valStartIndex = response.indexOf(valStart, propertyIndex)
                val valEndIndex = response.indexOf(valEnd, propertyIndex)

                val enumStartIndex = response.indexOf(enumStart, propertyIndex)
                val enumEndIndex = response.indexOf(enumEnd, propertyIndex)

                // --- すべての必須タグが正しく見つかったかチェック
                if (attrStartIndex in 0..<attrEndIndex &&
                    valStartIndex >= 0 && valEndIndex > valStartIndex &&
                    enumStartIndex >= 0 && enumEndIndex > enumStartIndex)
                {
                    // --- 文字列の切り出し
                    val attribute = response.substring(attrStartIndex + attrStart.length, attrEndIndex)
                    val currentValue = response.substring(valStartIndex + valStart.length, valEndIndex)

                    val propertyListString = response.substring(enumStartIndex + enumStart.length, enumEndIndex)
                    val selectionList = if (propertyListString.isNotEmpty())
                    {
                        propertyListString.split(" ")
                    }
                    else
                    {
                        emptyList()
                    }
                    // --- インスタンスを生成して返却
                    return ICameraStatus.CameraPropertyDescriptor(
                        propertyName = propertyName,
                        attribute = attribute,
                        current = currentValue,
                        values = selectionList
                    )
                }
            }
            // --- タグのパースに失敗した場合のログ
            Log.v(TAG, "getDescriptor(OMDS): Failed to parse XML tags for $propertyName. Response: $response")
        }
        catch (e: Exception)
        {
            Log.e(TAG, "Error in getDescriptor(OMDS) for property: $propertyName", e)
        }
        return defaultDescriptor
    }

    private fun getAvailableWhiteBalance(eventResponse: String) : List<String>
    {
        try
        {
            val wbValueList = getPropertySelectionList(eventResponse, "wbvalue")
            val wbItemList : ArrayList<String> = ArrayList()
            for (wbValue in wbValueList)
            {
                wbItemList.add(decideWhiteBalance(wbValue))
            }
            return (wbItemList)
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        return (ArrayList())
    }

    private fun decideWhiteBalance(wbValue: String) : String
    {
        try
        {
            return (when (wbValue)
            {
                "0" -> "AUTO"
                "18" -> "Daylight"
                "16" -> "Shade"
                "17" -> "Cloudy"
                "20" -> "Incandescent"
                "35" -> "Fluorescent"
                "64" -> "Underwater"
                "23" -> "Flash"
                "256" -> "WB1"
                "257" -> "WB2"
                "258" -> "WB3"
                "259" -> "WB4"
                "512" -> "CWB"
                else -> "($wbValue)"
            })
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        return ("($wbValue)")
    }

    private fun decideWhiteBalanceValue(wbName: String) : String
    {
        try
        {
            return (when (wbName)
            {
                "AUTO" -> "0"
                "Daylight" -> "18"
                "Shade" -> "16"
                "Cloudy" -> "17"
                "Incandescent" -> "20"
                "Fluorescent" -> "35"
                "Underwater" -> "64"
                "Flash" -> "23"
                "WB1" -> "256"
                "WB2" -> "257"
                "WB3" -> "258"
                "WB4" -> "259"
                "CWB" -> "512"
                else -> "0"
            })
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        return ("0")
    }

    private fun getPropertySelectionList(responseString: String, key: String) : List<String>
    {
        try
        {
            val propertyString = "<propname>$key</propname>"
            if (responseString.isNotEmpty())
            {
                val propertyIndex = responseString.indexOf(propertyString)
                if (propertyIndex > 0)
                {
                    val propertyValueIndex =
                        responseString.indexOf("<enum>", propertyIndex) + "<enum>".length
                    val propertyValueLastIndex = responseString.indexOf("</enum>", propertyIndex)
                    val propertyListString =
                        responseString.substring(propertyValueIndex, propertyValueLastIndex)
                    if (propertyListString.isNotEmpty())
                    {
                        val propertyList = propertyListString.split(" ")
                        val selectionList: ArrayList<String> = ArrayList()
                        selectionList.addAll(propertyList)
                        return (selectionList)
                    }
                }
            }
            Log.v(TAG, "getPropertySelectionList($propertyString) $responseString ..." )
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        return (ArrayList())
    }

    private fun sendGetPropertyDescriptionRequest(property: String): String
    {
        val requestUrl = "$executeUrl/get_camprop.cgi?com=desc&propname=$property"
        val response: String = http.httpGetWithHeader(requestUrl, headerMap, null,
            TIMEOUT_MS
        ) ?: ""
        dumpLog(requestUrl, response)
        return response
    }

    private fun sendSetPropertyRequest(property: String, value: String)
    {
        val requestUrl = "$executeUrl/set_camprop.cgi?com=set&propname=$property"
        val postData = "<?xml version=\"1.0\"?><set><value>$value</value></set>"
        val response: String = http.httpPostWithHeader(requestUrl, postData, headerMap, null, TIMEOUT_MS) ?: ""
        dumpLog(requestUrl, response)
    }

    private fun dumpLog(header: String, data: String)
    {
        if (DUMP_LOG)
        {
            val dataStep = 1536
            Log.v(TAG, "     ------------------------------------------ ")
            for (pos in 0..data.length step dataStep) {
                val lastIndex = if ((pos + dataStep) > data.length)
                {
                    data.length
                }
                else
                {
                    pos + dataStep
                }
                Log.v(TAG, " $header ($pos/${data.length}) ${data.substring(pos, lastIndex)}")
            }
            Log.v(TAG, "     ------------------------------------------ ")
        }
    }

    init
    {
        headerMap["User-Agent"] = userAgent // "OlympusCameraKit" // "OI.Share"
        headerMap["X-Protocol"] = userAgent // "OlympusCameraKit" // "OI.Share"
    }

    companion object
    {
        private val TAG = OmdsCameraProperties::class.java.simpleName

        // TIMEOUT VALUES
        private const val TIMEOUT_MS = 2500

        private const val DUMP_LOG = false
    }
}