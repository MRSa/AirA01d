package jp.osdn.gokigen.a01lib.camera.interfaces

interface IGetRecordImage
{
    interface RecordImageCallback
    {
        fun receivedRecordImage(isLastJpeg: Boolean, receivedContent: ByteArray?)
    }
    fun getCapturedImage(isLastJpeg: Boolean, callback: RecordImageCallback)

}
