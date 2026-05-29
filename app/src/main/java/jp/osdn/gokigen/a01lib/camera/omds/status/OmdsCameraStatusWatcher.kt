package jp.osdn.gokigen.a01lib.camera.omds.status

import android.util.Log
import jp.osdn.gokigen.a01lib.camera.interfaces.ICameraStatus
import jp.osdn.gokigen.a01lib.camera.interfaces.ICameraStatus.CameraProperty
import jp.osdn.gokigen.a01lib.camera.interfaces.ICameraStatusUpdateNotify
import jp.osdn.gokigen.a01lib.camera.interfaces.ICameraStatusWatcher
import jp.osdn.gokigen.a01lib.camera.omds.liveview.ILiveviewRtpHeaderReceiver
import java.util.*
import kotlin.Exception

class OmdsCameraStatusWatcher(
    opcEventReceiver: IOpcEventReceive? = null,
    userAgent: String = "OlympusCameraKit",
) : ICameraStatusWatcher, ICameraStatus, IOmdsCommunicationInfo, ILiveviewRtpHeaderReceiver
{
    private val headerMap: MutableMap<String, String> = HashMap()

    private val statusProvider = OmdsCameraStatusProvider()
    private val rtpHeaderParser = OpcRtpHeaderParser(statusProvider)
    private val opcEventWatcher = OpcEventStatusWatch(statusProvider)
    private val pushEventWatcher = OpcPushEventWatcher(opcEventReceiver)
    private val opcProperties = OpcCameraProperties()
    private val omdsEventWatcher = OmdsEventStatusWatch(statusProvider)
    private val omdsProperties = OmdsCameraProperties()

    private var isWatchingRtp = false
    private var isWatchingEvent = false
    private var useOpcProtocol : Boolean = true

    fun setUseOpcProtocol(isOpcProtocol: Boolean)
    {
        useOpcProtocol = isOpcProtocol
    }

    override fun subscribe(subscriber: ICameraStatusUpdateNotify)
    {
        statusProvider.subscribe(subscriber)
    }

    override fun unsubscribe(subscriber: ICameraStatusUpdateNotify)
    {
        statusProvider.unsubscribe(subscriber)
    }

    override fun setOmdsProtocol(isOpcProtocol: Boolean)
    {
        useOpcProtocol = isOpcProtocol
    }

    override fun setOmdsCommandList(commandList: String)
    {
        Log.v(TAG, "setOmdsCommandList()")
        //Log.v(TAG, "setOmdsCommandList:\n$commandList")
        startStatusWatch()
    }

    override fun startReceiveOpcEvent()
    {
        // ----- OPCの場合は、、イベントも監視する
        startEventWatch()
    }

    override fun receiveRtpHeader(byteBuffer: ByteArray)
    {
        // ----- RTP拡張ヘッダを受信した
        rtpHeaderParser.receiveRtpHeader(byteBuffer)
    }

    private fun startEventWatch(portNumber: Int = 65000)
    {
        // イベント受信用の準備...
        pushEventWatcher.finishEventReceiverThread()
        pushEventWatcher.requestOpcEventWatch()

        // 受信スレッドを動かす
        val thread = Thread { pushEventWatcher.eventReceiverThread(portNumber) }
        try
        {
            thread.start()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    override fun startStatusWatch()
    {
        try
        {
            startRtpHeaderWatch()
            startEventStatusWatch()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun startRtpHeaderWatch()
    {
        try
        {
            Log.v(TAG, " startRtpHeaderWatch()")
            val thread = Thread {
                isWatchingRtp = true
                while (isWatchingRtp)
                {
                    rtpHeaderParser.parseRtpHeader()
                    Thread.sleep(SLEEP_TIME_MS)
                }
            }
            thread.start()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun startEventStatusWatch()
    {
        try
        {
            Log.v(TAG, " startEventStatusWatch() : $useOpcProtocol")
            val thread = Thread {
                isWatchingEvent = true
                while (isWatchingEvent)
                {
                    // ----- EVENT POLLING
                    if (!useOpcProtocol)
                    {
                        omdsEventWatcher.watchOmdsStatus()
                    }
                    else
                    {
                        opcEventWatcher.watchOpcStatus()
                    }
                    Thread.sleep(SLEEP_EVENT_TIME_MS)
                }
            }
            thread.start()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    override fun stopStatusWatch()
    {
        isWatchingRtp = false
        isWatchingEvent = false

        Thread {
            pushEventWatcher.finishEventReceiverThread()
        }.start()
    }

    override fun getStatusList(key: CameraProperty): List<String>
    {
        return (if (useOpcProtocol) { opcProperties.getStatusList(key) } else { omdsProperties.getStatusList(key) })
    }

    override fun getStatus(key: CameraProperty): String
    {
        return if (useOpcProtocol)
        {
            opcEventWatcher.getStatus(key)
        }
        else
        {
            omdsEventWatcher.getStatus(key)
        }
    }

    override fun setStatus(key: CameraProperty, value: String)
    {
        if (useOpcProtocol)
        {
            opcProperties.setStatus(key, value)
        }
        else
        {
            omdsProperties.setStatus(key, value)
        }
    }

    override fun getDescriptorList(): List<ICameraStatus.CameraPropertyDescriptor>
    {
        // ----- descriptor list の応答は OMDS機のみサポート
        return (if (useOpcProtocol) { emptyList() } else { omdsProperties.getDescriptorList() })
    }

    override fun getDescriptor(propertyName: String): ICameraStatus.CameraPropertyDescriptor
    {
        return (if (useOpcProtocol) { opcProperties.getDescriptor(propertyName) } else { omdsProperties.getDescriptor(propertyName) })
    }

    override fun setStatusString(propertyName: String, value: String)
    {
        if (useOpcProtocol)
        {
            opcProperties.setStatusString(propertyName, value)
        }
        else
        {
            omdsProperties.setStatusString(propertyName, value)
        }
    }

    init
    {
        headerMap["User-Agent"] = userAgent // "OlympusCameraKit" // "OI.Share"
        headerMap["X-Protocol"] = userAgent // "OlympusCameraKit" // "OI.Share"
    }

    interface IOpcEventReceive
    {
        fun receivedOpcEvent(value: ByteArray)
    }

    companion object
    {
        private val TAG = OmdsCameraStatusWatcher::class.java.simpleName

        private const val SLEEP_TIME_MS = 500L
        private const val SLEEP_EVENT_TIME_MS = 500L
    }
}
