package jp.osdn.gokigen.a01lib.camera.interfaces

interface ICameraStatusWatcher
{
    fun subscribe(subscriber : ICameraStatusUpdateNotify)
    fun unsubscribe(subscriber : ICameraStatusUpdateNotify)

    fun startStatusWatch()
    fun stopStatusWatch()
}
