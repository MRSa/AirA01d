package jp.osdn.gokigen.a01lib.camera.interfaces

interface ICameraEventNotify
{
    fun getSubscribeId(): String
    fun receivedCameraEvent(eventMessage: ByteArray)
}