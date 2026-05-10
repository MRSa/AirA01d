package jp.osdn.gokigen.a01lib.camera.interfaces

interface ICameraConnectionStatus
{
    enum class CameraConnectionStatus
    {
        START, CONNECTING, CHECK_WIFI, CONNECTED, DISCONNECTED, NOT_FOUND, ERROR, EXCEPTION
    }

    fun onStatusNotify(status: CameraConnectionStatus)
}
