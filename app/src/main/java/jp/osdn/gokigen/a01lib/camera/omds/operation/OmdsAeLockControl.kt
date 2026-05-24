package jp.osdn.gokigen.a01lib.camera.omds.operation

import android.util.Log
import jp.osdn.gokigen.a01lib.camera.utils.communication.SimpleHttpClient
import java.lang.Exception
import java.util.*

class OmdsAeLockControl(
    userAgent: String = "OlympusCameraKit",
    private val executeUrl : String = "http://192.168.0.10",
) {
    private val headerMap: MutableMap<String, String> = HashMap()
    private val http = SimpleHttpClient()
    private var useOpcProtocol : Boolean = true

    fun setUseOpcProtocol(isOpcProtocol: Boolean)
    {
        useOpcProtocol = isOpcProtocol
    }

    fun lockAe()
    {
        val lockUrl = if (useOpcProtocol)
        {
            // OPC時の AE LOCK 処理
            executeUrl + AE_LOCK_COMMAND_OPC
        }
        else
        {
            // OMDS時の AF LOCK 処理
            executeUrl + AE_LOCK_COMMAND
        }
        commandSendMain(lockUrl)
    }

    fun unLockAe()
    {
        val unlockUrl = if (useOpcProtocol)
        {
            // OPC時の AE LOCK 処理
            executeUrl + AE_UNLOCK_COMMAND_OPC
        }
        else
        {
            // OMDS時の AF LOCK 処理
            executeUrl + AE_UNLOCK_COMMAND
        }
        commandSendMain(unlockUrl)
    }

    fun commandSendMain(lockUnlockUrl : String)
    {
        // Log.v(TAG, "lockAutoExposure()")
        try
        {
            val thread = Thread {
                try
                {
                    val lockReply = http.httpGetWithHeader(lockUnlockUrl, headerMap, null, TIMEOUT_MS) ?: ""
                    Log.v(TAG, "lockAutoExposure($lockUnlockUrl) reply: '$lockReply'")
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

    init
    {
        headerMap["User-Agent"] = userAgent // "OlympusCameraKit" // "OI.Share"
        headerMap["X-Protocol"] = userAgent // "OlympusCameraKit" // "OI.Share"
    }

    companion object
    {
        private val TAG = OmdsAutoFocusControl::class.java.simpleName
        private const val TIMEOUT_MS = 3000

        private const val AE_LOCK_COMMAND = "/exec_takemotion.cgi?com=execaelock"
        private const val AE_UNLOCK_COMMAND = "/exec_takemotion.cgi?com=releaseaelock"

        private const val AE_LOCK_COMMAND_OPC = "/exec_takemotion.cgi?com=newexecaelock"
        private const val AE_UNLOCK_COMMAND_OPC = "/exec_takemotion.cgi?com=newreleaseaelock"
    }
}
