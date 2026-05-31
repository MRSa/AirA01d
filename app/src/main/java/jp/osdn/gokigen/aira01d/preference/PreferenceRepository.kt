package jp.osdn.gokigen.aira01d.preference

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class PreferenceRepository(private val context: Context) {

    // ----- キーの定義
    private object PreferencesKeys {
        val CAMERA_CONNECT_AUTOMATICALLY = booleanPreferencesKey(
            PreferenceSettings.Camera.PREFERENCE_CAMERA_CONNECT_AUTOMATICALLY
        )
        val CAMERA_COMMAND_ISSUE_SINGLE = booleanPreferencesKey(
            PreferenceSettings.Camera.PREFERENCE_CAMERA_COMMAND_SINGLE_ISSUE
        )
    }

    // 設定値のストリーム (Flow)
    val connectCameraAutomaticallyFlow: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            // 保存されていない場合はデフォルト値を返す
            preferences[PreferencesKeys.CAMERA_CONNECT_AUTOMATICALLY]
                ?: PreferenceSettings.Camera.PREFERENCE_CAMERA_CONNECT_AUTOMATICALLY_DEFAULT_VALUE
        }

    val issueCommandSingleFlow: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            // 保存されていない場合はデフォルト値を返す
            preferences[PreferencesKeys.CAMERA_COMMAND_ISSUE_SINGLE]
                ?: PreferenceSettings.Camera.PREFERENCE_CAMERA_COMMAND_SINGLE_ISSUE_DEFAULT_VALUE
        }

    // --- 書き込み処理 (suspend 関数) ---
    suspend fun updateConnectCameraAutomatically(value: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CAMERA_CONNECT_AUTOMATICALLY] = value
        }
    }

    suspend fun updateCommandIssueSingle(value: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CAMERA_COMMAND_ISSUE_SINGLE] = value
        }
    }
}
