package jp.osdn.gokigen.a01lib.camera.omds.operation

import android.util.Log
import jp.osdn.gokigen.a01lib.camera.interfaces.ICameraLiveviewMagnify
import jp.osdn.gokigen.a01lib.camera.utils.communication.SimpleHttpClient
import java.lang.Exception
import java.util.HashMap

class OmdsOpcLiveviewMagnifyControl(
    userAgent: String = "OlympusCameraKit",
    private val executeUrl : String = "http://192.168.0.10",
    private val liveViewQuality : String = "0640x0480",
    // private val useOpcProtocol: Boolean = true
) : ICameraLiveviewMagnify
{
    private val headerMap: MutableMap<String, String> = HashMap()
    private val http = SimpleHttpClient()

    fun getLiveviewCenterPosition(quality: String): String
    {
        // "0640x0480" のような文字列を "x" で分割する
        val parts = quality.split("x")
        if (parts.size != 2) return "0000x0000" // 入力不正時のセーフティ

        // 文字列を数値に変換し、2で割って中心を求める
        val width = parts[0].toIntOrNull() ?: 0
        val height = parts[1].toIntOrNull() ?: 0

        // 中心座標を計算 (値を半分にする)
        val centerX = width / 2
        val centerY = height / 2

        // %04d で4桁の0詰めを指定
        return "%04dx%04d".format(centerX, centerY)
    }

    private fun setMagnifyScale(command: String) {
        try {
            val thread = Thread {
                try {
                    val reply: String = http.httpGetWithHeader(
                        executeUrl + command,
                        headerMap,
                        null,
                        TIMEOUT_MS
                    ) ?: ""
                    Log.v(TAG, "setMagnifyScale() : $command [reply: $reply]")
                }
                catch (e: Exception)
                {
                    e.printStackTrace()
                }
            }
            thread.start()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    override fun startMagnify(scale: Int) {
        val centerPosition = getLiveviewCenterPosition(liveViewQuality)
        setMagnifyScale("/start_lv.cgi?scale=$scale&assign=available&point=$centerPosition")
    }

    override fun changeMagnify(scale: Int) {
        setMagnifyScale("/change_lvscale.cgi?scale=$scale")
    }

    override fun stopMagnify() {
        setMagnifyScale("/stop_lv.cgi")
    }

    init
    {
        headerMap["User-Agent"] = userAgent // "OlympusCameraKit" // "OI.Share"
        headerMap["X-Protocol"] = userAgent // "OlympusCameraKit" // "OI.Share"
    }

    companion object
    {
        private val TAG = OmdsOpcLiveviewMagnifyControl::class.java.simpleName
        private const val TIMEOUT_MS = 3000
    }
}
