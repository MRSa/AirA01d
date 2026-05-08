package jp.osdn.gokigen.aira01d.ui.model

import android.content.Context
import android.util.Log
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import jp.osdn.gokigen.a01lib.camera.interfaces.ICameraConnectionStatus
import jp.osdn.gokigen.a01lib.camera.interfaces.ICameraConnectionStatus.CameraConnectionStatus
import jp.osdn.gokigen.a01lib.camera.interfaces.ICameraEventNotify
import jp.osdn.gokigen.a01lib.camera.interfaces.ICameraStatus
import jp.osdn.gokigen.a01lib.camera.interfaces.ICameraStatusUpdateNotify
import jp.osdn.gokigen.a01lib.camera.interfaces.ICaptureControl
import jp.osdn.gokigen.aira01d.AppSingleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.text.isNotEmpty

// ----- カメラ状態を保持し、画面表示のためにデータを提供する
class CameraStatusViewModel: ViewModel(), ICameraConnectionStatus, ICameraEventNotify, ICameraStatusUpdateNotify
{
    private lateinit var cameraStatus: ICameraStatus
    private val _cameraConnectionStatus : MutableLiveData<CameraConnectionStatus> by lazy { MutableLiveData<CameraConnectionStatus>() }
    val cameraConnectionStatus: LiveData<CameraConnectionStatus> = _cameraConnectionStatus

    private val _isConnectError : MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }
    val isConnectError : LiveData<Boolean> = _isConnectError

    private val _cameraInformation : MutableLiveData<String> by lazy { MutableLiveData<String>() }
    val cameraInformation : LiveData<String> = _cameraInformation

    private val _cameraInformationLevel : MutableLiveData<Int> by lazy { MutableLiveData<Int>() }
    val cameraInformationLevel : LiveData<Int> = _cameraInformationLevel

    private val _liveviewMagnifySize : MutableLiveData<Int> by lazy { MutableLiveData<Int>() }
    val liveViewMagnifySize : LiveData<Int> = _liveviewMagnifySize

    private val _takeMode : MutableLiveData<String> by lazy { MutableLiveData<String>() }
    val takeMode : LiveData<String> = _takeMode

    private val _tv : MutableLiveData<String> by lazy { MutableLiveData<String>() }
    val tv : LiveData<String> = _tv

    private val _av : MutableLiveData<String> by lazy { MutableLiveData<String>() }
    val av : LiveData<String> = _av

    private val _xv : MutableLiveData<String> by lazy { MutableLiveData<String>() }
    val xv : LiveData<String> = _xv

    private val _sv : MutableLiveData<String> by lazy { MutableLiveData<String>() }
    val sv : LiveData<String> = _sv

    private val _wb : MutableLiveData<String> by lazy { MutableLiveData<String>() }
    val wb : LiveData<String> = _wb

    private val _focusMode : MutableLiveData<String> by lazy { MutableLiveData<String>() }
    val focusMode : LiveData<String> = _focusMode

    private val _driveMode : MutableLiveData<String> by lazy { MutableLiveData<String>() }
    val driveMode : LiveData<String> = _driveMode

    private val _pictureEffect : MutableLiveData<String> by lazy { MutableLiveData<String>() }
    val pictureEffect : LiveData<String> = _pictureEffect

    private val _exposureWarning : MutableLiveData<String> by lazy { MutableLiveData<String>() }
    val exposureWarning : LiveData<String> = _exposureWarning

    private val _rawMode : MutableLiveData<String> by lazy { MutableLiveData<String>() }
    val rawMode : LiveData<String> = _rawMode

    private val _aspectRatio : MutableLiveData<String> by lazy { MutableLiveData<String>() }
    val aspectRatio : LiveData<String> = _aspectRatio

    private val _aeLockState : MutableLiveData<String> by lazy { MutableLiveData<String>() }
    val aeLockState : LiveData<String> = _aeLockState

    private val _batteryLevel : MutableLiveData<String> by lazy { MutableLiveData<String>() }
    val batteryLevel : LiveData<String> = _batteryLevel

    private val _meteringMode : MutableLiveData<String> by lazy { MutableLiveData<String>() }
    val meteringMode : LiveData<String> = _meteringMode

    var propertyList by mutableStateOf<List<String>>(emptyList())
        private set

    var activeProperty by mutableStateOf<ICameraStatus.CameraProperty?>(null)
        private set

    fun initializeViewModel()
    {
        try
        {
            // ----- カメラの接続状態を初期化
            _cameraConnectionStatus.value = AppSingleton.cameraControl.getCameraConnectionStatus()
            AppSingleton.cameraControl.subscribeCameraConnectionStatus(this)
            AppSingleton.cameraControl.subscribeEventReceiver(this)
            AppSingleton.cameraControl.subscribeCameraStatus(this)
            // ----- （保持している）カメラ状態を更新

            cameraStatus = AppSingleton.cameraControl.getCameraStatus()

            // ----- 保持状態を初期化
            _isConnectError.postValue(false)
            _cameraInformation.postValue("")
            _cameraInformationLevel.postValue(10)
            _liveviewMagnifySize.postValue(1)
            _takeMode.postValue("")
            _tv.postValue("")
            _av.postValue("")
            _sv.postValue("")
            _xv.postValue("")
            _wb.postValue("")
            _focusMode.postValue("")
            _exposureWarning.postValue("")
            _driveMode.postValue("")
            _batteryLevel.postValue("")
            _meteringMode.postValue("")
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
    override fun receivedCameraEvent(eventMessage: ByteArray)
    {
        // ----- カメラのステータスが変化したときの処理
        if (eventMessage.size < 4) return  // 期待した文字列の長さがなかったので解析はしない
        val appId = eventMessage[0]
        val event = (eventMessage[1].toInt() and 0xFF).toByte() // eventMessage[1]
        val length = ((eventMessage[2].toInt() and 0xFF) shl 8) or (eventMessage[3].toInt() and 0xFF)
        val dataBody = String(eventMessage, 4, eventMessage.size - 4, Charsets.UTF_8)
        Log.v(TAG, " - - - - - - - - - receivedCameraEvent(CameraStatusViewModel) : $appId [evt:$event] len:$length   '$dataBody'")
    }

    override fun changedTakeMode(newMode: String)
    {
        if ((newMode.isNotEmpty())&&(_takeMode.value != newMode))
        {
            _takeMode.postValue(newMode)
        }
    }

    override fun updatedShutterSpeed(tv: String) {
        if ((tv.isNotEmpty())&&(_tv.value != tv))
        {
            _tv.postValue(tv)
        }
    }

    override fun updatedAperture(av: String) {
        if ((av.isNotEmpty())&&(_av.value != av))
        {
            _av.postValue(av)
        }
    }

    override fun updatedExposureCompensation(xv: String) {
        if (_xv.value != xv)
        {
            _xv.postValue(xv)
        }
    }

    override fun updateIsoSensitivity(sv: String) {
        if ((sv.isNotEmpty())&&(_sv.value != sv))
        {
            _sv.postValue(sv)
        }
    }

    override fun updateFocusMode(focusMode: String) {
        if ((focusMode.isNotEmpty())&&(_focusMode.value != focusMode))
        {
            _focusMode.postValue(focusMode)
        }
    }

    override fun updateExposureWarning(exposureWarning: String) {
        if (_exposureWarning.value != exposureWarning)
        {
            _exposureWarning.postValue(exposureWarning)
        }
    }

    override fun updateDriveMode(driveMode: String) {
        if ((driveMode.isNotEmpty())&&(_driveMode.value != driveMode))
        {
            _driveMode.postValue(driveMode)
        }
    }

    override fun updatePictureEffect(pictureEffect: String) {
        Log.v(TAG, "PICTURE EFFECT: $pictureEffect")
        if ((pictureEffect.isNotEmpty())&&(_pictureEffect.value != pictureEffect))
        {
            _pictureEffect.postValue(pictureEffect)
        }
    }

    override fun updatedWhiteBalance(whiteBalance: String) {
        if ((whiteBalance.isNotEmpty())&&(_wb.value != whiteBalance))
        {
            _wb.postValue(whiteBalance)
        }
    }

    override fun updatedRawMode(rawMode: String) {
        Log.v(TAG, "RAW MODE: $rawMode")
        if ((rawMode.isNotEmpty())&&(_rawMode.value != rawMode))
        {
            _rawMode.postValue(rawMode)
        }
    }

    override fun updatedAspectRatio(aspectRatio: String) {
        Log.v(TAG, "ASPECT RATIO: $aspectRatio")
        if ((aspectRatio.isNotEmpty())&&(_aspectRatio.value != aspectRatio))
        {
            _aspectRatio.postValue(aspectRatio)
        }
    }

    override fun updatedAeLockState(aeLockState: String) {
        if ((aeLockState.isNotEmpty())&&(_aeLockState.value != aeLockState))
        {
            _aeLockState.postValue(aeLockState)
        }
    }

    override fun updateRemainBattery(batteryLevel: String) {
        if ((batteryLevel.isNotEmpty())&&(_batteryLevel.value != batteryLevel))
        {
            _batteryLevel.postValue(batteryLevel)
        }
    }

    override fun updatedMeteringMode(meteringMode: String) {
        if ((meteringMode.isNotEmpty())&&(_meteringMode.value != meteringMode))
        {
            _meteringMode.postValue(meteringMode)
        }
    }

    fun getPropertyValueList(key: ICameraStatus.CameraProperty) : List<String>
    {
        try
        {
            return AppSingleton.cameraControl.getCameraStatus().getStatusList(key)
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        return ArrayList()
    }

    fun loadPropertyList(key: ICameraStatus.CameraProperty)
    {
        if (activeProperty != null) return  // 既に loadPropertyListが動作中
        activeProperty = key
        propertyList = emptyList()

        viewModelScope.launch(Dispatchers.IO) {
            activeProperty = key
            try
            {
                val result = getPropertyValueList(key)
                withContext(Dispatchers.Main) {
                    // Log.v(TAG, "Received property list: $result (size: ${result.size})")
                    if (activeProperty == key) {
                        propertyList = result
                    }
                }
            }
            catch (e: Exception)
            {
                withContext(Dispatchers.Main) {
                    activeProperty = null
                    e.printStackTrace()
                }
            }
        }
    }

    // ダイアログを閉じたことをViewModelに知らせる（状態リセット用）
    fun onSelectPropertyDialogDismissed() {
        activeProperty = null
        propertyList = emptyList()
    }

    fun setProperty(key: ICameraStatus.CameraProperty, value: String)
    {
        // ----- カメラに対して選択されたプロパティを設定する
        CoroutineScope(Dispatchers.IO).launch {
            AppSingleton.cameraControl.getCameraStatus().setStatus(key, value)
        }
    }

    // ----- カメラの電源をOFFにしてから、アプリケーションを終了させる
    fun confirmPowerOff(onFinish: () -> Unit)
    {
        CoroutineScope(Dispatchers.Main).launch {
            try
            {
                AppSingleton.cameraControl.finishCamera(true)
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
                AppSingleton.cameraControl.disconnectFromCamera()
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
                AppSingleton.cameraControl.connectToCamera()
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
                AppSingleton.cameraControl.startCamera(context)
            }
            catch (e: Exception)
            {
                e.printStackTrace()
            }
        }
    }

    // ----- 撮影を行う
    fun doCapture(captureAction: ICaptureControl.CaptureAction)
    {
        CoroutineScope(Dispatchers.Main).launch {
            try
            {
                AppSingleton.cameraControl.getCaptureControl().doCapture(captureAction)
            }
            catch (e: Exception)
            {
                e.printStackTrace()
            }
        }
    }

    fun changeLiveviewScale()
    {
        val nextSize = when (_liveviewMagnifySize.value)
        {
            5 -> 7
            7 -> 10
            10 -> 14
            14 -> 1
            else -> 5
        }

        // ----- カメラに対して選択されたプロパティを設定する
        CoroutineScope(Dispatchers.IO).launch {
            // ----- ライブビューの画像拡大
            when (nextSize) {
                1 -> {
                    // ----- ライブビューの拡大を止める
                    AppSingleton.cameraControl.getLiveviewMagnify().stopMagnify()
                }
                5 -> {
                    // ----- ライブビューの拡大をスタート
                    AppSingleton.cameraControl.getLiveviewMagnify().startMagnify(nextSize)
                }
                else -> {
                    // ----- ライブビューの拡大サイズを変更する
                    AppSingleton.cameraControl.getLiveviewMagnify().changeMagnify(nextSize)
                }
            }
        }
        _liveviewMagnifySize.postValue(nextSize)
    }

    companion object
    {
        private val TAG = CameraStatusViewModel::class.java.simpleName
    }
}
