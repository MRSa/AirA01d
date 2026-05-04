package jp.osdn.gokigen.a01lib.camera.interfaces

import jp.osdn.gokigen.a01lib.camera.interfaces.screen.IAutoFocusFrameDisplay
import jp.osdn.gokigen.a01lib.camera.interfaces.screen.IIndicatorControl

interface IDisplayInjector
{
    fun injectDisplay(
        frameDisplayer: IAutoFocusFrameDisplay,
        indicator: IIndicatorControl,
    )
}
