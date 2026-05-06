package jp.osdn.gokigen.a01lib.camera.interfaces

import jp.osdn.gokigen.a01lib.camera.interfaces.liveview.ILiveViewController
import jp.osdn.gokigen.a01lib.camera.interfaces.playback.IPlaybackControl

interface ICameraInterfaceProvider
{
    fun getCameraInterfaceName() : String
    fun getCameraControl() : ICameraControl
    fun getFocusingControl(): IFocusingControl
    fun getZoomLensControl(): IZoomLensControl
    fun getCaptureControl(): ICaptureControl
    fun getCameraStatusListHolder(): ICameraStatus
    fun getHardwareStatus(): ICameraHardwareStatus
    fun getCameraRunMode(): ICameraRunMode
    fun getCameraStatusWatcher(): ICameraStatusWatcher
    fun getLiveViewControl(): ILiveViewController
    fun getCameraInformation(): ICameraInformation

    //fun getDisplayInjector(): IDisplayInjector?

    fun getPlaybackControl(): IPlaybackControl?
}
