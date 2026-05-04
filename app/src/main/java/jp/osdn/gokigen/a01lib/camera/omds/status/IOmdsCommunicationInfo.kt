package jp.osdn.gokigen.a01lib.camera.omds.status

interface IOmdsCommunicationInfo
{
    fun setOmdsCommandList(commandList: String)
    fun startReceiveOpcEvent()
}
