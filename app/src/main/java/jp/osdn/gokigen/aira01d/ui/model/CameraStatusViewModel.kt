package jp.osdn.gokigen.aira01d.ui.model

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import jp.osdn.gokigen.a01lib.camera.interfaces.ICameraConnectionStatus
import jp.osdn.gokigen.a01lib.camera.interfaces.ICameraConnectionStatus.CameraConnectionStatus
import jp.osdn.gokigen.a01lib.camera.interfaces.ICameraControl
import jp.osdn.gokigen.a01lib.camera.interfaces.ICameraEventNotify
import jp.osdn.gokigen.a01lib.camera.interfaces.screen.IAutoFocusFrameDisplay
import jp.osdn.gokigen.a01lib.camera.interfaces.screen.IIndicatorControl
import jp.osdn.gokigen.aira01d.AppSingleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// ----- カメラ状態を保持し、画面表示のためにデータを提供する
class CameraStatusViewModel: ViewModel(), ICameraConnectionStatus, ICameraEventNotify, IIndicatorControl
{
    private lateinit var cameraControl : ICameraControl

    private val _cameraConnectionStatus : MutableLiveData<CameraConnectionStatus> by lazy { MutableLiveData<CameraConnectionStatus>() }
    val cameraConnectionStatus: LiveData<CameraConnectionStatus> = _cameraConnectionStatus

    private val _isConnectError : MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }
    val isConnectError : LiveData<Boolean> = _isConnectError

    fun initializeViewModel()
    {
        try
        {
            this.cameraControl = AppSingleton.cameraControl

            // ----- カメラの接続状態を初期化
            _cameraConnectionStatus.value = cameraControl.getCameraConnectionStatus()
            cameraControl.subscribeCameraConnectionStatus(this)
            cameraControl.subscribeEventReceiver(this)
            // ----- （保持している）カメラ状態を更新

            _isConnectError.postValue(false)
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    override fun onStatusNotify(status: CameraConnectionStatus)
    {
        try
        {
            // ----- 接続エラー状態を検出する (CHECK_WIFI -> NOT_FOUNDは接続エラー)
            if ((status == CameraConnectionStatus.NOT_FOUND)&&
                ((_cameraConnectionStatus.value == CameraConnectionStatus.CHECK_WIFI)||
                (_cameraConnectionStatus.value == CameraConnectionStatus.CONNECTING)||
                (_cameraConnectionStatus.value == CameraConnectionStatus.NOT_FOUND)))
            {
                _isConnectError.postValue(true)
            }
            else
            {
                _isConnectError.postValue(false)
            }
            _cameraConnectionStatus.postValue(status)
            Log.v(TAG, "Connection : $status")
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    override fun getSubscribeId(): String { return ("CameraStatusViewModel") }
    override fun receivedCameraEvent(eventMessage: String)
    {
        // ----- カメラのステータスが変化したときの処理
    }

    // ----- カメラの電源をOFFにしてから、アプリケーションを終了させる
    fun confirmPowerOff(onFinish: () -> Unit)
    {
        CoroutineScope(Dispatchers.Main).launch {
            try
            {
                cameraControl.finishCamera(true)
                onFinish()
            }
            catch (e: Exception)
            {
                e.printStackTrace()
            }
        }
    }

    // ----- カメラから切断する
    fun disconnectFromCamera()
    {
        CoroutineScope(Dispatchers.Main).launch {
            try
            {
                _isConnectError.postValue(false)
                cameraControl.disconnectFromCamera()
            }
            catch (e: Exception)
            {
                e.printStackTrace()
            }
        }
    }

    // ----- カメラに接続する
    fun connectToCamera()
    {
        CoroutineScope(Dispatchers.Main).launch {
            try
            {
                _isConnectError.postValue(false)
                cameraControl.connectToCamera()
            }
            catch (e: Exception)
            {
                e.printStackTrace()
            }
        }
    }

    // ----- カメラに接続する
    fun startCamera(context: Context)
    {
        CoroutineScope(Dispatchers.Main).launch {
            try
            {
                _isConnectError.postValue(false)
                cameraControl.startCamera(context)
            }
            catch (e: Exception)
            {
                e.printStackTrace()
            }
        }
    }

    override fun onAfLockUpdate(focusingStatus: IAutoFocusFrameDisplay.FocusFrameStatus) {
        TODO("Not yet implemented")
    }

    override fun onShootingStatusUpdate(status: IIndicatorControl.ShootingStatus?) {
        TODO("Not yet implemented")
    }

    override fun onMovieStatusUpdate(status: IIndicatorControl.ShootingStatus?) {
        TODO("Not yet implemented")
    }

    override fun onBracketingStatusUpdate(message: String?) {
        TODO("Not yet implemented")
    }

    companion object
    {
        private val TAG = CameraStatusViewModel::class.java.simpleName
    }
}
