package jp.osdn.gokigen.aira01d.ui.model

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import jp.osdn.gokigen.aira01d.preference.PreferenceRepository
import jp.osdn.gokigen.aira01d.preference.PreferenceSettings
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PreferenceViewModel(private val repository: PreferenceRepository) : ViewModel()
{
    fun initializeViewModel() { }

    // Flow を UI 用の StateFlow に変換 (stateIn を使うことで、常に最新の値を保持する Hot Flow になる)
    val connectCameraAutomatically: StateFlow<Boolean> = repository.connectCameraAutomaticallyFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PreferenceSettings.Camera.PREFERENCE_CAMERA_CONNECT_AUTOMATICALLY_DEFAULT_VALUE
        )

    fun setConnectCameraAutomatically(value: Boolean) {
        viewModelScope.launch {
            try {
                repository.updateConnectCameraAutomatically(value)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update preference", e)
            }
        }
    }

    suspend fun getConnectCameraAutomaticallySync(): Boolean {
        return repository.connectCameraAutomaticallyFlow.first()
    }
    fun isConnectCameraAutomatically(): Boolean {
        return connectCameraAutomatically.value
    }

    companion object {
        private val TAG = PreferenceViewModel::class.java.simpleName
    }
}
