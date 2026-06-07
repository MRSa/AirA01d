package jp.osdn.gokigen.aira01d.ui.model

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import jp.osdn.gokigen.aira01d.AppSingleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ContentListViewModel(application: Application) : ViewModel()
{
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
                AppSingleton.cameraControl.stopLiveview()
                Thread.sleep(150L)
                // ----- 動作モードが切り替えられるまで実行する
                while (!AppSingleton.cameraControl.changeRunMode("standalone"))
                {
                    Log.v(TAG, "CHANGE RUN MODE(play -> standalone) : NG")
                    Thread.sleep(500L)
                }
                while (!AppSingleton.cameraControl.changeRunMode("play"))
                {
                    Log.v(TAG, "CHANGE RUN MODE(standalone -> play) : NG")
                    Thread.sleep(500L)
                }
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
                while (!AppSingleton.cameraControl.changeRunMode("standalone"))
                {
                    Log.v(TAG, "CHANGE RUN MODE(play -> standalone) : NG")
                    Thread.sleep(500L)
                }
                while (!AppSingleton.cameraControl.changeRunMode("rec"))
                {
                    Log.v(TAG, "CHANGE RUN MODE(standalone -> rec) : NG")
                    Thread.sleep(500L)
                }
                Thread.sleep(150L)
                AppSingleton.cameraControl.startLiveview()
            }
            catch (e: Exception)
            {
                Log.e(TAG, "ERR>Change RunMode to rec ${e.message}")
            }
        }
    }

    companion object {
        private val TAG = ContentListViewModel::class.java.simpleName

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
