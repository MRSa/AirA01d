package jp.osdn.gokigen.a01lib.camera.omds.operation

import android.util.Log
import jp.osdn.gokigen.a01lib.camera.utils.communication.SimpleHttpClient
import java.lang.Exception
import java.util.HashMap

class OmdsRunModeControl(private val liveViewQuality : String = "0640x0480", userAgent: String = "OlympusCameraKit", private val executeUrl : String = "http://192.168.0.10", private val dump_log : Boolean = DUMP_LOG)
{
    private val headerMap: MutableMap<String, String> = HashMap()
    private val http = SimpleHttpClient()
    private var currentRunMode = "unknown"
    private var useOpcProtocol : Boolean = true

    fun setUseOpcProtocol(isOpcProtocol: Boolean)
    {
        useOpcProtocol = isOpcProtocol
    }

    fun changeRunMode(runMode: String): Boolean
    {
        var isChange = false
        try
        {
            val command = if (useOpcProtocol) { CHANGE_MODE_COMMAND_OPC } else { CHANGE_MODE_COMMAND }
            val changeModeUrl = when (runMode) {
                "rec" -> {
                    "$executeUrl/$command?mode=$runMode&lvqty=$liveViewQuality"  // OI.Shareの場合は cammode
                }
                else -> {
                    "$executeUrl/$command?mode=$runMode"                        // OI.Shareの場合は cammode
                }
            }
            val response: String = http.httpGetWithHeader(changeModeUrl, headerMap, null, TIMEOUT_MS) ?: ""
            if (dump_log) {
                Log.v(TAG, " $changeModeUrl $response")
            }
            var message = ""
            try
            {
                if (((useOpcProtocol)&&(response.contains("OK"))||(response.contains("ok")))||
                    ((!useOpcProtocol)&&(response.contains("200"))))
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
            if (dump_log)
            {
                Log.v(TAG, " changeRunMode [$runMode] $message")
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        return isChange
    }

    fun getRunMode() : String
    {
        if (dump_log)
        {
            Log.v(TAG, " getRunMode() [$currentRunMode]")
        }
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

        private const val CHANGE_MODE_COMMAND = "switch_cammode.cgi"
        private const val CHANGE_MODE_COMMAND_OPC = "switch_cameramode.cgi"
        private const val TIMEOUT_MS = 5000
        private const val DUMP_LOG = false
    }
}
