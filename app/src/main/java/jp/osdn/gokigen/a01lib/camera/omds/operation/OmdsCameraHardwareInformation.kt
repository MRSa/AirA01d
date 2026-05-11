package jp.osdn.gokigen.a01lib.camera.omds.operation

import android.util.Log
import jp.osdn.gokigen.a01lib.camera.interfaces.ICameraHardwareInformation
import jp.osdn.gokigen.a01lib.camera.interfaces.ICameraHardwareInformation.Hardware
import jp.osdn.gokigen.a01lib.camera.utils.communication.SimpleHttpClient
import java.util.HashMap
import kotlin.Exception

class OmdsCameraHardwareInformation(userAgent: String = "OlympusCameraKit", private val executeUrl : String = "http://192.168.0.10"): ICameraHardwareInformation
{
    private val headerMap: MutableMap<String, String> = HashMap()
    private val http = SimpleHttpClient()

    override fun getCameraStatus(callback: ICameraHardwareInformation.Callback)
    {
        val result = mutableMapOf<String, String>()
        try
        {
            val thread = Thread {
                try
                {
                    //  カメラのステータスを取得する
                    val getStatusUrl = "$executeUrl/get_state.cgi"
                    val response: String = http.httpGetWithHeader(getStatusUrl, headerMap, null, TIMEOUT_MS) ?: ""
                    //Log.v(TAG, "/get_state.cgi RESP: (${response.length}) $response")
                    result[Hardware.CARDSTATUS] = pickupValue(Hardware.CARDSTATUS, response)
                    result[Hardware.CARDREMAINNUM] = pickupValue(Hardware.CARDREMAINNUM, response)
                    result[Hardware.CARDREMAINSEC] = pickupValue(Hardware.CARDREMAINSEC, response)
                    result[Hardware.CARDREMAINBYTE] = pickupValue(Hardware.CARDREMAINBYTE, response)
                    result[Hardware.LENSMOUNTSTATUS] = pickupValue(Hardware.LENSMOUNTSTATUS, response)
                    result[Hardware.IMAGINGSTATE] = pickupValue(Hardware.IMAGINGSTATE, response)
                    result[Hardware.FOCALLENGTH] = pickupValue(Hardware.FOCALLENGTH, response)
                    result[Hardware.WIDEFOCALLENGTH] = pickupValue(Hardware.WIDEFOCALLENGTH, response)
                    result[Hardware.TELEFOCALLENGTH] = pickupValue(Hardware.TELEFOCALLENGTH, response)
                    result[Hardware.ELECTRICZOOM] = pickupValue(Hardware.ELECTRICZOOM, response)
                    result[Hardware.MACROSETTING] = pickupValue(Hardware.MACROSETTING, response)
                }
                catch (e: Exception)
                {
                    e.printStackTrace()
                }
                callback.operationResult(result)
            }
            thread.start()
        }
        catch (e: Exception)
        {
            Log.v(TAG, "getCameraStatus()")
            e.printStackTrace()
        }
        return
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
        private val TAG = OmdsCameraHardwareInformation::class.java.simpleName
        private const val TIMEOUT_MS = 5000
    }
}
