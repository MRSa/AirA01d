package jp.osdn.gokigen.a01lib.camera.omds.operation

import android.util.Log
import jp.osdn.gokigen.a01lib.camera.interfaces.IOperationCallback
import jp.osdn.gokigen.a01lib.camera.utils.communication.SimpleHttpClient
import java.lang.Exception
import java.util.HashMap

class OmdsCamIndStatus(userAgent: String = "OlympusCameraKit", private val executeUrl : String = "http://192.168.0.10")
{
    private val headerMap: MutableMap<String, String> = HashMap()
    private val http = SimpleHttpClient()

    fun getCamInState(callback: IOperationCallback)
    {
        try
        {
            Log.v(TAG, " getCamInState")
            val thread = Thread {
                val camInStateUrl = "$executeUrl/get_camindstate.cgi"
                val response: String = http.httpGetWithHeader(camInStateUrl, headerMap, null, TIMEOUT_MS) ?: ""
                Log.v(TAG, " $camInStateUrl $response")
                val message = "fwversion ${pickupValue("fwversion", response)}\nlensfwversion ${pickupValue("lensfwversion", response)}\n"
                callback.operationResult(true, message)
                Log.v(TAG, message)
            }
            thread.start()
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
        private val TAG = OmdsCamIndStatus::class.java.simpleName
        private const val TIMEOUT_MS = 5000
    }
}
