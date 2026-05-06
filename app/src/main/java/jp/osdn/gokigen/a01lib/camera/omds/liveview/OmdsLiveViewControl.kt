package jp.osdn.gokigen.a01lib.camera.omds.liveview

import android.util.Log
import jp.osdn.gokigen.a01lib.camera.interfaces.liveview.IImageDataReceiver
import jp.osdn.gokigen.a01lib.camera.interfaces.liveview.ILiveViewController
import jp.osdn.gokigen.a01lib.camera.utils.communication.SimpleHttpClient
import kotlinx.coroutines.*
import java.net.DatagramPacket
import java.net.DatagramSocket

/**
 *    OMDS LiveView Control (RTP over UDP)
 */
class OmdsLiveViewControl(
    userAgent: String = "OlympusCameraKit",
    private val executeUrl: String = "http://192.168.0.10",
) : ILiveViewController, CoroutineScope by MainScope()
{
    private val headerMap: MutableMap<String, String> = mutableMapOf(
        "User-Agent" to userAgent,
        "X-Protocol" to userAgent
    )
    private val http = SimpleHttpClient()

    private val receivedImageBuffer = ByteArray(RECEIVE_BUFFER_SIZE)
    private var receivedImageBufferOffset = 0
    private var nextSequenceNumber = -1

    private var receiveSocket: DatagramSocket? = null
    private var whileStreamReceive = false
    private var job: Job? = null

    private var imageDataReceiver: IImageDataReceiver? = null
    private var statusWatcher: ILiveviewRtpHeaderReceiver? = null

    fun setReceiver(imageDataReceiver: IImageDataReceiver, statusWatcher: ILiveviewRtpHeaderReceiver)
    {
        this.imageDataReceiver = imageDataReceiver
        this.statusWatcher = statusWatcher
    }

    override fun startLiveView()
    {
        Log.v(TAG, "----- startLiveView() : OMDS -----")
        job = launch(Dispatchers.IO) {
            try
            {
                if (startReceiveStream())
                {
                    val requestUrl = executeUrl + LIVEVIEW_START_REQUEST
                    val reply = http.httpGetWithHeader(requestUrl, headerMap, null, TIMEOUT_MS) ?: ""
                    Log.v(TAG, "START LIVEVIEW: $requestUrl, Reply: $reply")
                }
            }
            catch (e: Exception)
            {
                Log.e(TAG, "Failed to start liveview", e)
            }
        }
    }

    override fun stopLiveView()
    {
        Log.v(TAG, "----- stopLiveView() : OMDS -----")
        whileStreamReceive = false
        statusWatcher?.stopStatusWatch()

        launch(Dispatchers.IO) {
            try
            {
                val requestUrl = executeUrl + LIVEVIEW_STOP_REQUEST
                val reply = http.httpGetWithHeader(requestUrl, headerMap, null, TIMEOUT_MS) ?: ""
                Log.v(TAG, "Stop request sent: $reply")
                closeReceiveSocket()
            }
            catch (e: Exception)
            {
                Log.e(TAG, "Error stopping liveview", e)
            }
            finally
            {
                job?.cancel()
            }
        }
    }

    private fun startReceiveStream(): Boolean
    {
        if (whileStreamReceive) return true
        return try {
            receiveSocket = DatagramSocket(LIVEVIEW_PORT).apply {
                soTimeout = TIMEOUT_MS
            }
            whileStreamReceive = true
            receivedImageBufferOffset = 0
            nextSequenceNumber = -1
            statusWatcher?.startStatusWatch()

            launch(Dispatchers.IO) { receiverLoop() }
            true
        }
        catch (e: Exception)
        {
            Log.e(TAG, "Socket open failed", e)
            whileStreamReceive = false
            false
        }
    }

    private fun receiverLoop()
    {
        val buffer = ByteArray(RECEIVE_BUFFER_SIZE)
        var timeoutCount = 0

        while (whileStreamReceive)
        {
            try
            {
                val packet = DatagramPacket(buffer, buffer.size)
                receiveSocket?.receive(packet)
                processRtpPacket(packet)
                timeoutCount = 0
            }
            catch (_: Exception)
            {
                if (++timeoutCount > TIMEOUT_MAX) {
                    Log.w(TAG, "Receive timeout reached max, retrying...")
                    timeoutCount = 0
                }
            }
        }
        closeReceiveSocket()
    }

    private fun processRtpPacket(packet: DatagramPacket)
    {
        val data = packet.data
        val len = packet.length
        if (len < 12) return

        // RTP Sequence Number (Byte 2-3)
        val seqNum = ((data[2].toInt() and 0xff) shl 8) or (data[3].toInt() and 0xff)

        // RTP Header interpretation
        val hasExtension = (data[0].toInt() and 0x10) != 0
        val isMarkerSet = (data[1].toInt() and 0x80) != 0

        var offset = 12 // Basic RTP header length
        if (hasExtension)
        {
            // Extension header length (4 bytes header + data)
            val extLen = (((data[offset + 2].toInt() and 0xff) shl 8) or (data[offset + 3].toInt() and 0xff)) * 4
            val totalHeaderSize = 12 + 4 + extLen

            // JPEG開始位置(SOI: 0xFFD8)を検索
            val jpegStart = findJpegStart(data)
            if (jpegStart != -1)
            {
                // フレームの開始パケットと判断
                // JPEG開始位置までのデータをRTPヘッダー（拡張情報込）としてWatcherに渡す
                statusWatcher?.receiveRtpHeader(data.copyOfRange(0, jpegStart))

                receivedImageBufferOffset = 0
                nextSequenceNumber = seqNum
                offset = jpegStart
            }
            else
            {
                // JPEG開始が見つからない場合は拡張ヘッダー分をスキップ
                offset = totalHeaderSize
            }
        }

        // Check sequence continuity
        if (nextSequenceNumber != -1 && seqNum != nextSequenceNumber)
        {
            Log.w(TAG, "Packet loss detected: Expected $nextSequenceNumber, got $seqNum")
        }
        nextSequenceNumber = (seqNum + 1) % 65536

        // Copy payload to buffer
        val payloadLen = len - offset
        if (payloadLen > 0 && receivedImageBufferOffset + payloadLen <= RECEIVE_BUFFER_SIZE)
        {
            System.arraycopy(data, offset, receivedImageBuffer, receivedImageBufferOffset, payloadLen)
            receivedImageBufferOffset += payloadLen
        }

        // JPEG End Detect (Marker bit or EOI: 0xFFD9)
        if (isMarkerSet || (data[len - 2] == 0xff.toByte() && data[len - 1] == 0xd9.toByte())) {
            if (receivedImageBufferOffset > 0)
            {
                val frame = receivedImageBuffer.copyOfRange(0, receivedImageBufferOffset)
                imageDataReceiver?.onUpdateLiveView(frame, null)
            }
            receivedImageBufferOffset = 0
        }
    }

    private fun findJpegStart(data: ByteArray, offset: Int = 12): Int {
        // パケットの終端まで0xFFD8を探す
        for (i in offset until data.size - 1) {
            if (data[i] == 0xff.toByte() && data[i + 1] == 0xd8.toByte()) return i
        }
        return -1
    }

    private fun closeReceiveSocket() {
        try {
            receiveSocket?.apply {
                if (!isClosed) close()
            }
            receiveSocket = null
            Log.v(TAG, "Socket closed")
        } catch (e: Exception) {
            Log.e(TAG, "Socket close error", e)
        }
    }

    companion object {
        private val TAG = OmdsLiveViewControl::class.java.simpleName
        private const val LIVEVIEW_START_REQUEST = "/exec_takemisc.cgi?com=startliveview&port=49152"
        private const val LIVEVIEW_STOP_REQUEST = "/exec_takemisc.cgi?com=stopliveview"
        private const val TIMEOUT_MAX = 3
        private const val RECEIVE_BUFFER_SIZE = 1024 * 1024 * 4 // 4MB
        private const val TIMEOUT_MS = 1500
        private const val LIVEVIEW_PORT = 49152
    }
}
