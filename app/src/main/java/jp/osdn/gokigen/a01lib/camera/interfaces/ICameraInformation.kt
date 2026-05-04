package jp.osdn.gokigen.a01lib.camera.interfaces

interface ICameraInformation
{
    val isManualFocus: Boolean
    val isElectricZoomLens: Boolean
    val isExposureLocked: Boolean
}
