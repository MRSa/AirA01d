package jp.osdn.gokigen.a01lib.camera.omds.status

import android.util.Log
import jp.osdn.gokigen.a01lib.camera.interfaces.ICameraStatus
import jp.osdn.gokigen.a01lib.camera.interfaces.ICameraStatusUpdateNotify
import jp.osdn.gokigen.a01lib.camera.utils.communication.SimpleHttpClient
import java.util.HashMap

class OpcEventStatusWatch(
    private val statusProvider: ICameraStatusUpdateNotify,
    userAgent: String = "OlympusCameraKit",
    private val executeUrl : String = "http://192.168.0.10",
)
{
    private val headerMap: MutableMap<String, String> = HashMap()
    private val http = SimpleHttpClient()
    private val currentStatuses = mutableMapOf<ICameraStatus.CameraProperty, String?>()
    fun getStatus(key: ICameraStatus.CameraProperty): String {
        return (this.currentStatuses[key] ?: "")
    }

    fun watchOpcStatus()
    {
        try
        {
            // OPC機のイベント受信
            val opcEventUrl = "$executeUrl/get_camprop.cgi?com=getlist"
            val postData = "<?xml version=\"1.0\"?><get><prop name=\"AE\"/><prop name=\"APERTURE\"/><prop name=\"BATTERY_LEVEL\"/><prop name=\"COLORTONE\"/><prop name=\"EXPREV\"/><prop name=\"ISO\"/><prop name=\"RECENTLY_ART_FILTER\"/><prop name=\"SHUTTER\"/><prop name=\"TAKEMODE\"/><prop name=\"TAKE_DRIVE\"/><prop name=\"WB\"/><prop name=\"AE_LOCK_STATE\"/><prop name=\"AF_LOCK_STATE\"/></get>"
            val eventResponse = http.httpPostWithHeader(opcEventUrl, postData, headerMap, null,
                TIMEOUT_MS
            ) ?: ""
            dumpLog(opcEventUrl, eventResponse)
            parseOpcProperties(eventResponse)
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun parseOpcProperties(eventResponse: String)
    {
        try
        {
            if (DUMP_LOG)
            {
                Log.v(TAG, "----- parseOpcProperties : $eventResponse")
            }

            // ----- 受信したプロパティを解釈して保管
            checkTakeMode(getPropertyValue(eventResponse, "<prop name=\"TAKEMODE\">"))
            checkWhiteBalance(getPropertyValue(eventResponse, "<prop name=\"WB\">"))
            checkPictureEffect(getPropertyValue(eventResponse, "<prop name=\"COLORTONE\">"))
            checkDriveMode(getPropertyValue(eventResponse, "<prop name=\"TAKE_DRIVE\">"))
            checkAeLockState(getPropertyValue(eventResponse, "<prop name=\"AE_LOCK_STATE\">"))
            checkAfLockState(getPropertyValue(eventResponse, "<prop name=\"AF_LOCK_STATE\">"))
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun checkTakeMode(takeMode: String)
    {
        val currentValue = this.currentStatuses[ICameraStatus.CameraProperty.TakeMode] ?: ""
        if (takeMode != currentValue)
        {
            currentStatuses[ICameraStatus.CameraProperty.TakeMode] = takeMode
            statusProvider.changedTakeMode(takeMode)
        }
    }

    private fun checkWhiteBalance(wb: String)
    {
        val currentValue = this.currentStatuses[ICameraStatus.CameraProperty.WhiteBalance] ?: ""
        if (wb != currentValue)
        {
            currentStatuses[ICameraStatus.CameraProperty.WhiteBalance] = wb
            statusProvider.updatedWhiteBalance(wb)
        }
    }

    private fun checkPictureEffect(pictureEffect: String)
    {
        val currentValue = this.currentStatuses[ICameraStatus.CameraProperty.PictureEffect] ?: ""
        if (pictureEffect != currentValue)
        {
            currentStatuses[ICameraStatus.CameraProperty.PictureEffect] = pictureEffect
            statusProvider.updatePictureEffect(pictureEffect)
        }
    }

    private fun checkDriveMode(driveMode: String)
    {
        val currentValue = this.currentStatuses[ICameraStatus.CameraProperty.DriveMode] ?: ""
        if (driveMode != currentValue)
        {
            currentStatuses[ICameraStatus.CameraProperty.DriveMode] = driveMode
            statusProvider.updateDriveMode(driveMode)
        }
    }

    private fun checkAeLockState(aeLockState: String)
    {
        val currentValue = this.currentStatuses[ICameraStatus.CameraProperty.AeLockState] ?: ""
        if (aeLockState != currentValue)
        {
            currentStatuses[ICameraStatus.CameraProperty.AeLockState] = aeLockState
        }
    }

    private fun checkAfLockState(afLockState: String)
    {
        val currentValue = this.currentStatuses[ICameraStatus.CameraProperty.AfLockState] ?: ""
        if (afLockState != currentValue)
        {
            currentStatuses[ICameraStatus.CameraProperty.AfLockState] = afLockState
        }
    }

    private fun getPropertyValue(responseString: String, propertyString: String) : String
    {
        try
        {
            val propertyIndex = responseString.indexOf(propertyString)
            if (propertyIndex > 0)
            {
                val propertyValueIndex = responseString.indexOf("<value>", propertyIndex) + "<value>".length
                val propertyValueLastIndex = responseString.indexOf("</value>", propertyIndex)
                return (responseString.substring(propertyValueIndex, propertyValueLastIndex))
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        return ("")
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
        private val TAG = OpcEventStatusWatch::class.java.simpleName

        private const val TIMEOUT_MS = 2500
        private const val DUMP_LOG = false
    }
}
