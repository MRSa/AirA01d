package jp.osdn.gokigen.a01lib.camera.interfaces

interface IZoomLensControl
{
    fun canZoom(): Boolean
    fun updateStatus()
    fun getMaximumFocalLength(): Float
    fun getMinimumFocalLength(): Float
    fun getCurrentFocalLength(): Float
    fun driveZoomLens(targetLength: Float)
    fun driveZoomLens(isZoomIn: Boolean)
    fun moveInitialZoomPosition()
    fun isDrivingZoomLens(): Boolean
}
