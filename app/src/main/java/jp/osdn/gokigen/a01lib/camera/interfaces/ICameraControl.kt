package jp.osdn.gokigen.a01lib.camera.interfaces

import android.content.Context
import jp.osdn.gokigen.a01lib.camera.interfaces.liveview.IImageDataReceiver
import jp.osdn.gokigen.a01lib.camera.interfaces.playback.IPlaybackControl

interface ICameraControl
{
    fun initialize(imageReceiver: IImageDataReceiver)

    fun connectToCamera()
    fun disconnectFromCamera()
    fun startCamera(context: Context)
    fun finishCamera(isPowerOff: Boolean)

    fun getCameraConnectionStatus(): ICameraConnectionStatus.CameraConnectionStatus
    fun subscribeCameraConnectionStatus(receiver: ICameraConnectionStatus)
    fun unsubscribeCameraConnectionStatus(receiver: ICameraConnectionStatus)
    fun subscribeEventReceiver(subscriber: ICameraEventNotify)
    fun unsubscribeEventReceiver(subscriber: ICameraEventNotify)
    fun subscribeCameraStatus(subscriber: ICameraStatusUpdateNotify)
    fun unsubscribeCameraStatus(subscriber: ICameraStatusUpdateNotify)

    fun changeRunMode(runMode: String, callback: IOperationCallback)

    fun needRotateImage() : Boolean
    fun getCameraStatus() : ICameraStatus?
    fun getFocusingControl() : IFocusingControl?
    fun getZoomControl() : IZoomLensControl?
    fun getCaptureControl() : ICaptureControl?
    fun getGetRecordImage() : IGetRecordImage?
    fun getLiveviewMagnify() : ICameraLiveviewMagnify?
    fun getDigitalZoomControl() : IDigitalZoomControl?
    fun getCameraHardwareInformation() : ICameraHardwareInformation
    fun getCameraConnectionProtocol() : ICameraConnectionStatus.CameraProtocol

    fun getCameraPlaybackControl() : IPlaybackControl
}
