package jp.osdn.gokigen.a01lib.camera.interfaces

interface IDigitalZoomControl
{
    interface DigitalZoomScaleCallback
    {
        fun zoomScale(lowerScale: Int, upperScale: Int)
    }
    fun getDigitalScopeScale(callback: DigitalZoomScaleCallback)
    fun changeDigitalZoomScale(scale: Int)
}
