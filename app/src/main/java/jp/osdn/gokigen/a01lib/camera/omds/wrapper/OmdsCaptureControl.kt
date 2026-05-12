package jp.osdn.gokigen.a01lib.camera.omds.wrapper

import android.util.Log
import jp.osdn.gokigen.a01lib.camera.interfaces.ICameraStatus
import jp.osdn.gokigen.a01lib.camera.interfaces.ICaptureControl
import jp.osdn.gokigen.a01lib.camera.omds.operation.OmdsContinuousShotControl
import jp.osdn.gokigen.a01lib.camera.omds.operation.OmdsMovieShotControl
import jp.osdn.gokigen.a01lib.camera.omds.operation.OmdsSingleShotControl
import java.lang.Exception
import java.util.*

class OmdsCaptureControl(val statusChecker : ICameraStatus): ICaptureControl
{
    private var isStarted = false
    private val singleShotControl = OmdsSingleShotControl()
    private val continuousShotControl = OmdsContinuousShotControl()
    private val movieShotControl = OmdsMovieShotControl()

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
                val action = when (captureAction) {
                    ICaptureControl.CaptureAction.ON -> false
                    ICaptureControl.CaptureAction.OFF -> true
                    ICaptureControl.CaptureAction.TOGGLE -> isStarted
                }
                continuousShotControl.continuousShot(action)
                if (captureAction == ICaptureControl.CaptureAction.TOGGLE)
                {
                    // --- TOGGLE を指定された場合には、フラグを反転させる
                    isStarted = !isStarted
                }
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

    override fun doMovie(captureAction: ICaptureControl.CaptureAction)
    {
        try
        {
            val action = when (captureAction) {
                ICaptureControl.CaptureAction.ON -> false
                ICaptureControl.CaptureAction.OFF -> true
                ICaptureControl.CaptureAction.TOGGLE -> isStarted
            }
            movieShotControl.movieAction(action)
            if (captureAction == ICaptureControl.CaptureAction.TOGGLE)
            {
                // --- TOGGLE を指定された場合には、フラグを反転させる
                isStarted = !isStarted
            }
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
