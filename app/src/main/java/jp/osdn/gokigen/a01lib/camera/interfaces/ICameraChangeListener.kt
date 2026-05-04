package jp.osdn.gokigen.a01lib.camera.interfaces

interface ICameraChangeListener
{
    fun onApiListModified(apis: List<String?>?)
    fun onCameraStatusChanged(status: String?)
    fun onLiveviewStatusChanged(status: Boolean)
    fun onShootModeChanged(shootMode: String?)
    fun onZoomPositionChanged(zoomPosition: Int)
    fun onStorageIdChanged(storageId: String?)
    fun onFocusStatusChanged(focusStatus: String?)
    fun onDriveModeChanged(driveMode: String?)
    fun onResponseError()
}
