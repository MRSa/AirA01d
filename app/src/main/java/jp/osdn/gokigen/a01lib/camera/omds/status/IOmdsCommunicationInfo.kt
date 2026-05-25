package jp.osdn.gokigen.a01lib.camera.omds.status

interface IOmdsCommunicationInfo
{
    fun setOmdsProtocol(isOpcProtocol: Boolean)
    fun setOmdsCommandList(commandList: String)
    fun startReceiveOpcEvent()
}
