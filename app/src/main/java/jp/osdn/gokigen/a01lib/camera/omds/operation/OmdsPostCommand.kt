package jp.osdn.gokigen.a01lib.camera.omds.operation

import android.util.Log
import jp.osdn.gokigen.a01lib.camera.interfaces.IOperationCallback
import jp.osdn.gokigen.a01lib.camera.utils.communication.SimpleHttpClient
import java.lang.Exception
import java.util.HashMap

class OmdsPostCommand(userAgent: String = "OlympusCameraKit", private val executeUrl : String = "http://192.168.0.10")
{
    private val headerMap: MutableMap<String, String> = HashMap()
    private val http = SimpleHttpClient()

    fun sendCommand(commandCgi: String, parameter: String, body: String, callback: IOperationCallback)
    {
        try
        {
            Log.v(TAG, " OmdsCommands [$commandCgi] [$parameter] [$body]")
            val thread = Thread {
                val commandUrl = if (parameter.length > 1) {
                    "$executeUrl/$commandCgi?$parameter"
                } else {
                    "$executeUrl/$commandCgi"
                }
                val response: String = http.httpPostWithHeader(commandUrl, body, headerMap, null, TIMEOUT_MS) ?: ""
                Log.v(TAG, " $commandUrl $response")
                var isChange = false
                try
                {
                    isChange = (response.contains("OK"))||(response.contains("ok"))
                }
                catch (e: Exception)
                {
                    e.printStackTrace()
                }
                callback.operationResult(isChange, response)
            }
            thread.start()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    init
    {
        headerMap["User-Agent"] = userAgent // "OlympusCameraKit" or "OI.Share"
        headerMap["X-Protocol"] = userAgent // "OlympusCameraKit" or "OI.Share"
    }

    companion object
    {
        private val TAG = OmdsPostCommand::class.java.simpleName
        private const val TIMEOUT_MS = 5000
    }
}
