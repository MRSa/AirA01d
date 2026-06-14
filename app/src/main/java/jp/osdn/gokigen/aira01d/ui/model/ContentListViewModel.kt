package jp.osdn.gokigen.aira01d.ui.model

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import androidx.compose.runtime.getValue
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
import jp.osdn.gokigen.aira01d.AppSingleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
