package jp.osdn.gokigen.a01lib.camera.omds.status

import android.util.Log
import jp.osdn.gokigen.a01lib.camera.interfaces.ICameraStatus.CameraProperty
import jp.osdn.gokigen.a01lib.camera.utils.communication.SimpleHttpClient
import java.util.HashMap

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
                else -> { Log.v(TAG, " Not support Property key(SET) : $key") }
            }
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

    private fun getPropertySelectionList(responseString: String, key: String) : List<String>
    {
        try
        {
            val propertyString = "<propname>$key</propname>"
            if (responseString.isNotEmpty())
            {
                val propertyIndex = responseString.indexOf(propertyString)
                if (propertyIndex > 0)
                {
                    val propertyValueIndex =
                        responseString.indexOf("<enum>", propertyIndex) + "<enum>".length
                    val propertyValueLastIndex = responseString.indexOf("</enum>", propertyIndex)
                    val propertyListString =
                        responseString.substring(propertyValueIndex, propertyValueLastIndex)
                    if (propertyListString.isNotEmpty())
                    {
                        val propertyList = propertyListString.split(" ")
                        val selectionList: ArrayList<String> = ArrayList()
                        selectionList.addAll(propertyList)
                        return (selectionList)
                    }
                }
            }
            Log.v(TAG, "getPropertySelectionList($propertyString) $responseString ..." )
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        return (ArrayList())
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