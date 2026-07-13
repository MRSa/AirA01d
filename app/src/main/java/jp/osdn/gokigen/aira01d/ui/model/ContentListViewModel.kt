package jp.osdn.gokigen.aira01d.ui.model

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import jp.osdn.gokigen.a01lib.camera.interfaces.ICameraConnectionStatus
import jp.osdn.gokigen.a01lib.camera.interfaces.playback.ICameraFileInfo
import jp.osdn.gokigen.a01lib.camera.interfaces.playback.IPlaybackControl
import jp.osdn.gokigen.a01lib.camera.omds.playback.OmdsFileTransfer
import jp.osdn.gokigen.a01lib.camera.utils.storage.MediaStoreStreamSaveHelper
import jp.osdn.gokigen.aira01d.AppSingleton
import jp.osdn.gokigen.aira01d.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.time.Duration.Companion.milliseconds

class ContentListViewModel(val application: Application) : ViewModel()
{
    private val _runMode = MutableLiveData<String>()
    val runMode: LiveData<String> = _runMode

    private val _cameraProtocol = MutableLiveData<ICameraConnectionStatus.CameraProtocol>()
    val cameraProtocol: LiveData<ICameraConnectionStatus.CameraProtocol> = _cameraProtocol

    var fileList by mutableStateOf<List<ICameraFileInfo.ImageFileInfo>>(emptyList())
        private set

    private val _contentStatus = MutableLiveData<ContentLoadingStatus>()
    val contentStatus: LiveData<ContentLoadingStatus> = _contentStatus

    private val _currentExif = MutableStateFlow<ExifDataToDisplay?>(null)
    val currentExif: StateFlow<ExifDataToDisplay?> = _currentExif.asStateFlow()

    // --- ダウンロードの状態管理用 State (Compose の mutableStateOf を使用)
    var isDownloading by mutableStateOf(false)
        private set
    var downloadProgress by mutableFloatStateOf(0.0f)
        private set
    var downloadStatusText by mutableStateOf("")
        private set
    var downloadFileName by mutableStateOf("")
        private set

    init
    {
        try {
            _runMode.value = "unknown"
            _contentStatus.value = ContentLoadingStatus.Uninitialized
        } catch (e: Exception) {
            Log.v(TAG, "initialize Exception: ${e.localizedMessage}")
        }
    }

    private fun Context.findActivity(): Activity? {
        var context = this
        while (context is ContextWrapper) {
            if (context is Activity) return context
            context.baseContext.also { context = it }
        }
        return null
    }

    fun changeRunModeToPlayback()
    {
        Log.v(TAG, "called changeRunModeToPlayback()")
        val currentRunMode = AppSingleton.cameraControl.getCurrentRunMode()
        if(currentRunMode == "play")
        {
            // ----- 既に再生モードだと判断し、何もせずに終了する
            Log.v(TAG, "already PLAY mode")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try
            {
                var retryCount = 10
                AppSingleton.cameraControl.stopLiveview()
                Thread.sleep(150L)
                val cameraProtocol = AppSingleton.cameraControl.getCameraProtocol()
                if (cameraProtocol == ICameraConnectionStatus.CameraProtocol.OPC)
                {
                    // ----- OPCの場合、動作モードが切り替えられるまで実行する
                    while (!AppSingleton.cameraControl.changeRunMode("standalone")) {
                        Log.v(TAG, "CHANGE RUN MODE(play -> standalone) : NG")
                        Thread.sleep(500L)
                        retryCount--
                        if (retryCount < 0) { break }
                    }
                }
                _cameraProtocol.postValue(cameraProtocol)
                _runMode.postValue(AppSingleton.cameraControl.getCurrentRunMode())

                retryCount = 10
                while (!AppSingleton.cameraControl.changeRunMode("play"))
                {
                    Log.v(TAG, "CHANGE RUN MODE(standalone -> play) : NG")
                    Thread.sleep(500L)
                    retryCount--
                    if (retryCount < 0) { break }
                }
                _runMode.postValue(AppSingleton.cameraControl.getCurrentRunMode())

                // ----- 画像の全一覧を取得する
                getAllContentList()
            }
            catch (e: Exception)
            {
                Log.e(TAG, "ERR>Change RunMode to playback ${e.message}")
            }
        }
    }

    fun changeRunModeToRecord(context: Context)
    {
        // ----- 撮影モードに切り替える
        Log.v(TAG, "called changeRunModeToRecord()")

        val activity = context.findActivity()
        if (activity != null && activity.isChangingConfigurations) {
            // --- 画面の回転処理中に、ここが呼び出されたので、通信は行わない
            Log.d(TAG, "INFO> detect a screen rotation, ignored.")
            return
        }

        // ----- 動作モードが切り替えられるまで実行する
        viewModelScope.launch(Dispatchers.IO) {
            try
            {
                var retryCount = 10
                val cameraProtocol = AppSingleton.cameraControl.getCameraProtocol()
                if (cameraProtocol == ICameraConnectionStatus.CameraProtocol.OPC)
                {
                    // --- OPCの場合、いったん standaloneモードに切り替える
                    while (!AppSingleton.cameraControl.changeRunMode("standalone")) {
                        Log.v(TAG, "CHANGE RUN MODE(play -> standalone) : NG")
                        Thread.sleep(500L)
                        retryCount--
                        if (retryCount < 0) { break }
                    }
                }
                _cameraProtocol.postValue(cameraProtocol)
                _runMode.postValue(AppSingleton.cameraControl.getCurrentRunMode())
                retryCount = 10
                while (!AppSingleton.cameraControl.changeRunMode("rec"))
                {
                    Log.v(TAG, "CHANGE RUN MODE(standalone -> rec) : NG")
                    Thread.sleep(500L)
                    retryCount--
                    if (retryCount < 0) { break }
                }
                Thread.sleep(150L)
                if (cameraProtocol == ICameraConnectionStatus.CameraProtocol.OPC)
                {
                    // ----- 受信イベントのウォッチを行う
                    AppSingleton.cameraControl.startEventReceive()
                }
                Thread.sleep(150L)
                AppSingleton.cameraControl.startLiveview()
                _runMode.postValue(AppSingleton.cameraControl.getCurrentRunMode())
            }
            catch (e: Exception)
            {
                Log.e(TAG, "ERR>Change RunMode to rec ${e.message}")
            }
        }
    }

    fun getAllContentList()
    {
        _contentStatus.postValue(ContentLoadingStatus.Fetching)
        viewModelScope.launch {
            try
            {
                Log.v(TAG, " - - - - - - getAllContentList() called")

                // --- ファイル名の一覧
                val result = withContext(Dispatchers.IO) { getContentList() }

                // 結果の反映はMainスレッドで
                fileList = result
                _contentStatus.postValue(ContentLoadingStatus.Ready)
                Log.v(TAG, "number of contents : ${fileList.size}")
            }
            catch (e: Exception)
            {
                Log.e(TAG, "ERR>Change RunMode to playback ${e.message}")
            }
        }
    }

    private fun getContentList(directory: String = ROOT_DIRECTORY) : List<ICameraFileInfo.ImageFileInfo>
    {
        val allItems = mutableListOf<ICameraFileInfo.ImageFileInfo>()
        fun walk(currentPath: String) {
            try {
                val remoteFiles = AppSingleton.cameraControl
                    .getCameraPlaybackControl()
                    .getImageFileList(currentPath)

                remoteFiles.forEach { file ->
                    when {
                        file.isDirectory -> {
                            // ディレクトリなら再帰
                            walk("$currentPath/${file.fileName}")
                        }
                        !file.isSystem && !file.isHidden -> {
                            // システムまたは隠しファイル以外は登録
                            allItems.add(file)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "ERR>walk($currentPath): ${e.message}")
            }
        }
        walk(directory)
        return allItems
    }

    fun executeDownload(
        context: Context,
        file: ICameraFileInfo.ImageFileInfo,
        selectedSize: GetImageSize,
        downloadMessage: String,
        storeOKMessage: String,
        storeNGMessage: String,
        storeErrorMessage: String
    ) {
        val baseUrl = AppSingleton.CAMERA_BASE_URL
        val fileTransfer = OmdsFileTransfer(executeUrl = baseUrl)
        val storeFileName = createTimestampedFileName(file.fileName)

        // 状態の初期化
        isDownloading = true
        downloadProgress = 0.0f
        downloadStatusText = downloadMessage
        downloadFileName = file.fileName

        // --- 選択された画像サイズに応じてリクエストパスを調整
        val downloadPath = when (selectedSize) {
            GetImageSize.WIDTH_640_PX -> "/get_resizeimg.cgi?DIR=${file.directory}/${file.fileName}&size=0640"
            GetImageSize.WIDTH_1024_PX -> "/get_resizeimg.cgi?DIR=${file.directory}/${file.fileName}&size=1024"
            GetImageSize.WIDTH_1280_PX -> "/get_resizeimg.cgi?DIR=${file.directory}/${file.fileName}&size=1280"
            GetImageSize.WIDTH_1600_PX -> "/get_resizeimg.cgi?DIR=${file.directory}/${file.fileName}&size=1600"
            GetImageSize.WIDTH_1920_PX -> "/get_resizeimg.cgi?DIR=${file.directory}/${file.fileName}&size=1920"
            GetImageSize.WIDTH_2048_PX -> "/get_resizeimg.cgi?DIR=${file.directory}/${file.fileName}&size=2048"
            GetImageSize.WIDTH_2560_PX -> "/get_resizeimg.cgi?DIR=${file.directory}/${file.fileName}&size=2560"
            GetImageSize.ORIGINAL -> "${file.directory}/${file.fileName}"
        }

        val streamSaver = MediaStoreStreamSaveHelper(context, storeFileName)

        // viewModelScope で実行することで画面回転に耐える
        viewModelScope.launch(Dispatchers.IO) {
            val isReady = streamSaver.open()
            if (!isReady) {
                withContext(Dispatchers.Main) {
                    isDownloading = false
                    downloadFileName = ""
                    Toast.makeText(context, storeNGMessage, Toast.LENGTH_SHORT).show()
                }
                return@launch
            }

            fileTransfer.downloadContent(
                directory = downloadPath,
                callback = object : IPlaybackControl.IContentTransferCallback {
                    override fun onReceive(readBytes: Int, length: Int, size: Int, data: ByteArray?) {
                        if (data != null && data.isNotEmpty()) {
                            streamSaver.write(data)
                        }
                        if (length > 0) {
                            val pct = readBytes.toFloat() / length.toFloat()
                            // Mainスレッド（または Compose Stateの変更が安全な場所）で更新
                            downloadProgress = pct
                        }
                    }

                    override fun onCompleted() {
                        streamSaver.close(success = true)
                        viewModelScope.launch(Dispatchers.Main) {
                            isDownloading = false
                            downloadFileName = ""
                            Toast.makeText(
                                context,
                                "$storeOKMessage:${file.fileName}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onErrorOccurred(e: Exception?) {
                        streamSaver.close(success = false)
                        viewModelScope.launch(Dispatchers.Main) {
                            isDownloading = false
                            downloadFileName = ""
                            Toast.makeText(
                                context,
                                "$storeErrorMessage: ${e?.localizedMessage}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            )
        }
    }

    fun updateExifInfo(path: String, fileName: String, cacheFilePath: String?)
    {
        // --- 画像のExif情報を取得する
        // Log.v(TAG, "updateExifInfo: $fileName")
        _currentExif.value = null  // 新しい画像の読み込みが始まったら、一旦古いEXIF情報をクリア
        viewModelScope.launch {
            try {
                val exifDataToDisplay = withContext(Dispatchers.IO) {

                    val exif = if (_cameraProtocol.value == ICameraConnectionStatus.CameraProtocol.OPC)
                    {
                        // ----- OPC機の場合は、Exifをカメラから転送して取得
                        AppSingleton.cameraControl.getCameraPlaybackControl().getExif("$path/$fileName")
                    }
                    else
                    {
                        // ----- OMDS機の場合は、キャッシュファイルから取得
                        if (cacheFilePath != null)
                        {
                            ExifInterface(cacheFilePath)
                        }
                        else
                        {
                            // ----- キャッシュファイルがない(特定できない)場合は、Exifをカメラから転送して取得する
                            AppSingleton.cameraControl.getCameraPlaybackControl().getExif("$path/$fileName")
                        }
                    }

                    // 絞り値
                    val fNumber = exif?.getAttribute(ExifInterface.TAG_F_NUMBER)

                    // ISO感度
                    val iso = exif?.getAttribute(ExifInterface.TAG_PHOTOGRAPHIC_SENSITIVITY)

                    // 焦点距離
                    val focalLengthDouble = exif?.getAttributeDouble(ExifInterface.TAG_FOCAL_LENGTH, 0.0) ?: 0.0

                    // モデル
                    val model = exif?.getAttribute(ExifInterface.TAG_MODEL)

                    // GPS情報があるかどうか
                    val latitude = exif?.getAttribute(ExifInterface.TAG_GPS_LATITUDE)
                    val haGpsInfo = !latitude.isNullOrEmpty()

                    // プログラムモード
                    val programModeIndex = exif?.getAttributeInt(ExifInterface.TAG_EXPOSURE_PROGRAM, 0) ?: 0
                    val exposurePrograms = application.applicationContext.resources.getStringArray(R.array.exif_exposure_program_value)
                    val programModeStr = exposurePrograms.getOrNull(programModeIndex) ?: exposurePrograms[0]

                    // 測光モード
                    val meteringModeRaw = exif?.getAttributeInt(ExifInterface.TAG_METERING_MODE, 0) ?: 0
                    val meteringModeIndex = when (meteringModeRaw) {
                        in 0..6 -> meteringModeRaw // 0〜6（Unknown〜Partial）はそのまま
                        255 -> 7                   // 255（Other）なら、配列の7番目を指定
                        else -> 0                  // 規格外の値が来たら 0（Unknown）にする
                    }
                    val meteringModes = application.applicationContext.resources.getStringArray(R.array.exif_metering_mode_value)
                    val meteringModeStr = meteringModes.getOrNull(meteringModeIndex) ?: meteringModes[0]

                    // シャッタースピード
                    val exposureTimeStr = exif?.getAttribute(ExifInterface.TAG_EXPOSURE_TIME)
                    val value = exposureTimeStr?.toFloatOrNull() ?: 0.0f
                    val exposureTime = if (value in 0.0f..0.5f) { // 0.0より大きく0.5未満 (1/value が 2.0 以上になる条件)
                        // シャッター速度を分数で表示する (例: 1/250 s)
                        val inv = 1.0f / value
                        var intValue = inv.toInt()

                        // 割り切れない数値の丸め処理 (4や9で終わる場合の補正)
                        if (intValue % 10 in listOf(4, 9)) {
                            intValue++
                        }
                        " 1/$intValue"
                    } else {
                        // シャッター速度を数値（秒数）で表示する (例: 1.5s / 0s)
                        " ${exposureTimeStr ?: "0"} s"
                    }

                    // --- 取得した値を表示(仮)
                    Log.v(TAG, "Read EXIF: $path/$fileName $cacheFilePath (SS:$exposureTime, F$fNumber, ISO$iso) $focalLengthDouble mm")

                    // データクラスにして返す
                    ExifDataToDisplay(
                        fileName = fileName,
                        aperture = fNumber,
                        exposureTime = exposureTime,
                        focalLength = focalLengthDouble,
                        programMode = programModeStr,
                        meteringMode = meteringModeStr,
                        iso = iso,
                        model = model,
                        hasGpsInfo = haGpsInfo
                    )
                }
                _currentExif.value = exifDataToDisplay
            }
            catch (e: Exception)
            {
                Log.e(TAG, "updateExifInfo : $fileName (${e.localizedMessage})")
                //e.printStackTrace()
                _currentExif.value = null
            }
        }
    }

    fun downloadMultipleFiles(files: List<ICameraFileInfo.ImageFileInfo>, imageSize: GetImageSize, context: Context)
    {
        isDownloading = true

        viewModelScope.launch(Dispatchers.IO)
        {
            val baseUrl = AppSingleton.CAMERA_BASE_URL
            val fileTransfer = OmdsFileTransfer(executeUrl = baseUrl)

            var downloadCount = 0
            var successCount = 0

            for (file in files)
            {
                // ----- JPEGファイルの時には、指定された画像サイズでダウンロードする
                val selectedSize = if (file.fileName.endsWith(suffix = "JPG", ignoreCase = true)) {
                    imageSize
                } else {
                    GetImageSize.ORIGINAL
                }

                // ----- 保存するファイル名
                val storeFileName = createTimestampedFileName(file.fileName)

                // 状態の初期化
                downloadCount++ // 画像取得数
                isDownloading = true
                downloadProgress = 0.0f
                downloadStatusText = "${context.getString(R.string.now_downloading)} : $downloadCount/${files.size}"
                downloadFileName = file.fileName

                // --- 選択された画像サイズに応じてリクエストパスを調整
                val downloadPath = when (selectedSize) {
                    GetImageSize.WIDTH_640_PX -> "/get_resizeimg.cgi?DIR=${file.directory}/${file.fileName}&size=0640"
                    GetImageSize.WIDTH_1024_PX -> "/get_resizeimg.cgi?DIR=${file.directory}/${file.fileName}&size=1024"
                    GetImageSize.WIDTH_1280_PX -> "/get_resizeimg.cgi?DIR=${file.directory}/${file.fileName}&size=1280"
                    GetImageSize.WIDTH_1600_PX -> "/get_resizeimg.cgi?DIR=${file.directory}/${file.fileName}&size=1600"
                    GetImageSize.WIDTH_1920_PX -> "/get_resizeimg.cgi?DIR=${file.directory}/${file.fileName}&size=1920"
                    GetImageSize.WIDTH_2048_PX -> "/get_resizeimg.cgi?DIR=${file.directory}/${file.fileName}&size=2048"
                    GetImageSize.WIDTH_2560_PX -> "/get_resizeimg.cgi?DIR=${file.directory}/${file.fileName}&size=2560"
                    GetImageSize.ORIGINAL -> "${file.directory}/${file.fileName}"
                }

                val streamSaver = MediaStoreStreamSaveHelper(context, storeFileName)
                val isReady = streamSaver.open()
                if (!isReady)
                {
                    withContext(Dispatchers.Main) {
                        isDownloading = false
                        downloadFileName = ""
                        Toast.makeText(context, context.getString(R.string.stored_image_ng), Toast.LENGTH_SHORT).show()
                    }
                    continue // 次のファイルのダウンロードへ
                }

                // --- ファイル取得実処理 (コールバックが完了するまで処理を一時停止する)
                val isSuccess = suspendCancellableCoroutine { continuation ->
                    fileTransfer.downloadContent(
                        directory = downloadPath,
                        callback = object : IPlaybackControl.IContentTransferCallback {
                            override fun onReceive(readBytes: Int, length: Int, size: Int, data: ByteArray?) {
                                if (data != null && data.isNotEmpty()) {
                                    streamSaver.write(data)
                                }
                                if (length > 0) {
                                    val percent = readBytes.toFloat() / length.toFloat()
                                    downloadProgress = percent
                                }
                            }

                            override fun onCompleted() {
                                streamSaver.close(success = true)
                                // コルーチン再開: 戻り値として true を返す
                                if (continuation.isActive) continuation.resume(true)
                            }

                            override fun onErrorOccurred(e: Exception?) {
                                streamSaver.close(success = false)
                                // コルーチン再開: 戻り値として false を返す
                                if (continuation.isActive) continuation.resume(false)
                            }
                        }
                    )
                    // コルーチンがキャンセルされた場合の処理
                    continuation.invokeOnCancellation {
                        streamSaver.close(success = false)
                    }
                }

                // 一時停止が解除され、ここに流れてくる（UI更新と次のアイテムへの移行）
                if (isSuccess)
                {
                    // --- ダウンロード成功
                    successCount++
                }
                withContext(Dispatchers.Main) {
                    downloadFileName = ""
                }

                // 1つのファイル処理が終わったら少し間隔をあける
                delay(150.milliseconds)
            }

            // 一括ダウンロードの完了表示
            val finishString = "${context.getString(R.string.finish_bulk_downloading_head)} $successCount/$downloadCount ${context.getString(R.string.finish_bulk_downloading_foot)}"
            withContext(Dispatchers.Main) {
                isDownloading = false
                downloadFileName = ""
                downloadProgress = 0.0f
                Toast.makeText(context, finishString, Toast.LENGTH_LONG).show()
            }
        }
    }

    fun clearExifInfo()
    {
        _currentExif.value = null  // 新しい画像の読み込みが始まったら、一旦古いEXIF情報をクリア
    }

    // --- ファイル名に現在のタイムスタンプを付与する関数  例: "R101010.JPG" -> "R101010_20261213123400.JPG"
    private fun createTimestampedFileName(originalFileName: String): String {
        val dotIndex = originalFileName.lastIndexOf('.')
        val timestamp = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(Date())

        return if (dotIndex != -1) {
            // ---拡張子がある場合 (base = R101010, ext = .JPG)
            val baseName = originalFileName.substring(0, dotIndex)
            val extension = originalFileName.substring(dotIndex)
            "${baseName}_$timestamp$extension"
        } else {
            // --- 拡張子がない場合
            "${originalFileName}_$timestamp"
        }
    }

    enum class ContentLoadingStatus {
        Uninitialized, ChangingMode, Fetching, Ready
    }
    enum class DisplayMode {
        Grid, List
    }

    // ソート順の定義
    enum class SortOrder {
        NEWEST, // 最新から
        OLDEST  // 最古から
    }

    // 拡張子の定義
    enum class ExtensionFilter {
        ALL,
        JPEG,
        RAW,
        MOV,
        OTHER;

        // ファイル名から該当するか判定するヘルパー
        fun matches(fileName: String): Boolean {
            val ext = fileName.substringAfterLast('.', "").uppercase()
            return when (this) {
                ALL -> true
                JPEG -> ext == "JPG" || ext == "JPEG"
                RAW -> ext == "ORF"
                MOV -> ext == "MOV"
                OTHER -> ext != "JPG" && ext != "JPEG" && ext != "ORF" && ext != "MOV"
            }
        }
    }

    enum class GetImageSize {
        ORIGINAL,
        WIDTH_640_PX,
        WIDTH_1024_PX,
        WIDTH_1280_PX,
        WIDTH_1600_PX,
        WIDTH_1920_PX,
        WIDTH_2048_PX,
        WIDTH_2560_PX,
    }

    companion object {
        private val TAG = ContentListViewModel::class.java.simpleName
        private const val ROOT_DIRECTORY = "/DCIM"

        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                // アプリケーションのContextを取得
                val application = extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]!!

                return ContentListViewModel(application = application) as T
            }
        }
    }
}
