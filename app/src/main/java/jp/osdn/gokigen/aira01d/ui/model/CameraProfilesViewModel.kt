package jp.osdn.gokigen.aira01d.ui.model

import android.net.Uri
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
import jp.osdn.gokigen.aira01d.cameraprofile.CameraProfileRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CameraProfilesViewModel(private val repository: CameraProfileRepository) : ViewModel()
{
    private val _cameraProfileOperationStatus = MutableLiveData<CameraProfileOperationStatus>()
    val cameraProfileOperationStatus: LiveData<CameraProfileOperationStatus> = _cameraProfileOperationStatus

    private val _fileNameToPull = MutableLiveData<String>()
    val fileNameToPull: LiveData<String> = _fileNameToPull

    private val _fileNameToApply = MutableLiveData<String>()
    val fileNameToApply: LiveData<String> = _fileNameToApply

    private val _fileNameToDelete = MutableLiveData<String>()
    val fileNameToDelete: LiveData<String> = _fileNameToDelete

    private val _fileNameToRename = MutableLiveData<String>()
    val fileNameToRename: LiveData<String> = _fileNameToRename

    private val _fileNameToExport = MutableLiveData<String>()
    val fileNameToExport: LiveData<String> = _fileNameToExport

    private val _executionModeIndex = MutableLiveData<Int>()
    val executionModeIndex: LiveData<Int> = _executionModeIndex

    var fileList by mutableStateOf<List<String>>(emptyList())
        private set

    init {
        _cameraProfileOperationStatus.value = CameraProfileOperationStatus.NoDialog
        _executionModeIndex.value = 0
        try
        {
            getAllFileList(CameraProfileOperationStatus.NoDialog)
        }
        catch (e: Exception)
        {
            Log.v(TAG, "ERR>init() ${e.localizedMessage}")
        }
    }

    fun setExecutionMode(index: Int)
    {
        _executionModeIndex.value = index
    }

    fun proceedCameraProfileOperation() {
        when (_cameraProfileOperationStatus.value) {
            CameraProfileOperationStatus.NoDialog -> { _cameraProfileOperationStatus.value = CameraProfileOperationStatus.OpenedDialog }
            CameraProfileOperationStatus.OpenedDialog -> { _cameraProfileOperationStatus.value = CameraProfileOperationStatus.NoDialog }
            CameraProfileOperationStatus.FinishedPullAction -> { getAllFileList() }
            CameraProfileOperationStatus.FailedPullAction -> { getAllFileList() }
            CameraProfileOperationStatus.FinishedDeleteAction -> { getAllFileList() }
            CameraProfileOperationStatus.FailedDeleteAction -> { getAllFileList() }
            CameraProfileOperationStatus.FinishedRenameFile -> { getAllFileList() }
            CameraProfileOperationStatus.FailedRenameFile -> { getAllFileList() }
            CameraProfileOperationStatus.FinishedExportToFile -> { _cameraProfileOperationStatus.value = CameraProfileOperationStatus.OpenedDialog }
            CameraProfileOperationStatus.FailedExportToFile -> { _cameraProfileOperationStatus.value = CameraProfileOperationStatus.OpenedDialog }
            CameraProfileOperationStatus.FinishedApplyAction -> { _cameraProfileOperationStatus.value = CameraProfileOperationStatus.OpenedDialog }
            CameraProfileOperationStatus.FailedApplyAction -> { _cameraProfileOperationStatus.value = CameraProfileOperationStatus.OpenedDialog }
            CameraProfileOperationStatus.AbortExportAction -> { _cameraProfileOperationStatus.value = CameraProfileOperationStatus.OpenedDialog }
            CameraProfileOperationStatus.SetRenameFileName -> { _cameraProfileOperationStatus.value = CameraProfileOperationStatus.OpenedDialog }
            CameraProfileOperationStatus.FinishedImportAction -> { getAllFileList() }
            CameraProfileOperationStatus.FailedImportAction -> { getAllFileList() }
            else -> {}
        }
        Log.v(TAG, "Camera Profile Status: ${_cameraProfileOperationStatus.value}")
    }

    fun cancelAction()
    {
        _cameraProfileOperationStatus.value = CameraProfileOperationStatus.OpenedDialog
    }

    fun abortExport()
    {
        _cameraProfileOperationStatus.value = CameraProfileOperationStatus.AbortExportAction
    }

    fun deleteProfile(fileName: String)
    {
        //---- ファイルを削除する指示
        Log.v(TAG, "Delete profile: $fileName")
        if (fileName.isEmpty())
        {
            // ----- ファイル名が指定されていない場合は、何もしない
            return
        }
        _fileNameToDelete.value = fileName
        _cameraProfileOperationStatus.value = CameraProfileOperationStatus.DeleteActionConfirmation
    }

    fun confirmDeleteProfile()
    {
        val fileNameToDelete = _fileNameToDelete.value?: ""
        viewModelScope.launch {
            try
            {
                // カメラからの設定読み出しを開始する
                _cameraProfileOperationStatus.postValue(CameraProfileOperationStatus.ReadingProfileFromCamera)

                // Dispatchers.IO で非同期取得
                val result = withContext(Dispatchers.IO) {
                    repository.deleteCameraProfile(fileNameToDelete)
                }
                // メインスレッドで安全にComposeのStateへ反映...読み出し状況に合わせて応答を反映
                if (result) {
                    _cameraProfileOperationStatus.postValue(CameraProfileOperationStatus.FinishedDeleteAction)
                }
                else
                {
                    _cameraProfileOperationStatus.postValue(CameraProfileOperationStatus.FailedDeleteAction)
                }
            }
            catch (e: Exception)
            {
                e.printStackTrace()
                _cameraProfileOperationStatus.postValue(CameraProfileOperationStatus.FailedDeleteAction)
            }
        }
    }

    // ----- ファイル名のリネーム処理
    fun renameProfile(fileName : String)
    {
        Log.v(TAG, "Rename profile: $fileName")
        if (fileName.isEmpty())
        {
            // ----- ファイル名が指定されていない場合は、何もしない
            return
        }
        _fileNameToRename.value = fileName
        _cameraProfileOperationStatus.value = CameraProfileOperationStatus.SetRenameFileName
    }

    fun confirmRenameProfile(newFileName: String)
    {
        if (newFileName.isEmpty())
        {
            // ----- ファイル名が指定されていない場合は、何もしない
            _cameraProfileOperationStatus.value = CameraProfileOperationStatus.SetRenameFileName
            return
        }
        _cameraProfileOperationStatus.value = CameraProfileOperationStatus.FileRenaming
        val fileNameToRename = _fileNameToRename.value?: ""
        viewModelScope.launch {
            try
            {
                // ファイル名の変更処理
                _cameraProfileOperationStatus.postValue(CameraProfileOperationStatus.FileRenaming)

                // Dispatchers.IO で非同期取得
                val result = withContext(Dispatchers.IO) {
                    repository.renameProfileFile(fileNameToRename, newFileName)
                }
                // メインスレッドで安全にComposeのStateへ反映...読み出し状況に合わせて応答を反映
                if (result) {
                    _cameraProfileOperationStatus.postValue(CameraProfileOperationStatus.FinishedRenameFile)
                }
                else
                {
                    _cameraProfileOperationStatus.postValue(CameraProfileOperationStatus.FailedRenameFile)
                }
            }
            catch (e: Exception)
            {
                e.printStackTrace()
                _cameraProfileOperationStatus.postValue(CameraProfileOperationStatus.FailedRenameFile)
            }
        }
    }

    // ----- カメラへプロファイルを設定する指示
    fun applyProfile(fileName : String)
    {
        Log.v(TAG, "Apply profile: $fileName")
        if (fileName.isEmpty())
        {
            // ----- ファイル名が指定されていない場合は、何もしない
            return
        }
        _fileNameToApply.value = fileName
        _cameraProfileOperationStatus.value = CameraProfileOperationStatus.ApplyActionConfirmation
    }

    fun confirmApplyProfile()
    {
        val fileNameToApply = _fileNameToApply.value?: ""
        viewModelScope.launch {
            try
            {
                // カメラからの設定読み出しを開始する
                _cameraProfileOperationStatus.postValue(CameraProfileOperationStatus.ReadingProfileFromFile)

                // Dispatchers.IO で非同期取得
                val result = withContext(Dispatchers.IO) {
                    val cameraProperties = repository.readCameraPropertyFile(fileNameToApply)
                    if (cameraProperties.isEmpty())
                    {
                        // ----- ファイルからカメラプロパティが読み込めなかった...
                        false
                    }
                    else {
                        _cameraProfileOperationStatus.postValue(CameraProfileOperationStatus.ApplyingProfileToCamera)
                        // カメラプロパティを設定する
                        repository.setCameraPropertiesToCamera(cameraProperties)
                    }
                }
                // メインスレッドで安全にComposeのStateへ反映...読み出し状況に合わせて応答を反映
                if (result) {
                    _cameraProfileOperationStatus.postValue(CameraProfileOperationStatus.FinishedApplyAction)
                }
                else
                {
                    _cameraProfileOperationStatus.postValue(CameraProfileOperationStatus.FailedApplyAction)
                }
            }
            catch (e: Exception)
            {
                e.printStackTrace()
                _cameraProfileOperationStatus.postValue(CameraProfileOperationStatus.FailedApplyAction)
            }
        }
    }

    // ----- カメラから設定を取得するアクション(指示)
    fun pullProfile(fileName : String)
    {
        Log.v(TAG, "Pull profile: $fileName")
        if (fileName.isEmpty())
        {
            // ----- ファイル名が指定されていない場合は、何もしない
            return
        }
        _fileNameToPull.value = fileName
        _cameraProfileOperationStatus.value = CameraProfileOperationStatus.PullActionConfirmation
    }

    // ----- カメラから設定を取得するアクション(実行確認)
    fun confirmPullProfile()
    {
        val fileNameToPull = _fileNameToPull.value?: ""
        viewModelScope.launch {
            try
            {
                // カメラからの設定読み出しを開始する
                _cameraProfileOperationStatus.postValue(CameraProfileOperationStatus.ReadingProfileFromCamera)

                // Dispatchers.IO で非同期取得
                val result = withContext(Dispatchers.IO) {
                    repository.storeAllCameraProfiles(fileNameToPull)
                }
                // メインスレッドで安全にComposeのStateへ反映...読み出し状況に合わせて応答を反映
                if (result) {
                    _cameraProfileOperationStatus.postValue(CameraProfileOperationStatus.FinishedPullAction)
                }
                else
                {
                    _cameraProfileOperationStatus.postValue(CameraProfileOperationStatus.FailedPullAction)
                }
            }
            catch (e: Exception)
            {
                e.printStackTrace()
                _cameraProfileOperationStatus.postValue(CameraProfileOperationStatus.FailedPullAction)
            }
        }
    }

    fun getAllFileList(finishedStatus: CameraProfileOperationStatus = CameraProfileOperationStatus.OpenedDialog)
    {
        // ----- 格納しているファイルの一覧を取得する処理
        if (finishedStatus != CameraProfileOperationStatus.NoDialog)
        {
            // ----- 処理終了時にダイアログを表示させない場合は、ステータスは変えない
            _cameraProfileOperationStatus.postValue(CameraProfileOperationStatus.ReadingFileList)
        }
        viewModelScope.launch {
            try
            {
                Log.v(TAG, " - - - - - - getAllContentList() called")

                // --- ファイル名の一覧
                val result = withContext(Dispatchers.IO) { repository.getSavedFileList() }

                // 結果の反映はMainスレッドで
                fileList = result
                _cameraProfileOperationStatus.postValue(finishedStatus)
                Log.v(TAG, "number of contents : ${fileList.size}")
            }
            catch (e: Exception)
            {
                Log.e(TAG, "ERR>get file list ${e.message}")
            }
        }
    }

    fun exportProfileFile(fileName: String)
    {
        //---- ファイルをエクスポートする指示
        Log.v(TAG, "Export profile: $fileName")
        if (fileName.isEmpty())
        {
            // ----- ファイル名が指定されていない場合は、何もしない
            return
        }
        _fileNameToExport.value = fileName
        _cameraProfileOperationStatus.value = CameraProfileOperationStatus.SelectExportDirectory
    }

    fun exportCameraPropertyFile(destinationUri: Uri)
    {
        val fileNameToExport =_fileNameToExport.value ?:""
        if (fileNameToExport.isEmpty())
        {
            // ----- ファイル名が指定されていない場合は、何もしない
            _cameraProfileOperationStatus.value = CameraProfileOperationStatus.OpenedDialog
            return
        }
        viewModelScope.launch {
            try
            {
                _cameraProfileOperationStatus.postValue(CameraProfileOperationStatus.ExportingProfileToFile)

                // --- ファイル名の一覧
                val result = withContext(Dispatchers.IO) { repository.exportCameraPropertyFile(fileNameToExport, destinationUri) }
                // メインスレッドで安全にComposeのStateへ反映...読み出し状況に合わせて応答を反映
                if (result) {
                    _cameraProfileOperationStatus.postValue(CameraProfileOperationStatus.FinishedExportToFile)
                }
                else
                {
                    _cameraProfileOperationStatus.postValue(CameraProfileOperationStatus.FailedExportToFile)
                }
            }
            catch (e: Exception)
            {
                e.printStackTrace()
                _cameraProfileOperationStatus.postValue(CameraProfileOperationStatus.FailedExportToFile)
            }
        }
    }

    fun importCameraPropertyFile(sourceUri: Uri)
    {
        viewModelScope.launch {
            try
            {
                _cameraProfileOperationStatus.postValue(CameraProfileOperationStatus.ImportingProfileFromStorage)

                // --- ファイル名の一覧
                val result = withContext(Dispatchers.IO) { repository.importCameraPropertyFile(sourceUri) }
                // メインスレッドで安全にComposeのStateへ反映...読み出し状況に合わせて応答を反映
                if (result) {
                    _cameraProfileOperationStatus.postValue(CameraProfileOperationStatus.FinishedImportAction)
                }
                else
                {
                    _cameraProfileOperationStatus.postValue(CameraProfileOperationStatus.FailedImportAction)
                }
            }
            catch (e: Exception)
            {
                e.printStackTrace()
                _cameraProfileOperationStatus.postValue(CameraProfileOperationStatus.FailedImportAction)
            }
        }
    }

    enum class CameraProfileOperationStatus {
        NoDialog,
        OpenedDialog,
        PullActionConfirmation,
        ReadingProfileFromCamera,
        FinishedPullAction,
        FailedPullAction,
        ReadingFileList,
        DeleteActionConfirmation,
        FinishedDeleteAction,
        FailedDeleteAction,
        SelectExportDirectory,
        AbortExportAction,
        ExportingProfileToFile,
        FinishedExportToFile,
        FailedExportToFile,
        ApplyActionConfirmation,
        ReadingProfileFromFile,
        ApplyingProfileToCamera,
        FinishedApplyAction,
        FailedApplyAction,
        SetRenameFileName,
        FileRenaming,
        FinishedRenameFile,
        FailedRenameFile,
        ImportingProfileFromStorage,
        FinishedImportAction,
        FailedImportAction,
    }

    companion object {
        private val TAG = CameraProfilesViewModel::class.java.simpleName

        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                // アプリケーションのContextを取得
                val application = extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]!!

                // Repository のインスタンスを作成
                val repository = CameraProfileRepository(application.applicationContext)

                return CameraProfilesViewModel(repository) as T
            }
        }
    }
}
