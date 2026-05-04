package jp.osdn.gokigen.a01lib.camera.interfaces

import android.content.Context
import jp.osdn.gokigen.a01lib.camera.interfaces.liveview.IImageDataReceiver
import jp.osdn.gokigen.a01lib.camera.interfaces.screen.IAutoFocusFrameDisplay
import jp.osdn.gokigen.a01lib.camera.interfaces.screen.IIndicatorControl

interface ICameraControl
{
    fun initialize(imageReceiver: IImageDataReceiver, frameDisplayer: IAutoFocusFrameDisplay, indicator: IIndicatorControl)

    fun connectToCamera()
    fun disconnectFromCamera()
    fun startCamera(context: Context)
    fun finishCamera(isPowerOff: Boolean)

    fun getCameraConnectionStatus(): ICameraConnectionStatus.CameraConnectionStatus
    fun subscribeCameraConnectionStatus(receiver: ICameraConnectionStatus)
    fun unsubscribeCameraConnectionStatus(receiver: ICameraConnectionStatus)
    fun subscribeEventReceiver(subscriber: ICameraEventNotify)
    fun unsubscribeEventReceiver(subscriber: ICameraEventNotify)

    fun changeRunMode(runMode: String, callback: IOperationCallback)

    fun needRotateImage() : Boolean
    fun getDisplayInjector() : IDisplayInjector?
    fun getCameraStatus() : ICameraStatus?
    fun getFocusingControl() : IFocusingControl?
    fun getZoomControl() : IZoomLensControl?
    fun getCaptureControl() : ICaptureControl?
}
