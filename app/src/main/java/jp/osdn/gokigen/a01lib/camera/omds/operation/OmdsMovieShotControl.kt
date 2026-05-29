package jp.osdn.gokigen.a01lib.camera.omds.operation

import android.util.Log
import jp.osdn.gokigen.a01lib.camera.utils.communication.SimpleHttpClient
import java.lang.Exception
import java.util.HashMap

class OmdsMovieShotControl(userAgent: String = "OlympusCameraKit", private val executeUrl : String = "http://192.168.0.10")
{
    private val headerMap: MutableMap<String, String> = HashMap()
    private val http = SimpleHttpClient()
    private var useOpcProtocol : Boolean = true

    fun setUseOpcProtocol(isOpcProtocol: Boolean)
    {
        useOpcProtocol = isOpcProtocol
    }

    fun movieAction(isStop: Boolean)
    {
        Log.v(TAG, "movieAction() isStop: $isStop")
        try
        {
            val sendUrl = if (isStop) { if (useOpcProtocol) { executeUrl + CAPTURE_STOP_COMMAND_OPC } else { executeUrl + CAPTURE_STOP_COMMAND } } else { if (useOpcProtocol) { executeUrl + CAPTURE_COMMAND_OPC } else { executeUrl + CAPTURE_COMMAND } }
            http.httpGetWithHeader(sendUrl, headerMap, null, TIMEOUT_MS)?.lowercase() ?: ""
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    companion object
    {
        private val TAG: String = OmdsMovieShotControl::class.java.simpleName
        private const val TIMEOUT_MS = 3000
        private const val CAPTURE_COMMAND = "/exec_takemotion.cgi?com=startmovietake"
        private const val CAPTURE_COMMAND_OPC = "/exec_takemotion.cgi?com=newstartmovietake"
        private const val CAPTURE_STOP_COMMAND = "/exec_takemotion.cgi?com=stopmovietake"
        private const val CAPTURE_STOP_COMMAND_OPC = "/exec_takemotion.cgi?com=newstopmovietake"
    }

    init
    {
        headerMap["User-Agent"] = userAgent // "OlympusCameraKit" // "OI.Share"
        headerMap["X-Protocol"] = userAgent // "OlympusCameraKit" // "OI.Share"
    }
}
