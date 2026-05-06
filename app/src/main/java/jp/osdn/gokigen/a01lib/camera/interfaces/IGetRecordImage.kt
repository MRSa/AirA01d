package jp.osdn.gokigen.a01lib.camera.interfaces

interface IGetRecordImage
{
    interface RecordImageCallback
    {
        fun receivedRecordImage(isLastJpeg: Boolean, receivedContent: String)
    }
    fun getCapturedImage(isLastJpeg: Boolean, callback: RecordImageCallback)

}
