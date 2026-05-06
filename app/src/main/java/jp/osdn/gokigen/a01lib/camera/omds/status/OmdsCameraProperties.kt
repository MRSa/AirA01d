package jp.osdn.gokigen.a01lib.camera.omds.status

import android.util.Log
import jp.osdn.gokigen.a01lib.camera.interfaces.ICameraStatus.CameraProperty
import jp.osdn.gokigen.a01lib.camera.utils.communication.SimpleHttpClient
import java.util.HashMap
import kotlin.collections.set

class OmdsCameraProperties(
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
                CameraProperty.TakeMode ->  sendStatusRequest("takemode", value)
                CameraProperty.ShutterSpeed ->  sendStatusRequest("shutspeedvalue", value)
                CameraProperty.Aperture ->  sendStatusRequest("focalvalue", value)
                CameraProperty.ExposureCompensation ->  sendStatusRequest("expcomp", value)
                CameraProperty.IsoSensitivity ->  sendStatusRequest("isospeedvalue", value)
                CameraProperty.DriveMode ->  sendStatusRequest("drivemode", value)
                CameraProperty.WhiteBalance ->  sendStatusRequest("wbvalue", decideWhiteBalanceValue(value))
                CameraProperty.PictureEffect ->  sendStatusRequest("colortone", value)
                else -> { }
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
            Log.v(TAG, " getStatusListOmds($key)")
            return (when (key) {
                CameraProperty.TakeMode -> getPropertySelectionList(sendGetPropertyDescriptionRequest("takemode"), "takemode")
                CameraProperty.ShutterSpeed -> getPropertySelectionList(sendGetPropertyDescriptionRequest("shutspeedvalue"), "shutspeedvalue")
                CameraProperty.Aperture -> getPropertySelectionList(sendGetPropertyDescriptionRequest("focalvalue"), "focalvalue")
                CameraProperty.IsoSensitivity -> getPropertySelectionList(sendGetPropertyDescriptionRequest("isospeedvalue"), "isospeedvalue")
                CameraProperty.ExposureCompensation -> getPropertySelectionList(sendGetPropertyDescriptionRequest("expcomp"), "expcomp")
                CameraProperty.WhiteBalance -> getAvailableWhiteBalance(sendGetPropertyDescriptionRequest("wbvalue"))
                CameraProperty.PictureEffect -> getPropertySelectionList(sendGetPropertyDescriptionRequest("colortone"), "colortone")
                CameraProperty.DriveMode -> getPropertySelectionList(sendGetPropertyDescriptionRequest("drivemode"), "drivemode")
                else -> ArrayList()
            })
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        return (ArrayList())
    }

    private fun getAvailableWhiteBalance(eventResponse: String) : List<String>
    {
        try
        {
            val wbValueList = getPropertySelectionList(eventResponse, "wbvalue")
            val wbItemList : ArrayList<String> = ArrayList()
            for (wbValue in wbValueList)
            {
                wbItemList.add(decideWhiteBalance(wbValue))
            }
            return (wbItemList)
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        return (ArrayList())
    }

    private fun decideWhiteBalance(wbValue: String) : String
    {
        try
        {
            return (when (wbValue)
            {
                "0" -> "AUTO"
                "18" -> "Daylight"
                "16" -> "Shade"
                "17" -> "Cloudy"
                "20" -> "Incandescent"
                "35" -> "Fluorescent"
                "64" -> "Underwater"
                "23" -> "Flash"
                "256" -> "WB1"
                "257" -> "WB2"
                "258" -> "WB3"
                "259" -> "WB4"
                "512" -> "CWB"
                else -> "($wbValue)"
            })
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        return ("($wbValue)")
    }

    private fun decideWhiteBalanceValue(wbName: String) : String
    {
        try
        {
            return (when (wbName)
            {
                "AUTO" -> "0"
                "Daylight" -> "18"
                "Shade" -> "16"
                "Cloudy" -> "17"
                "Incandescent" -> "20"
                "Fluorescent" -> "35"
                "Underwater" -> "64"
                "Flash" -> "23"
                "WB1" -> "256"
                "WB2" -> "257"
                "WB3" -> "258"
                "WB4" -> "259"
                "CWB" -> "512"
                else -> "0"
            })
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        return ("0")
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

    private fun sendGetPropertyDescriptionRequest(property: String): String
    {
        val requestUrl = "$executeUrl/get_camprop.cgi?com=desc&propname=$property"
        val response: String = http.httpGetWithHeader(requestUrl, headerMap, null,
            TIMEOUT_MS
        ) ?: ""
        dumpLog(requestUrl, response)
        return response
    }

    private fun sendStatusRequest(property: String, value: String)
    {
        val requestUrl = "$executeUrl/set_camprop.cgi?com=set&propname=$property"
        val postData = "<?xml version=\"1.0\"?><set><value>$value</value></set>"
        val response: String = http.httpPostWithHeader(requestUrl, postData, headerMap, null, TIMEOUT_MS) ?: ""
        dumpLog(requestUrl, response)
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
        private val TAG = OmdsCameraStatusWatcher::class.java.simpleName

        // TIMEOUT VALUES
        private const val TIMEOUT_MS = 2500

        private const val DUMP_LOG = false
    }
}