package jp.osdn.gokigen.a01lib.camera.omds.connection

import android.util.Log
import jp.osdn.gokigen.a01lib.camera.interfaces.ICameraConnectionStatus
import jp.osdn.gokigen.a01lib.camera.omds.status.IOmdsCommunicationInfo
import jp.osdn.gokigen.a01lib.camera.utils.communication.SimpleHttpClient
import java.lang.Exception
import java.util.HashMap

class OmdsCameraConnectSequence(private val cameraStatusReceiver: ICameraConnectionStatus, private val communicationInfo: IOmdsCommunicationInfo, private val liveViewQuality : String, userAgent : String = "OlympusCameraKit", private val executeUrl : String = "http://192.168.0.10") : Runnable
{
    private val headerMap: MutableMap<String, String> = HashMap()
    private val http = SimpleHttpClient()

    override fun run()
    {
        try
        {
            // --- TODO: OPC専用のカメラ接続シーケンスになっている。 (他OMDS機に対応するためには変更が必要。)
            val getCommandListUrl = "$executeUrl/get_commandlist.cgi"
            val getConnectModeUrl = "$executeUrl/get_connectmode.cgi"
            val switchOpcCameraModeUrl = "$executeUrl/switch_cameramode.cgi"
            val switchCommPathUrl = "$executeUrl/switch_commpath.cgi"

            val response: String = http.httpGetWithHeader(getConnectModeUrl, headerMap, null, TIMEOUT_MS) ?: ""
            Log.v(TAG, " $getConnectModeUrl $response")
            if (response.isNotEmpty())
            {
                // --- カメラが受け付けるコマンドリストを取得する
                val response1: String = http.httpGetWithHeader(getCommandListUrl, headerMap, null, TIMEOUT_MS) ?: ""
                Log.v(TAG, " $getCommandListUrl (${response1.length})")
                communicationInfo.setOmdsCommandList(response1)

                // --- 通信経路をWiFiに(強制)変更する
                val response2: String = http.httpGetWithHeader("$switchCommPathUrl?path=wifi", headerMap, null, TIMEOUT_MS) ?: ""
                Log.v(TAG, " $switchCommPathUrl?path=wifi ($response2)")

                // --- OPCのコマンドを発行する (standaloneモードに切り替える)
                val response3: String = http.httpGetWithHeader("$switchOpcCameraModeUrl?mode=standalone", headerMap, null, TIMEOUT_MS) ?: ""
                Log.v(TAG, " $switchOpcCameraModeUrl?mode=standalone ($response3)")
                if (response3.length > 5)
                {
                    Log.v(TAG, " -=-=-=-=-=- DETECTED OPC CAMERA -=-=-=-=-=-")
                    communicationInfo.startReceiveOpcEvent()
                }

                // --- 撮影モードに切り替える
                val response4: String = http.httpGetWithHeader("$switchOpcCameraModeUrl?mode=rec&lvqty=$liveViewQuality", headerMap, null, TIMEOUT_MS) ?: ""
                Log.v(TAG, " $switchOpcCameraModeUrl?mode=rec $response4")

                ////////////////  for TEST   ////////////////
                if (DUMP_STATUS)
                {
                    val testUrl = "$executeUrl/get_proplist.cgi"  // プロパティ一覧 (OPC)
                    //val testUrl = "$executeUrl/get_camprop.cgi?com=desc&propname=desclist"  // コマンド一覧
                    val testResponse: String = http.httpGetWithHeader(testUrl, headerMap, null, TIMEOUT_MS) ?: ""
                    Log.v(TAG, "     ------------------------------------------ ")
                    for (pos in 0..testResponse.length step 768)
                    {
                        val lastIndex = if ((pos + 768) > testResponse.length) { testResponse.length } else { pos + 768 }
                        Log.v(TAG, " $testUrl ($pos/${testResponse.length}) ${testResponse.substring(pos, lastIndex)}")
                    }
                    Log.v(TAG, "     ------------------------------------------ ")
                }
                ////////////////  for TEST   ////////////////

                onConnectNotify()
            }
            else
            {
                cameraStatusReceiver.onStatusNotify(ICameraConnectionStatus.CameraConnectionStatus.NOT_FOUND)
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
            cameraStatusReceiver.onStatusNotify(ICameraConnectionStatus.CameraConnectionStatus.EXCEPTION)
        }
    }

    private fun onConnectNotify()
    {
        try
        {
            val thread = Thread { // カメラとの接続確立を通知する
                cameraStatusReceiver.onStatusNotify(ICameraConnectionStatus.CameraConnectionStatus.CONNECTED)
                Log.v(TAG, "onConnectNotify()")
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
        private val TAG = OmdsCameraConnectSequence::class.java.simpleName
        private const val TIMEOUT_MS = 3000
        private const val DUMP_STATUS = false
    }
}
