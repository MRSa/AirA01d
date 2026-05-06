package jp.osdn.gokigen.a01lib.camera.omds.liveview

interface ILiveviewRtpHeaderReceiver
{
    fun startStatusWatch()
    fun stopStatusWatch()
    fun receiveRtpHeader(byteBuffer: ByteArray)
}
