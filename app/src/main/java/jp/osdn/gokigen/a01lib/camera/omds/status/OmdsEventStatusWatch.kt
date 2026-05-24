package jp.osdn.gokigen.a01lib.camera.omds.status

import android.util.Log
import jp.osdn.gokigen.a01lib.camera.interfaces.ICameraStatus
import jp.osdn.gokigen.a01lib.camera.interfaces.ICameraStatusUpdateNotify
import jp.osdn.gokigen.a01lib.camera.utils.communication.SimpleHttpClient
import java.util.HashMap

class OmdsEventStatusWatch(
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

    fun watchOmdsStatus()
    {
        try
        {
            // OMDS機のイベント受信
            val omdsEventUrl = "$executeUrl/get_camprop.cgi?com=desc&propname=desclist"
            val eventResponse = http.httpGetWithHeader(omdsEventUrl, headerMap, null,
                TIMEOUT_MS
            ) ?: ""
            if (eventResponse.isNotEmpty())
            {
                dumpLog(omdsEventUrl, eventResponse)
                parseOmdsProperties(eventResponse)
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun parseOmdsProperties(eventResponse: String)
    {
        try
        {
            checkTakeMode(getPropertyValue(eventResponse, "<propname>takemode</propname>"))
            checkWhiteBalance(decideWhiteBalance(getPropertyValue(eventResponse, "<propname>wbvalue</propname>")))
            checkPictureEffect(getPropertyValue(eventResponse, "<propname>colortone</propname>"))
            checkDriveMode(getPropertyValue(eventResponse, "<propname>drivemode</propname>"))
            checkShutterSpeed(getPropertyValue(eventResponse, "<propname>shutspeedvalue</propname>"))
            checkAperture(getPropertyValue(eventResponse, "<propname>focalvalue</propname>"))
            checkIsoSensitivity(getPropertyValue(eventResponse, "<propname>isospeedvalue</propname>"))
            checkExposureCompensation(getPropertyValue(eventResponse, "<propname>expcomp</propname>"))
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

    private fun checkShutterSpeed(shutterSpeed: String)
    {
        val currentValue = this.currentStatuses[ICameraStatus.CameraProperty.ShutterSpeed] ?: ""
        if (shutterSpeed != currentValue)
        {
            currentStatuses[ICameraStatus.CameraProperty.ShutterSpeed] = shutterSpeed
            statusProvider.updatedShutterSpeed(shutterSpeed)
        }
    }

    private fun checkAperture(aperture: String)
    {
        val currentValue = this.currentStatuses[ICameraStatus.CameraProperty.Aperture] ?: ""
        if (aperture != currentValue)
        {
            currentStatuses[ICameraStatus.CameraProperty.Aperture] = aperture
            statusProvider.updatedAperture(aperture)
        }
    }

    private fun checkIsoSensitivity(isoSensitivity: String)
    {
        val currentValue = this.currentStatuses[ICameraStatus.CameraProperty.IsoSensitivity] ?: ""
        if (isoSensitivity != currentValue)
        {
            currentStatuses[ICameraStatus.CameraProperty.IsoSensitivity] = isoSensitivity
            statusProvider.updateIsoSensitivity(isoSensitivity)
        }
    }

    private fun checkExposureCompensation(exposureCompensation: String)
    {
        val currentValue = this.currentStatuses[ICameraStatus.CameraProperty.ExposureCompensation] ?: ""
        if (exposureCompensation != currentValue)
        {
            currentStatuses[ICameraStatus.CameraProperty.ExposureCompensation] = exposureCompensation
            statusProvider.updatedExposureCompensation(exposureCompensation)
        }
    }

    fun decideWhiteBalance(wbValue: String) : String
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
        private val TAG = OmdsEventStatusWatch::class.java.simpleName

        private const val TIMEOUT_MS = 2500
        private const val DUMP_LOG = false
    }
}
