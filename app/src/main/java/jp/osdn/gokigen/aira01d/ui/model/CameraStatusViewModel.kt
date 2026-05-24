package jp.osdn.gokigen.aira01d.ui.model

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import jp.osdn.gokigen.a01lib.camera.interfaces.ICameraConnectionStatus
import jp.osdn.gokigen.a01lib.camera.interfaces.ICameraConnectionStatus.CameraConnectionStatus
import jp.osdn.gokigen.a01lib.camera.interfaces.ICameraEventNotify
import jp.osdn.gokigen.a01lib.camera.interfaces.ICameraHardwareInformation
import jp.osdn.gokigen.a01lib.camera.interfaces.ICameraHardwareInformation.Hardware
import jp.osdn.gokigen.a01lib.camera.interfaces.ICameraStatus
import jp.osdn.gokigen.a01lib.camera.interfaces.ICameraStatusUpdateNotify
import jp.osdn.gokigen.a01lib.camera.interfaces.ICaptureControl
import jp.osdn.gokigen.a01lib.camera.interfaces.IDigitalZoomControl
import jp.osdn.gokigen.aira01d.AppSingleton
import jp.osdn.gokigen.aira01d.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt
import kotlin.text.isNotEmpty

// ----- カメラ状態を保持し、画面表示のためにデータを提供する
class CameraStatusViewModel : ViewModel(), ICameraConnectionStatus, ICameraEventNotify, ICameraStatusUpdateNotify {

    private lateinit var cameraStatus: ICameraStatus

    // ----- LiveData群（カプセル化構造を維持しつつ、by lazy を排してシンプルに宣言）
    private val _cameraConnectionStatus = MutableLiveData<CameraConnectionStatus>()
    val cameraConnectionStatus: LiveData<CameraConnectionStatus> = _cameraConnectionStatus

    private val _isConnectError = MutableLiveData<Boolean>()
    val isConnectError: LiveData<Boolean> = _isConnectError

    private val _isMediaBusy = MutableLiveData<Boolean>()
    val isMediaBusy: LiveData<Boolean> = _isMediaBusy

    private val _isCaptureActivated = MutableLiveData<Boolean>()
    val isCaptureActivated: LiveData<Boolean> = _isCaptureActivated

    private val _cameraInformation = MutableLiveData<String>()
    val cameraInformation: LiveData<String> = _cameraInformation

    private val _cameraInformationLevel = MutableLiveData<Int>()
    val cameraInformationLevel: LiveData<Int> = _cameraInformationLevel

    private val _liveviewMagnifySize = MutableLiveData<Int>()
    val liveViewMagnifySize: LiveData<Int> = _liveviewMagnifySize

    private val _focalLengthNow = MutableLiveData<Int>()
    val focalLengthNow: LiveData<Int> = _focalLengthNow

    private val _focalLengthWide = MutableLiveData<Int>()
    val focalLengthWide: LiveData<Int> = _focalLengthWide

    private val _focalLengthTele = MutableLiveData<Int>()
    val focalLengthTele: LiveData<Int> = _focalLengthTele

    private val _digitalZoomScaleMin = MutableLiveData<Int>()
    val digitalZoomScaleMin: LiveData<Int> = _digitalZoomScaleMin

    private val _digitalZoomScaleMax = MutableLiveData<Int>()
    val digitalZoomScaleMax: LiveData<Int> = _digitalZoomScaleMax

    private val _digitalZoomScaleCurrent = MutableLiveData<Int>()
    val digitalZoomScaleCurrent: LiveData<Int> = _digitalZoomScaleCurrent

    private val _takeMode = MutableLiveData<String>()
    val takeMode: LiveData<String> = _takeMode

    private val _tv = MutableLiveData<String>()
    val tv: LiveData<String> = _tv

    private val _av = MutableLiveData<String>()
    val av: LiveData<String> = _av

    private val _xv = MutableLiveData<String>()
    val xv: LiveData<String> = _xv

    private val _sv = MutableLiveData<String>()
    val sv: LiveData<String> = _sv

    private val _wb = MutableLiveData<String>()
    val wb: LiveData<String> = _wb

    private val _focusMode = MutableLiveData<String>()
    val focusMode: LiveData<String> = _focusMode

    private val _driveMode = MutableLiveData<String>()
    val driveMode: LiveData<String> = _driveMode

    private val _pictureEffect = MutableLiveData<String>()
    val pictureEffect: LiveData<String> = _pictureEffect

    private val _artFilter = MutableLiveData<String>()
    val artFilter: LiveData<String> = _artFilter

    private val _exposureWarningResId = MutableLiveData<Int>()
    val exposureWarningResId: LiveData<Int> = _exposureWarningResId

    private val _exposureWarningLevel = MutableLiveData<Int>()
    val exposureWarningLevel: LiveData<Int> = _exposureWarningLevel

    private val _rawMode = MutableLiveData<String>()
    val rawMode: LiveData<String> = _rawMode

    private val _aspectRatio = MutableLiveData<String>()
    val aspectRatio: LiveData<String> = _aspectRatio

    private val _aeLockState = MutableLiveData<String>()
    val aeLockState: LiveData<String> = _aeLockState

    private val _batteryLevel = MutableLiveData<String>()
    val batteryLevel: LiveData<String> = _batteryLevel

    private val _meteringMode = MutableLiveData<String>()
    val meteringMode: LiveData<String> = _meteringMode

    private val _electricZoom = MutableLiveData<String>()
    val electricZoom: LiveData<String> = _electricZoom

    private val _checkingCameraHardware = MutableLiveData<Boolean>()
    val checkingCameraHardware: LiveData<Boolean> = _checkingCameraHardware

    // ----- MediatorLiveData
    val digitalZoomScaleList = MediatorLiveData<List<Int>>().apply {
        addSource(digitalZoomScaleMin) { updateDigitalZoomScaleList() }
        addSource(digitalZoomScaleMax) { updateDigitalZoomScaleList() }
        addSource(digitalZoomScaleCurrent) { updateDigitalZoomScaleList() }
    }

    val focalLengthList = MediatorLiveData<List<Int>>().apply {
        addSource(focalLengthWide) { updateFocalList() }
        addSource(focalLengthTele) { updateFocalList() }
        addSource(focalLengthNow) { updateFocalList() }
    }

    // ----- プロパティリストの保持
    var propertyList by mutableStateOf<List<String>>(emptyList())
        private set

    var activeProperty by mutableStateOf<ICameraStatus.CameraProperty?>(null)
        private set

    var queryDigitalZoom by mutableStateOf(false)
        private set

    var canUseDigitalZoom by mutableStateOf(false)
        private set

    var propertyDescriptor by mutableStateOf(
        ICameraStatus.CameraPropertyDescriptor(
            propertyName = "",
            attribute = "",
            current = "",
            values = emptyList()
        ))
        private set

    var queryPropertyName by mutableStateOf<String?>(null)
        private set

    // ----- デジタルズームの倍率変更ダイアログの表示制御
    private val _showDigitalZoomScaleDialog = MutableLiveData(false)
    val showDigitalZoomScaleDialog: LiveData<Boolean> = _showDigitalZoomScaleDialog

    fun initializeViewModel()
    {
        try {
            // ----- UIスレッドの初期化なので .value を使用する
            _cameraConnectionStatus.value = AppSingleton.cameraControl.getCameraConnectionStatus()
            AppSingleton.cameraControl.subscribeCameraConnectionStatus(this)
            AppSingleton.cameraControl.subscribeEventReceiver(this)
            AppSingleton.cameraControl.subscribeCameraStatus(this)

            cameraStatus = AppSingleton.cameraControl.getCameraStatus()

            _isConnectError.value = false
            _isCaptureActivated.value = false
            _isMediaBusy.value = false
            _cameraInformation.value = ""
            _cameraInformationLevel.value = 10
            _exposureWarningLevel.value = 10
            _liveviewMagnifySize.value = 1
            _focalLengthNow.value = 0
            _focalLengthWide.value = 0
            _focalLengthTele.value = 0
            _takeMode.value = ""
            _tv.value = ""
            _av.value = ""
            _sv.value = ""
            _xv.value = ""
            _wb.value = ""
            _focusMode.value = ""
            _exposureWarningResId.value = R.string.blank
            _driveMode.value = ""
            _batteryLevel.value = ""
            _meteringMode.value = ""
            _electricZoom.value = ""
            _checkingCameraHardware.value = false
            _digitalZoomScaleMin.value = 100
            _digitalZoomScaleMax.value = 100
            _digitalZoomScaleCurrent.value = 100
            _showDigitalZoomScaleDialog.value = false
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
            // カメラからの通知はバックグラウンドスレッドの可能性があるため postValue を使用
            val currentStatus = _cameraConnectionStatus.value
            if ((status == CameraConnectionStatus.NOT_FOUND) &&
                ((currentStatus == CameraConnectionStatus.CHECK_WIFI) ||
                        (currentStatus == CameraConnectionStatus.CONNECTING) ||
                        (currentStatus == CameraConnectionStatus.NOT_FOUND)))
            {
                _isConnectError.postValue(true)
            } else if ((status == CameraConnectionStatus.ERROR) || (status == CameraConnectionStatus.EXCEPTION)) {
                _isConnectError.postValue(true)
            } else {
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

    override fun detectCameraProtocol(protocol: ICameraConnectionStatus.CameraProtocol)
    {
        Log.v(TAG, "CAMERA PROTOCOL: $protocol")
    }

    override fun getSubscribeId(): String { return "CameraStatusViewModel" }

    // ----- Liveview Header解析後、呼び出される関数群 -----

    override fun receivedCameraEvent(eventMessage: ByteArray)
    {
        if (eventMessage.size < 4) return
        val appId = eventMessage[0]
        val event = (eventMessage[1].toInt() and 0xFF).toByte()
        val length = ((eventMessage[2].toInt() and 0xFF) shl 8) or (eventMessage[3].toInt() and 0xFF)
        val dataBody = String(eventMessage, 4, eventMessage.size - 4, Charsets.UTF_8)
        Log.v(TAG, " - - - - - - - - - receivedCameraEvent(CameraStatusViewModel) : $appId [evt:$event] len:$length   '$dataBody'")
    }

    override fun changedTakeMode(newMode: String) {
        if (newMode.isNotEmpty() && _takeMode.value != newMode) {
            _takeMode.postValue(newMode)
        }
    }

    override fun updatedShutterSpeed(tv: String) {
        if (tv.isNotEmpty() && _tv.value != tv) {
            _tv.postValue(tv)
        }
    }

    override fun updatedAperture(av: String) {
        if (av.isNotEmpty() && _av.value != av) {
            _av.postValue(av)
        }
    }

    override fun updatedExposureCompensation(xv: String) {
        if (_xv.value != xv) {
            _xv.postValue(xv)
        }
    }

    override fun updateIsoSensitivity(sv: String) {
        if (sv.isNotEmpty() && _sv.value != sv) {
            _sv.postValue(sv)
        }
    }

    override fun updateFocusMode(focusMode: String) {
        if (focusMode.isNotEmpty() && _focusMode.value != focusMode) {
            _focusMode.postValue(focusMode)
        }
    }

    override fun updateExposureWarning(exposureWarning: Int) {
        if (exposureWarning > 0) {
            _exposureWarningResId.postValue(R.string.exposure_warning)
            _exposureWarningLevel.postValue(4)
        } else {
            _exposureWarningResId.postValue(R.string.blank)
            _exposureWarningLevel.postValue(10)
        }
    }

    override fun updateDriveMode(driveMode: String) {
        if (driveMode.isNotEmpty() && _driveMode.value != driveMode) {
            _driveMode.postValue(driveMode)
        }
    }

    override fun updatePictureEffect(pictureEffect: String) {
        Log.v(TAG, "PICTURE EFFECT: $pictureEffect")
        if (pictureEffect.isNotEmpty() && _pictureEffect.value != pictureEffect) {
            _pictureEffect.postValue(pictureEffect)
        }
    }

    override fun updateArtFilter(artFilter: String) {
        if (artFilter.isNotEmpty() && _artFilter.value != artFilter) {
            _artFilter.postValue(artFilter)
        }
    }

    override fun updatedWhiteBalance(whiteBalance: String) {
        if (whiteBalance.isNotEmpty() && _wb.value != whiteBalance) {
            _wb.postValue(whiteBalance)
        }
    }

    override fun updatedRawMode(rawMode: String) {
        Log.v(TAG, "RAW MODE: $rawMode")
        if (rawMode.isNotEmpty() && _rawMode.value != rawMode) {
            _rawMode.postValue(rawMode)
        }
    }

    override fun updatedAspectRatio(aspectRatio: String) {
        Log.v(TAG, "ASPECT RATIO: $aspectRatio")
        if (aspectRatio.isNotEmpty() && _aspectRatio.value != aspectRatio) {
            _aspectRatio.postValue(aspectRatio)
        }
    }

    override fun updatedAeLockState(aeLockState: String) {
        if (aeLockState.isNotEmpty() && _aeLockState.value != aeLockState) {
            _aeLockState.postValue(aeLockState)
        }
    }

    override fun updateRemainBattery(batteryLevel: String) {
        if (batteryLevel.isNotEmpty() && _batteryLevel.value != batteryLevel) {
            _batteryLevel.postValue(batteryLevel)
        }
    }

    override fun updatedMeteringMode(meteringMode: String) {
        if (meteringMode.isNotEmpty() && _meteringMode.value != meteringMode) {
            _meteringMode.postValue(meteringMode)
        }
    }

    override fun updatedMediaStatus(mediaStatus: Int) {
        val mask = 0b11110000
        _isMediaBusy.postValue((mediaStatus and mask) != 0)
    }

    override fun updatedOrientation(orientation: Int) {}

    override fun updatedAvailableShots(numOfImages: Int) {}

    override fun updatedZoomInfo(wide: Int, current: Int, tele: Int) {
        _focalLengthNow.postValue(current)
        _focalLengthWide.postValue(wide)
        _focalLengthTele.postValue(tele)
    }

    override fun updatedLevelGauge(accuracy: Int, orientation: Int, roll: Int, pitch: Int) {}

    private fun getPropertyValueList(key: ICameraStatus.CameraProperty): List<String> {
        try {
            return AppSingleton.cameraControl.getCameraStatus().getStatusList(key)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ArrayList()
    }

    fun getPropertyDescriptor(propertyName: String)
    {
        // ディスクリプタの問い合わせ
        if (queryPropertyName != null)
        {
            Log.v(TAG, "getPropertyDescriptor($propertyName) : Now Querying [$queryPropertyName]")
            return // 既に問い合わせ中の時は、何もしない
        }
        Log.v(TAG, "--- getPropertyDescriptor($propertyName)")

        queryPropertyName = propertyName
        propertyDescriptor = ICameraStatus.CameraPropertyDescriptor(
                propertyName = "",
                attribute = "",
                current = "",
                values = emptyList()
        )

        viewModelScope.launch {
            try
            {
                val descriptor = withContext(Dispatchers.IO) {
                    AppSingleton.cameraControl.getCameraStatus().getDescriptor(propertyName)
                }
                if (queryPropertyName == propertyName) {
                    propertyDescriptor = descriptor
                    Log.v(TAG, " --- RECEIVED PROPERTY DESCRIPTOR : $queryPropertyName (${propertyDescriptor.current})")
                    if (propertyDescriptor.values.isEmpty())
                    {
                        // ---- うまく選択肢のデータが取れなかった... "待ち"を解除する
                        Log.v(TAG, "      DESCRIPTOR IS NONE...(${propertyDescriptor.propertyName})")
                        queryPropertyName = null
                    }
                }
                else
                {
                    // --- 待っているのと違うのが送られてきた... 無視（待ちを解除）する
                    Log.v(TAG, "--- Received Wrong Property: $queryPropertyName (wait: $propertyName)")
                    queryPropertyName = null
                }
            }
            catch (e: Exception)
            {
                Log.e("CameraViewModel", "Failed to get descriptor", e)
                queryPropertyName = null
            }
        }
    }

    fun onDismissedPropertyDescriptor()
    {
        queryPropertyName = null
        propertyDescriptor = ICameraStatus.CameraPropertyDescriptor(
            propertyName = "",
            attribute = "",
            current = "",
            values = emptyList()
        )
    }

    fun setPropertyString(propertyName: String, value: String) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    AppSingleton.cameraControl.getCameraStatus().setStatusString(propertyName, value)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun loadPropertyList(key: ICameraStatus.CameraProperty) {
        if (activeProperty != null) return
        activeProperty = key
        propertyList = emptyList()

        viewModelScope.launch {
            try
            {
                // Dispatchers.IO で非同期取得
                val result = withContext(Dispatchers.IO) {
                    getPropertyValueList(key)
                }
                // メインスレッドで安全にComposeのStateへ反映
                if (activeProperty == key) {
                    propertyList = result
                }
            }
            catch (e: Exception)
            {
                activeProperty = null
                e.printStackTrace()
            }
        }
    }

    fun onSelectPropertyDialogDismissed() {
        activeProperty = null
        propertyList = emptyList()
    }

    fun setProperty(key: ICameraStatus.CameraProperty, value: String) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    AppSingleton.cameraControl.getCameraStatus().setStatus(key, value)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun confirmPowerOff(onFinish: () -> Unit) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    AppSingleton.cameraControl.finishCamera(true)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                viewModelScope.launch(Dispatchers.Main) {
                    AppSingleton.destroyCameraControl()
                    onFinish()
                }
            }
        }
    }

    fun disconnectFromCamera() {
        viewModelScope.launch {
            try {
                _isConnectError.value = false
                withContext(Dispatchers.IO) {
                    AppSingleton.cameraControl.disconnectFromCamera()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun connectToCamera() {
        viewModelScope.launch {
            try {
                _isConnectError.value = false
                withContext(Dispatchers.IO) {
                    AppSingleton.cameraControl.connectToCamera()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun startCamera(context: Context)
    {
        viewModelScope.launch {
            try
            {
                _isConnectError.value = false
                withContext(Dispatchers.IO) {
                    AppSingleton.cameraControl.startCamera(context)
                }
            }
            catch (e: Exception)
            {
                e.printStackTrace()
            }
        }
    }

    fun tryCapture()
    {
        try
        {
            val captureAction = ICaptureControl.CaptureAction.TOGGLE
            val isMovie = (_takeMode.value == "movie")
            val isContinuous = !(_driveMode.value ?: "").contains("NORMAL")

            // UI反映用のLiveDataは、ボタンを押した瞬間に Main スレッドで即時反映 (.value)
            if (isMovie || isContinuous) {
                val captureStatus = _isCaptureActivated.value ?: true
                _isCaptureActivated.value = !captureStatus
            } else {
                _isCaptureActivated.value = false
            }

            viewModelScope.launch {
                try {
                    withContext(Dispatchers.IO) {
                        if (isMovie) {
                            AppSingleton.cameraControl.getCaptureControl().doMovie(captureAction)
                        } else {
                            AppSingleton.cameraControl.getCaptureControl().doCapture(captureAction)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun changeAELockState(isLock: Boolean) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    if (isLock) {
                        AppSingleton.cameraControl.getFocusingControl().lockAutoExposure()
                    } else {
                        AppSingleton.cameraControl.getFocusingControl().unlockAutoExposure()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun changeLiveviewScale() {
        val nextSize = when (_liveviewMagnifySize.value) {
            5 -> 7
            7 -> 10
            10 -> 14
            14 -> 1
            else -> 5
        }

        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    when (nextSize) {
                        1 -> AppSingleton.cameraControl.getLiveviewMagnify().stopMagnify()
                        5 -> AppSingleton.cameraControl.getLiveviewMagnify().startMagnify(nextSize)
                        else -> AppSingleton.cameraControl.getLiveviewMagnify().changeMagnify(nextSize)
                    }
                }
                _liveviewMagnifySize.value = nextSize // 完了後に即時反映
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun driveZoomLens(focalLength: Int) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    AppSingleton.cameraControl.getZoomControl().driveZoomLens(focalLength.toFloat())
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun checkElectricZoom() {
        _checkingCameraHardware.value = true
        _electricZoom.value = ""

        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    AppSingleton.cameraControl.getCameraHardwareInformation().getCameraStatus(
                        object : ICameraHardwareInformation.Callback {
                            override fun operationResult(result: Map<String, String?>) {
                                val zoom = result[Hardware.ELECTRICZOOM] ?: ""
                                Log.v(TAG, "ELECTRIC ZOOM: $zoom  ${_focalLengthWide.value}mm - ${_focalLengthTele.value}mm  (${_focalLengthNow.value}mm)")

                                // コールバックからメインスレッドへ復帰して反映
                                viewModelScope.launch(Dispatchers.Main) {
                                    _electricZoom.value = zoom.ifEmpty { "NG" }
                                    _checkingCameraHardware.value = false
                                }
                            }
                        }
                    )
                }
            } catch (e: Exception) {
                _checkingCameraHardware.value = false
                e.printStackTrace()
            }
        }
    }

    fun clearElectricZoomInfo() {
        _electricZoom.value = ""
    }

    fun checkDigitalZoomScale()
    {
        if (queryDigitalZoom) return // 確認中の時には処理をしない
        queryDigitalZoom = true

        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    AppSingleton.cameraControl.getDigitalZoomControl().getDigitalScopeScale(
                        object : IDigitalZoomControl.DigitalZoomScaleCallback {
                            override fun zoomScale(lowerScale: Int, upperScale: Int) {
                                viewModelScope.launch(Dispatchers.Main) {
                                    _digitalZoomScaleMin.value = lowerScale
                                    _digitalZoomScaleMax.value = upperScale
                                    canUseDigitalZoom = lowerScale < upperScale
                                    queryDigitalZoom = false

                                    // ズーム可能なら ViewModel 側の表示状態フラグを立てる
                                    if (canUseDigitalZoom) {
                                        _showDigitalZoomScaleDialog.value = true
                                    }
                                }
                            }
                        }
                    )
                }
            } catch (e: Exception) {
                queryDigitalZoom = false
                e.printStackTrace()
            }
        }
    }

    fun changeDigitalZoomScale(focalLength: Int) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    AppSingleton.cameraControl.getDigitalZoomControl().changeDigitalZoomScale(focalLength)
                }
                _digitalZoomScaleCurrent.value = focalLength
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun dismissDigitalZoomScaleDialog() {
        _showDigitalZoomScaleDialog.value = false
    }

    private fun updateFocalList() {
        val wide = focalLengthWide.value ?: 0
        val tele = focalLengthTele.value ?: 0
        val now = focalLengthNow.value ?: 0
        if (wide >= tele) {
            focalLengthList.value = emptyList()
            return
        }

        val targetSize = 10  // 10段階
        val step = (tele - wide).toDouble() / (targetSize - 1)
        val list = (0 until targetSize).map { (wide + it * step).roundToInt() }.toMutableList()

        if (now !in list) list.add(now)
        focalLengthList.value = list.sorted()
    }

    private fun updateDigitalZoomScaleList() {
        val scaleMin = digitalZoomScaleMin.value ?: 0
        val scaleMax = digitalZoomScaleMax.value ?: 0
        val now = digitalZoomScaleCurrent.value ?: 0
        if (scaleMin >= scaleMax) {
            digitalZoomScaleList.value = emptyList()
            return
        }

        val targetSize = 9
        val step = (scaleMax - scaleMin).toDouble() / (targetSize - 1)
        val list = (0 until targetSize).map { (scaleMin + it * step).roundToInt() }.toMutableList()

        if (now !in list) list.add(now)
        digitalZoomScaleList.value = list.sorted()
    }

    companion object
    {
        private val TAG = CameraStatusViewModel::class.java.simpleName
    }
}
