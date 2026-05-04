package jp.osdn.gokigen.a01lib.camera.interfaces

interface ICameraStatusHolder
{
    fun getCameraStatus(): String?
    fun getLiveviewStatus(): Boolean
    fun getShootMode(): String?
    fun getAvailableShootModes(): List<String?>?
    fun getZoomPosition(): Int
    fun getStorageId(): String?
}
