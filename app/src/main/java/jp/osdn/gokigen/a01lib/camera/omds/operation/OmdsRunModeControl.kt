package jp.osdn.gokigen.a01lib.camera.omds.operation

import android.util.Log
import jp.osdn.gokigen.a01lib.camera.interfaces.IOperationCallback
import jp.osdn.gokigen.a01lib.camera.utils.communication.SimpleHttpClient
import java.lang.Exception
import java.util.HashMap

class OmdsRunModeControl(private val liveViewQuality : String = "0640x0480", userAgent: String = "OlympusCameraKit", private val executeUrl : String = "http://192.168.0.10")
{
    private val headerMap: MutableMap<String, String> = HashMap()
    private val http = SimpleHttpClient()
    private var currentRunMode = "unknown"

    fun changeRunMode(runMode: String, callback: IOperationCallback)
    {
        try
        {
            Log.v(TAG, " changeRunMode [$runMode]")
            val thread = Thread { // カメラとの接続確立を通知する
                val changeModeUrl = when (runMode) {
                    "rec" -> {
                        "$executeUrl/switch_cameramode.cgi?mode=$runMode&lvqty=$liveViewQuality"  // OI.Shareの場合は cammode
                    }
                    else -> {
                        "$executeUrl/switch_cameramode.cgi?mode=$runMode"                        // OI.Shareの場合は cammode
                    }
                }
                val response: String = http.httpGetWithHeader(changeModeUrl, headerMap, null, TIMEOUT_MS) ?: ""
                Log.v(TAG, " $changeModeUrl $response")
                var message = ""
                var isChange = false
                try
                {
                    if ((response.contains("OK"))||(response.contains("ok")))
                    {
                        currentRunMode = runMode
                        isChange = true
                        message = "success: $currentRunMode"
                    }
                    else
                    {
                        isChange = false
                        message = "error: $response"
                    }
                }
                catch (e: Exception)
                {
                    e.printStackTrace()
                }
                callback.operationResult(isChange, message)
                Log.v(TAG, message)
            }
            thread.start()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    fun getRunMode() : String
    {
        return currentRunMode
    }

    init
    {
        headerMap["User-Agent"] = userAgent // "OlympusCameraKit" // "OI.Share"
        headerMap["X-Protocol"] = userAgent // "OlympusCameraKit" // "OI.Share"
    }

    companion object
    {
        private val TAG = OmdsRunModeControl::class.java.simpleName
        private const val TIMEOUT_MS = 5000
    }
}
