package jp.osdn.gokigen.a01lib.camera.omds.playback

import jp.osdn.gokigen.a01lib.camera.interfaces.playback.IPlaybackControl
import jp.osdn.gokigen.a01lib.camera.utils.communication.SimpleHttpClient

class OmdsPlaybackControl(
    userAgent: String = "OlympusCameraKit",
    private val executeUrl : String = "http://192.168.0.10",
) : IPlaybackControl
{
    private val headerMap: MutableMap<String, String> = HashMap()
    private val http = SimpleHttpClient()
    private var useOpcProtocol : Boolean = true

    private val imageListGetter = OmdsGetImageFileList()

    fun setUseOpcProtocol(isOpcProtocol: Boolean)
    {
        useOpcProtocol = isOpcProtocol
        imageListGetter.useOpcProtocol = isOpcProtocol
    }

    override fun enterPlaybackMode(): Boolean { return true }
    override fun leavePlaybackMode(): Boolean { return true }

    override fun getImageFileList(directory: String): List<IPlaybackControl.ImageFileInfo>
    {
        return imageListGetter.getImageFileList(directory)
    }

    init
    {
        headerMap["User-Agent"] = userAgent // "OlympusCameraKit" // "OI.Share"
        headerMap["X-Protocol"] = userAgent // "OlympusCameraKit" // "OI.Share"
    }

    companion object
    {
        private val TAG = OmdsPlaybackControl::class.java.simpleName
        private const val TIMEOUT_MS = 3500

        private const val GET_IMAGELIST_COMMAND = "/get_imglist.cgi"
        private const val GET_IMAGELIST_COMMAND_OPC = "/get_imglist.cgi"
    }
}
