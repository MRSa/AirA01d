package jp.osdn.gokigen.a01lib.camera.interfaces

interface IOperationCallback
{
    fun operationResult(isChange: Boolean, responseText: String)
}