package jp.osdn.gokigen.a01lib.camera.omds.wrapper

import android.util.Log
import jp.osdn.gokigen.a01lib.camera.interfaces.ICameraStatus
import jp.osdn.gokigen.a01lib.camera.interfaces.ICaptureControl
import jp.osdn.gokigen.a01lib.camera.omds.operation.OmdsContinuousShotControl
import jp.osdn.gokigen.a01lib.camera.omds.operation.OmdsSingleShotControl
import java.lang.Exception
import java.util.*

class OmdsCaptureControl(val statusChecker : ICameraStatus): ICaptureControl
{
    private var isStarted = false
    private val singleShotControl = OmdsSingleShotControl()
    private val continuousShotControl = OmdsContinuousShotControl()

    override fun doCapture(captureAction: ICaptureControl.CaptureAction)
    {
        Log.v(TAG, "doCapture() : $captureAction")
        try
        {
            val status = statusChecker.getStatus(ICameraStatus.CameraProperty.DriveMode).lowercase(Locale.getDefault())
            Log.v(TAG, "OMDS Capture (Mode : $status)")
            if (!status.contains("normal"))
            {
                // 連写の場合...
                continuousShotControl.continuousShot(isStarted)
                isStarted = !isStarted
                return
            }

            // 単写の場合...
            singleShotControl.singleShot()
            return
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    companion object
    {
        private val TAG: String = OmdsCaptureControl::class.java.simpleName
    }

}
