package jp.osdn.gokigen.a01lib.camera.omds.operation

import android.util.Log
import jp.osdn.gokigen.a01lib.camera.interfaces.IOperationCallback
import jp.osdn.gokigen.a01lib.camera.utils.communication.SimpleHttpClient
import java.util.Date
import java.util.HashMap
import kotlin.Exception

class OmdsCameraStatus(userAgent: String = "OlympusCameraKit", private val executeUrl : String = "http://192.168.0.10")
{
    private val headerMap: MutableMap<String, String> = HashMap()
    private val http = SimpleHttpClient()

    fun getCameraStatus(callback: IOperationCallback)
    {
        try
        {
            val thread = Thread {
                //  ステータスを取得する
                val getStatusUrl = "$executeUrl/get_state.cgi"
                val response: String = http.httpGetWithHeader(getStatusUrl, headerMap, null, TIMEOUT_MS) ?: ""
                Log.v(TAG, "RESP: (${response.length}) $response")
                callback.operationResult(true, response)
                parseReceivedStatus(response)
            }
            thread.start()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun parseReceivedStatus(response: String)
    {
        try
        {
            val currentTime = Date(System.currentTimeMillis())
            val cardStatus = pickupValue("cardstatus", response)
            val cardRemainNum = pickupValue("cardremainnum", response)
            val cardRemainSec = pickupValue("cardremainsec", response)
            val cardRemainByte = pickupValue("cardremainbyte", response)
            val lensMountStatus = pickupValue("lensmountstatus", response)
            val imagingState = pickupValue("imagingstate", response)

            val focalLength = pickupValue("focallength", response)
            val wideFocalLength = pickupValue("widefocallength", response)
            val teleFocalLength = pickupValue("telefocallength", response)
            val electricZoom = pickupValue("electriczoom", response)
            val macroSetting = pickupValue("macrosetting", response)
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun pickupValue(tagName: String, data: String): String
    {
        var value = ""
        try
        {
            val startTag = "<$tagName>"
            val endTag = "</$tagName>"

            val startPosition = data.indexOf(startTag) + startTag.length
            val endPosition = data.indexOf(endTag)
            if ((startPosition >= 0)&&(endPosition > 0))
            {
                value = data.substring(startPosition, endPosition)
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        return (value)
    }

    init
    {
        headerMap["User-Agent"] = userAgent // "OlympusCameraKit" // "OI.Share"
        headerMap["X-Protocol"] = userAgent // "OlympusCameraKit" // "OI.Share"
    }

    companion object
    {
        private val TAG = OmdsCameraStatus::class.java.simpleName
        private const val TIMEOUT_MS = 5000
    }
}
