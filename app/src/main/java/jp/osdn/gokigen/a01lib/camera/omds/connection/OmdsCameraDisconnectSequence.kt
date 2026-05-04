package jp.osdn.gokigen.a01lib.camera.omds.connection

import android.util.Log
import jp.osdn.gokigen.a01lib.camera.interfaces.ICameraConnectionStatus
import jp.osdn.gokigen.a01lib.camera.utils.communication.SimpleHttpClient
import java.lang.Exception
import java.util.HashMap

class OmdsCameraDisconnectSequence(private val powerOff: Boolean, private val cameraStatusReceiver: ICameraConnectionStatus, userAgent : String = "OlympusCameraKit", private val  executeUrl : String = "http://192.168.0.10") : Runnable
{
    private val headerMap: MutableMap<String, String> = HashMap()
    private val http = SimpleHttpClient()

    override fun run()
    {
        // カメラをPowerOffして接続を切る
        try
        {
            if (powerOff)
            {
                val cameraPowerOffUrl = "$executeUrl/exec_pwoff.cgi"
                val response: String = http.httpGetWithHeader(cameraPowerOffUrl, headerMap, null, TIMEOUT_MS) ?: ""
                Log.v(TAG, " $cameraPowerOffUrl $response")
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        finally
        {
            cameraStatusReceiver.onStatusNotify(ICameraConnectionStatus.CameraConnectionStatus.DISCONNECTED)
        }
    }

    init
    {
        headerMap["User-Agent"] = userAgent // "OlympusCameraKit" // or "OI.Share"
        headerMap["X-Protocol"] = userAgent // "OlympusCameraKit" // or "OI.Share"
    }

    companion object
    {
        private val TAG = OmdsCameraDisconnectSequence::class.java.simpleName
        private const val TIMEOUT_MS = 5000
    }
}
