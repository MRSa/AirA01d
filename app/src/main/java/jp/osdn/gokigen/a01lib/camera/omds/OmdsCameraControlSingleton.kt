package jp.osdn.gokigen.a01lib.camera.omds

import android.content.Context
import android.util.Log
import jp.osdn.gokigen.a01lib.camera.interfaces.ICameraControl
import jp.osdn.gokigen.a01lib.camera.interfaces.ICameraStatus
import jp.osdn.gokigen.a01lib.camera.interfaces.ICameraConnectionStatus
import jp.osdn.gokigen.a01lib.camera.interfaces.ICameraConnectionStatus.CameraConnectionStatus
import jp.osdn.gokigen.a01lib.camera.interfaces.ICaptureControl
import jp.osdn.gokigen.a01lib.camera.interfaces.IFocusingControl
import jp.osdn.gokigen.a01lib.camera.interfaces.ICameraEventNotify
import jp.osdn.gokigen.a01lib.camera.interfaces.ICameraHardwareInformation
import jp.osdn.gokigen.a01lib.camera.interfaces.ICameraLiveviewMagnify
import jp.osdn.gokigen.a01lib.camera.interfaces.ICameraStatusUpdateNotify
import jp.osdn.gokigen.a01lib.camera.interfaces.IDigitalZoomControl
import jp.osdn.gokigen.a01lib.camera.interfaces.IGetRecordImage
import jp.osdn.gokigen.a01lib.camera.interfaces.IOperationCallback
import jp.osdn.gokigen.a01lib.camera.interfaces.IZoomLensControl
import jp.osdn.gokigen.a01lib.camera.interfaces.liveview.IImageDataReceiver
import jp.osdn.gokigen.a01lib.camera.omds.connection.OmdsCameraConnection
import jp.osdn.gokigen.a01lib.camera.omds.liveview.OmdsLiveViewControl
import jp.osdn.gokigen.a01lib.camera.omds.operation.OmdsCamIndStatus
import jp.osdn.gokigen.a01lib.camera.omds.operation.OmdsCameraGetProperty
import jp.osdn.gokigen.a01lib.camera.omds.operation.OmdsCameraHardwareInformation
import jp.osdn.gokigen.a01lib.camera.omds.operation.OmdsCommPathControl
import jp.osdn.gokigen.a01lib.camera.omds.operation.OmdsCommPathStatus
import jp.osdn.gokigen.a01lib.camera.omds.operation.OmdsDigitalZoomControl
import jp.osdn.gokigen.a01lib.camera.omds.operation.OmdsGetCommand
import jp.osdn.gokigen.a01lib.camera.omds.operation.OmdsGetRecordImage
import jp.osdn.gokigen.a01lib.camera.omds.operation.OmdsOpcLiveviewMagnifyControl
import jp.osdn.gokigen.a01lib.camera.omds.operation.OmdsPostCommand
import jp.osdn.gokigen.a01lib.camera.omds.operation.OmdsRunModeControl
import jp.osdn.gokigen.a01lib.camera.omds.operation.OmdsTimeSync
import jp.osdn.gokigen.a01lib.camera.omds.operation.OmdsZoomLensControl
import jp.osdn.gokigen.a01lib.camera.omds.status.OmdsCameraStatusWatcher
import jp.osdn.gokigen.a01lib.camera.omds.wrapper.OmdsCaptureControl
import jp.osdn.gokigen.a01lib.camera.omds.wrapper.OmdsFocusControl

class OmdsCameraControlSingleton : ICameraConnectionStatus, OmdsCameraStatusWatcher.IOpcEventReceive, ICameraControl
{
    private val statusWatcher = OmdsCameraStatusWatcher(this)
    private val liveviewControl = OmdsLiveViewControl()
    private var cameraConnectionStatus: CameraConnectionStatus = CameraConnectionStatus.DISCONNECTED
    private val subscriberList = ArrayList<ICameraEventNotify>()
    private val connectionStatusReceiverList = ArrayList<ICameraConnectionStatus>()

    private lateinit var cameraConnection: OmdsCameraConnection
    private lateinit var runModeControl : OmdsRunModeControl
    private lateinit var commPathControl : OmdsCommPathControl
    private lateinit var timeSync: OmdsTimeSync
    private lateinit var cameraHardwareInformation: OmdsCameraHardwareInformation
    private lateinit var getCameraProperty: OmdsCameraGetProperty
    private lateinit var getCommand: OmdsGetCommand
    private lateinit var postCommand: OmdsPostCommand
    private lateinit var camInState: OmdsCamIndStatus
    private lateinit var camCommPathStatus: OmdsCommPathStatus
    private lateinit var focusControl: OmdsFocusControl
    private lateinit var captureControl: OmdsCaptureControl
    private lateinit var getRecordImage: OmdsGetRecordImage
    private lateinit var liveviewMagnify: OmdsOpcLiveviewMagnifyControl
    private lateinit var zoomLensControl: OmdsZoomLensControl
    private lateinit var digitalZoomControl: OmdsDigitalZoomControl

    private var isInitialized  = false
    private var cameraProtocol: ICameraConnectionStatus.CameraProtocol = ICameraConnectionStatus.CameraProtocol.OPC

    override fun initialize(imageReceiver: IImageDataReceiver)
    {
        if (!isInitialized)
        {
            try
            {
                this.cameraConnection = OmdsCameraConnection(statusWatcher, this)
                this.runModeControl = OmdsRunModeControl()
                this.commPathControl = OmdsCommPathControl()
                this.timeSync = OmdsTimeSync()
                this.cameraHardwareInformation = OmdsCameraHardwareInformation()
                this.getCameraProperty = OmdsCameraGetProperty()
                this.getCommand = OmdsGetCommand()
                this.postCommand = OmdsPostCommand()
                this.camInState = OmdsCamIndStatus()
                this.camCommPathStatus = OmdsCommPathStatus()
                this.focusControl = OmdsFocusControl()
                this.captureControl = OmdsCaptureControl(statusWatcher)
                this.getRecordImage = OmdsGetRecordImage()
                this.liveviewMagnify = OmdsOpcLiveviewMagnifyControl()
                this.zoomLensControl = OmdsZoomLensControl()
                this.digitalZoomControl = OmdsDigitalZoomControl()
                this.subscriberList.clear()
                this.connectionStatusReceiverList.clear()

                this.liveviewControl.setReceiver(imageReceiver, statusWatcher)

                isInitialized = true
            }
            catch (e: Exception)
            {
                e.printStackTrace()
                isInitialized = false
            }
        }
    }

    override fun connectToCamera()
    {
        Log.v(TAG, " connectToCamera() : OMDS ")
        try
        {
            cameraConnection.connect()
        }
        catch (e : Exception)
        {
            e.printStackTrace()
        }
    }

    override fun disconnectFromCamera()
    {
        Log.v(TAG, " disconnectFromCamera() : OMDS ")
        try
        {
            statusWatcher.stopStatusWatch()
            cameraConnection.disconnect(false)
        }
        catch (e : Exception)
        {
            e.printStackTrace()
        }
    }

    override fun startCamera(context: Context)
    {
        try
        {
            Log.v(TAG, " startCamera() : OMDS ")
            cameraConnection.startWatchWifiStatus(context)
        }
        catch (e : Exception)
        {
            e.printStackTrace()
        }
    }

    override fun finishCamera(isPowerOff: Boolean)
    {
        try
        {
            Log.v(TAG, " finishCamera() : $isPowerOff ")
            statusWatcher.stopStatusWatch()
            cameraConnection.disconnect(isPowerOff)
            cameraConnection.release()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    override fun getCameraConnectionStatus() : CameraConnectionStatus { return cameraConnectionStatus }

    override fun changeRunMode(runMode: String, callback: IOperationCallback)
    {
        try
        {
            if (::runModeControl.isInitialized)
            {
                runModeControl.changeRunMode(runMode, callback)
                return
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        callback.operationResult(false, "ERROR")
    }

    override fun subscribeCameraConnectionStatus(receiver: ICameraConnectionStatus)
    {
        try
        {
            Log.v(TAG, "subscribeCameraConnectionStatus()")
            connectionStatusReceiverList.add(receiver)
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    override fun unsubscribeCameraConnectionStatus(receiver: ICameraConnectionStatus)
    {
        try
        {
            Log.v(TAG, "unsubscribeCameraConnectionStatus() ")
            connectionStatusReceiverList.remove(receiver)
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    override fun needRotateImage(): Boolean { return false }

    override fun getCameraStatus() : ICameraStatus { return statusWatcher }

    override fun getFocusingControl(): IFocusingControl { return focusControl }

    override fun getZoomControl(): IZoomLensControl { return zoomLensControl }

    override fun getCaptureControl(): ICaptureControl { return captureControl }

    override fun getGetRecordImage(): IGetRecordImage { return getRecordImage }

    override fun getLiveviewMagnify(): ICameraLiveviewMagnify { return liveviewMagnify }

    override fun getDigitalZoomControl(): IDigitalZoomControl { return digitalZoomControl }

    override fun getCameraHardwareInformation(): ICameraHardwareInformation { return cameraHardwareInformation }

    override fun getCameraConnectionProtocol(): ICameraConnectionStatus.CameraProtocol { return cameraProtocol }

    override fun onStatusNotify(status: CameraConnectionStatus)
    {
        try
        {
            // ----- TODO： Run mode を加味してライブビューの制御を行う必要があるかも
            if ((cameraConnectionStatus != CameraConnectionStatus.CONNECTED)&&(status == CameraConnectionStatus.CONNECTED))
            {
                // ----- カメラとの接続ができた... Liveview を開始する
                liveviewControl.startLiveView()
            }
            else if ((cameraConnectionStatus == CameraConnectionStatus.CONNECTED)&&(status != CameraConnectionStatus.CONNECTED))
            {
                // ----- カメラとの接続が切れた... Liveview を停止する
                liveviewControl.stopLiveView()
            }
            // ---- ステータス変更を通知する
            connectionStatusReceiverList.forEach { subscriber ->
                try
                {
                    subscriber.onStatusNotify(status)
                }
                catch (e: Exception)
                {
                    e.printStackTrace()
                }
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        // ----- 現在のステータスを保管する
        cameraConnectionStatus = status
    }

    override fun detectCameraProtocol(protocol: ICameraConnectionStatus.CameraProtocol)
    {
        // --- カメラと接続した時に検出したプロトコルが違ったら、変更を行う
        if (cameraProtocol != protocol)
        {
            cameraProtocol = protocol

            // プロトコルが変わった場合は、各制御クラスにプロトコルを通知して切り替える
            val isOpcProtocol = (protocol == ICameraConnectionStatus.CameraProtocol.OPC)
            statusWatcher.setUseOpcProtocol(isOpcProtocol)
            runModeControl.setUseOpcProtocol(isOpcProtocol)
            commPathControl.setUseOpcProtocol(isOpcProtocol)
            timeSync.setUseOpcProtocol(isOpcProtocol)
            cameraHardwareInformation.setUseOpcProtocol(isOpcProtocol)
            getCameraProperty.setUseOpcProtocol(isOpcProtocol)
            camInState.setUseOpcProtocol(isOpcProtocol)
            camCommPathStatus.setUseOpcProtocol(isOpcProtocol)
            focusControl.setUseOpcProtocol(isOpcProtocol)
            captureControl.setUseOpcProtocol(isOpcProtocol)
            getRecordImage.setUseOpcProtocol(isOpcProtocol)
            liveviewMagnify.setUseOpcProtocol(isOpcProtocol)
            zoomLensControl.setUseOpcProtocol(isOpcProtocol)
            digitalZoomControl.setUseOpcProtocol(isOpcProtocol)
        }
    }

    override fun subscribeEventReceiver(subscriber: ICameraEventNotify)
    {
        try
        {
            Log.v(TAG, "subscribeCameraEvent() : ${subscriber.getSubscribeId()}")
            subscriberList.add(subscriber)
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    override fun unsubscribeEventReceiver(subscriber: ICameraEventNotify)
    {
        try
        {
            Log.v(TAG, "unsubscribeCameraEvent() : ${subscriber.getSubscribeId()}")
            subscriberList.remove(subscriber)
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    override fun subscribeCameraStatus(subscriber: ICameraStatusUpdateNotify) {
        statusWatcher.subscribe(subscriber)
    }

    override fun unsubscribeCameraStatus(subscriber: ICameraStatusUpdateNotify) {
        statusWatcher.unsubscribe(subscriber)
    }

    override fun receivedOpcEvent(value: ByteArray)
    {
        try
        {
            //Log.v(TAG, "receivedCameraEvent() [subscriber: ${subscriberList.size}]")
            subscriberList.forEach { subscriber ->
                try
                {
                    subscriber.receivedCameraEvent(value)
                }
                catch (e: Exception)
                {
                    e.printStackTrace()
                }
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    companion object
    {
        private val TAG = OmdsCameraControlSingleton::class.java.simpleName
    }
}
