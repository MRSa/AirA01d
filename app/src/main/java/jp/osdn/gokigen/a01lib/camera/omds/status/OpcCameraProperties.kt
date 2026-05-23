package jp.osdn.gokigen.a01lib.camera.omds.status

import android.util.Log
import jp.osdn.gokigen.a01lib.camera.interfaces.ICameraStatus
import jp.osdn.gokigen.a01lib.camera.interfaces.ICameraStatus.CameraProperty
import jp.osdn.gokigen.a01lib.camera.utils.communication.SimpleHttpClient
import java.util.HashMap
import kotlin.String

class OpcCameraProperties(
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
                CameraProperty.TakeMode -> sendSetPropertyRequest("TAKEMODE", value)
                CameraProperty.ShutterSpeed -> sendSetPropertyRequest("SHUTTER", value)
                CameraProperty.Aperture -> sendSetPropertyRequest("APERTURE", value)
                CameraProperty.IsoSensitivity -> sendSetPropertyRequest("ISO", value)
                CameraProperty.ExposureCompensation -> sendSetPropertyRequest("EXPREV", value)
                CameraProperty.FocusMode -> sendSetPropertyRequest("FOCUS_STILL", value)
                CameraProperty.WhiteBalance -> sendSetPropertyRequest("WB", value)
                CameraProperty.PictureEffect -> sendSetPropertyRequest("COLORTONE", value)
                CameraProperty.DriveMode -> sendSetPropertyRequest("TAKE_DRIVE", value)
                CameraProperty.RawMode -> sendSetPropertyRequest("RAW", value)
                CameraProperty.AspectRatio -> sendSetPropertyRequest("ASPECT_RATIO", value)
                CameraProperty.MeteringMode -> sendSetPropertyRequest("AE", value)
                CameraProperty.AfLockState -> sendSetPropertyRequest("AF_LOCK_STATE", value)
                CameraProperty.AeLockState -> sendSetPropertyRequest("AE_LOCK_STATE", value)
                CameraProperty.BatteryRemain -> sendSetPropertyRequest("BATTERY_LEVEL", value)
                CameraProperty.ArtFilter -> sendSetPropertyRequest("RECENTLY_ART_FILTER", value)
                else -> { Log.v(TAG, " Not support Property key(SET) : $key") }
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
            Log.v(TAG, " getStatusListOpc($key)")
            val propertyKey =  when (key) {
                CameraProperty.TakeMode -> "TAKEMODE"
                CameraProperty.ShutterSpeed -> "SHUTTER"
                CameraProperty.Aperture -> "APERTURE"
                CameraProperty.IsoSensitivity -> "ISO"
                CameraProperty.ExposureCompensation -> "EXPREV"
                CameraProperty.FocusMode -> "FOCUS_STILL"
                CameraProperty.WhiteBalance -> "WB"
                CameraProperty.PictureEffect -> "COLORTONE"
                CameraProperty.DriveMode -> "TAKE_DRIVE"
                CameraProperty.RawMode -> "RAW"
                CameraProperty.AspectRatio -> "ASPECT_RATIO"
                CameraProperty.MeteringMode -> "AE"
                CameraProperty.AfLockState -> "AF_LOCK_STATE"
                CameraProperty.AeLockState -> "AE_LOCK_STATE"
                CameraProperty.BatteryRemain -> "BATTERY_LEVEL"
                CameraProperty.ArtFilter -> "RECENTLY_ART_FILTER"
                else -> ""
            }
            if (propertyKey.isNotEmpty())
            {
                val response = sendGetPropertyDescriptionRequest(propertyKey)
                return (getPropertySelectionList(response, propertyKey))
            }
            Log.v(TAG, " Not support Property key(GET) : $key")
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
            Log.v(TAG, "getPropertyDescriptor: Failed to parse XML tags for $propertyName. Response: $response")
        }
        catch (e: Exception)
        {
            Log.e(TAG, "Error in getPropertyDescriptor for property: $propertyName", e)
        }
        return defaultDescriptor
    }

    private fun getPropertySelectionList(responseString: String, key: String): List<String>
    {
        val propertyString = "<propname>$key</propname>"
        if (responseString.isEmpty()) return emptyList()  // 文字列が空なら即座に空リストを返す
        try
        {
            val propertyIndex = responseString.indexOf(propertyString)
            if (propertyIndex >= 0)
            {
                // --- タグのインデックスを検索
                val enumStartTag = "<enum>"
                val enumEndTag = "</enum>"
                val startIndex = responseString.indexOf(enumStartTag, propertyIndex)
                val endIndex = responseString.indexOf(enumEndTag, propertyIndex)

                // 両方のタグが正しく見つかり、かつ位置関係が正常な場合のみ処理
                if (startIndex in 0..<endIndex)
                {
                    val propertyValueIndex = startIndex + enumStartTag.length
                    val propertyListString = responseString.substring(propertyValueIndex, endIndex)
                    if (propertyListString.isNotEmpty())
                    {
                        return propertyListString.split(" ")
                    }
                }
            }
            // 処理が正常に完了しなかった場合...
            Log.v(TAG, "getPropertySelectionList($propertyString) failed or empty. Response: $responseString")
        }
        catch (e: Exception)
        {
            Log.e(TAG, "Error in getPropertySelectionList for key: $key", e)
        }
        return emptyList()
    }

    private fun sendSetPropertyRequest(property: String, value: String)
    {
        val requestUrl = "$executeUrl/set_camprop.cgi?com=set&propname=$property"
        val postData = "<?xml version=\"1.0\"?><set><value>$value</value></set>"
        val response: String = http.httpPostWithHeader(requestUrl, postData, headerMap, null, TIMEOUT_MS) ?: ""
        dumpLog(requestUrl, response)
    }

    private fun sendGetPropertyDescriptionRequest(property: String): String
    {
        val requestUrl = "$executeUrl/get_camprop.cgi?com=desc&propname=$property"
        val response: String = http.httpGetWithHeader(requestUrl, headerMap, null, TIMEOUT_MS) ?: ""
        dumpLog(requestUrl, response)
        return response
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
        private val TAG = OpcCameraProperties::class.java.simpleName
        private const val TIMEOUT_MS = 2500
        private const val DUMP_LOG = false
    }
}