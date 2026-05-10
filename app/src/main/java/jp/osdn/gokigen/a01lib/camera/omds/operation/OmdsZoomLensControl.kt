package jp.osdn.gokigen.a01lib.camera.omds.operation

import android.util.Log
import jp.osdn.gokigen.a01lib.camera.interfaces.IZoomLensControl
import jp.osdn.gokigen.a01lib.camera.utils.communication.SimpleHttpClient
import java.lang.Exception
import java.util.HashMap

class OmdsZoomLensControl(userAgent: String = "OlympusCameraKit", private val executeUrl : String = "http://192.168.0.10", private val useOpcProtocol: Boolean = true) : IZoomLensControl
{
    private val headerMap: MutableMap<String, String> = HashMap()
    private val http = SimpleHttpClient()
    private var isZooming = false

    override fun canZoom(): Boolean
    {
        Log.v(TAG, "canZoom()")
        return true
    }

    override fun updateStatus()
    {
        Log.v(TAG, "updateStatus()")
    }

    override fun getMaximumFocalLength(): Float
    {
        Log.v(TAG, "getMaximumFocalLength()")
        return (0.0f)
    }

    override fun getMinimumFocalLength(): Float
    {
        Log.v(TAG, "getMinimumFocalLength()")
        return (0.0f)
    }

    override fun getCurrentFocalLength(): Float
    {
        Log.v(TAG, "getCurrentFocalLength()")
        return (0.0f)
    }

    override fun driveZoomLens(targetLength: Float)
    {
        // ----- 焦点距離を指定してズームを動かす
        val targetLengthInt = targetLength.toInt()
        Log.v(TAG, "driveZoomLens() : ${targetLengthInt}mm")
        try {
            val thread = Thread {
                try
                {
                    val command: String = if (useOpcProtocol)
                    {
                        "/exec_takemisc.cgi?com=newctrlzoom&ctrl=start&dir=fix&focallen=$targetLengthInt"
                    }
                    else
                    {
                        "/exec_takemisc.cgi?com=ctrlzoom&move=start&dir=fix&focallen=$targetLengthInt"
                    }
                    http.httpGetWithHeader(
                        executeUrl + command,
                        headerMap,
                        null,
                        TIMEOUT_MS
                    ) ?: ""
                }
                catch (e: Exception)
                {
                    e.printStackTrace()
                }
            }
            thread.start()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    override fun moveInitialZoomPosition()
    {
        Log.v(TAG, "moveInitialZoomPosition()")
    }

    override fun isDrivingZoomLens(): Boolean
    {
        Log.v(TAG, "isDrivingZoomLens()")
        return false
    }

    override fun driveZoomLens(isZoomIn: Boolean) {
        Log.v(TAG, "driveZoomLens() : $isZoomIn")
        try {
            val thread = Thread {
                try {
                    val command: String = if (isZooming)
                    {
                        if (useOpcProtocol)
                        {
                            "/exec_takemisc.cgi?com=newctrlzoom&ctrl=stop"
                        }
                        else
                        {
                            "/exec_takemisc.cgi?com=ctrlzoom&move=off"
                        }
                    }
                    else
                    {
                        if (isZoomIn)
                        {
                            if (useOpcProtocol)
                            {
                                "/exec_takemisc.cgi?com=newctrlzoom&ctrl=start&dir=tele"
                            }
                            else
                            {
                                "/exec_takemisc.cgi?com=ctrlzoom&move=telemove"
                            }
                        }
                        else
                        {
                            if (useOpcProtocol)
                            {
                                "/exec_takemisc.cgi?com=newctrlzoom&ctrl=start&dir=wide"
                            }
                            else
                            {
                                "/exec_takemisc.cgi?com=ctrlzoom&move=widemove"
                             }

                       }
                    }
                    val reply: String = http.httpGetWithHeader(
                        executeUrl + command,
                        headerMap,
                        null,
                        TIMEOUT_MS
                    ) ?: ""
                    isZooming = !isZooming
                    Log.v(TAG, "ZOOM : $isZooming cmd : $command  RET : $reply")
                }
                catch (e: Exception)
                {
                    e.printStackTrace()
                }
            }
            thread.start()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    companion object
    {
        private val TAG = OmdsZoomLensControl::class.java.simpleName
        private const val TIMEOUT_MS = 3000
    }

    init
    {
        headerMap["User-Agent"] = userAgent // "OlympusCameraKit" // "OI.Share"
        headerMap["X-Protocol"] = userAgent // "OlympusCameraKit" // "OI.Share"
    }
}
