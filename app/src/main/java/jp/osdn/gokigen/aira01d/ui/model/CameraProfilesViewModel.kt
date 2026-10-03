package jp.osdn.gokigen.aira01d.ui.model

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import jp.osdn.gokigen.aira01d.AppSingleton
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

    init {
        _cameraProfileOperationStatus.value = CameraProfileOperationStatus.NoDialog
    }

    fun proceedCameraProfileOperation() {
        when (_cameraProfileOperationStatus.value) {
            CameraProfileOperationStatus.NoDialog -> { _cameraProfileOperationStatus.value = CameraProfileOperationStatus.OpenedDialog }
            CameraProfileOperationStatus.OpenedDialog -> { _cameraProfileOperationStatus.value = CameraProfileOperationStatus.NoDialog }
            else -> {}
        }
        Log.v(TAG, "Camera Profile Status: ${_cameraProfileOperationStatus.value}")
    }

    fun cancelAction()
    {
        _cameraProfileOperationStatus.value = CameraProfileOperationStatus.OpenedDialog
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
                //
                _cameraProfileOperationStatus.value = CameraProfileOperationStatus.ReadingProfileFromCamera

                // Dispatchers.IO で非同期取得
                val result = withContext(Dispatchers.IO) {
                    repository.storeAllCameraProfiles(fileNameToPull)
                }
                // メインスレッドで安全にComposeのStateへ反映
                _cameraProfileOperationStatus.postValue(CameraProfileOperationStatus.FinishedPullAction)
            }
            catch (e: Exception)
            {
                e.printStackTrace()
            }
            finally {
                // ----- カメラからの読み出しが完了、データをファイルに書き出す
                _cameraProfileOperationStatus.postValue(CameraProfileOperationStatus.FinishedPullAction)
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
        SetRenameFileName,
        ConfirmationRenameFile,
        FileRenaming,
        FinishedRenameFile,
        ConfirmationDeleteFile,
        FileDeleting,
        FinishedDeleteFile,
        ExportActionConfirmation,
        ExportingProfileToFile,
        AbortExport,
        FinishedExport,
        ApplyActionConfirmation,
        ReadingProfileFromFile,
        ApplyingProfileToCamera,
        AbortApply,
        FinishedApply,
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
