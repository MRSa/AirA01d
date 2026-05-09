package jp.osdn.gokigen.a01lib.camera.omds.status

import android.util.Log
import jp.osdn.gokigen.a01lib.camera.omds.status.OmdsCameraStatusWatcher.IOpcEventReceive
import jp.osdn.gokigen.a01lib.camera.utils.communication.SimpleHttpClient
import java.io.ByteArrayOutputStream
import java.net.Socket
import java.util.HashMap
import kotlin.collections.set

class OpcPushEventWatcher(
    private val opcEventReceiver: IOpcEventReceive? = null,
    userAgent: String = "OlympusCameraKit",
    private val executeUrl : String = "http://192.168.0.10",
)
{
    private val headerMap: MutableMap<String, String> = HashMap()
    private val http = SimpleHttpClient()
    private var whileEventReceive = false

    fun requestOpcEventWatch(portNo: Int = 65000)
    {
        try
        {
            // OPC機のイベント通知開始
            val eventWatchUrl = "$executeUrl/start_pushevent.cgi?port=$portNo"
            Log.v(TAG, " requestOpcEventWatch : $eventWatchUrl")
            val response = http.httpGetWithHeader(eventWatchUrl, headerMap, null,
                TIMEOUT_MS
            ) ?: ""
            if ((response.isNotEmpty())&&(DUMP_LOG))
            {
                // ----- ログ表示モードの時
                Log.v(TAG, "start_pushevent.cgi (len:${response.length}) $response")
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    fun eventReceiverThread(portNumber: Int)
    {
        if (whileEventReceive)
        {
            Log.v(TAG, "startReceiveStream() : already starting.")
            return
        }
        try
        {
            whileEventReceive = true
            val bufferSize = RECEIVE_BUFFER_SIZE
            val byteArray = ByteArray(bufferSize)
            val hostName = executeUrl.substring("http://".length)
            Log.v(TAG, " OPC: EVENT LISTEN : $hostName, $portNumber")
            val eventReceiveSocket = Socket(hostName, portNumber)
            val inputStream = eventReceiveSocket.getInputStream()
            while (whileEventReceive)
            {
                try
                {
                    // Thread.sleep(SLEEP_EVENT_TIME_MS)
                    val dataBytes = inputStream.available()
                    if (dataBytes > 0)
                    {
                        // データがあった...受信する
                        // Log.v(TAG, " RECEIVE OPC EVENT : $dataBytes bytes")
                        val byteStream = ByteArrayOutputStream()
                        var readIndex = 0
                        while (readIndex < dataBytes)
                        {
                            val readBytes = inputStream.read(byteArray, 0, bufferSize)
                            if (readBytes <= 0)
                            {
                                Log.v(TAG, " RECEIVED MESSAGE FINISHED ($dataBytes)")
                                break
                            }
                            readIndex += readBytes
                            byteStream.write(byteArray, 0, readBytes)
                        }
                        opcEventReceiver?.receivedOpcEvent(byteStream.toByteArray())
                    }
                    else
                    {
                        if (DUMP_LOG)
                        {
                            Log.v(TAG, " NOT RECEIVE OPC EVENT ...WAIT AGAIN...")
                        }
                    }
                    Thread.sleep(SLEEP_EVENT_TIME_MS)
                }
                catch (e: Exception)
                {
                    e.printStackTrace()
                    whileEventReceive = false
                    finishEventReceiverThread()
                }
            }
            System.gc()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    fun finishEventReceiverThread()
    {
        try
        {
            Log.v(TAG, "finishEventReceiverThread()")
            whileEventReceive = false

            // OPC機のイベント通知を止める処理
            val eventWatchUrl = "$executeUrl/stop_pushevent.cgi"
            val response = http.httpGetWithHeader(eventWatchUrl, headerMap, null,
                TIMEOUT_MS
            ) ?: ""
            if ((response.isNotEmpty())&&(DUMP_LOG))
            {
                // ----- ログ表示モードの時
                Log.v(TAG, "stop_pushevent.cgi (len:${response.length}) $response")
            }
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
        private val TAG = OpcPushEventWatcher::class.java.simpleName

        private const val SLEEP_EVENT_TIME_MS = 500L // 500ms
        private const val TIMEOUT_MS = 2500

        private const val RECEIVE_BUFFER_SIZE = 16384

        private const val DUMP_LOG = false
    }
}

