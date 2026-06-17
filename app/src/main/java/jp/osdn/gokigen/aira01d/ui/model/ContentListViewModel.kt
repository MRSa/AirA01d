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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ContentListViewModel(application: Application) : ViewModel()
{
    private val _runMode = MutableLiveData<String>()
    val runMode: LiveData<String> = _runMode

    private val _cameraProtocol = MutableLiveData<ICameraConnectionStatus.CameraProtocol>()
    val cameraProtocol: LiveData<ICameraConnectionStatus.CameraProtocol> = _cameraProtocol

    var fileList by mutableStateOf<List<ICameraFileInfo.ImageFileInfo>>(emptyList())
        private set

    private val _contentStatus = MutableLiveData<ContentLoadingStatus>()
    val contentStatus: LiveData<ContentLoadingStatus> = _contentStatus

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

        // ★重要: viewModelScope で実行することで画面回転に耐える
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
