package jp.osdn.gokigen.aira01d.ui.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import jp.osdn.gokigen.a01lib.camera.interfaces.ICaptureControl
import jp.osdn.gokigen.aira01d.AppSingleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SelfTimerViewModel : ViewModel()
{

    private var timerJob: Job? = null

    // --- State の定義 ---

    // タイマーが稼働中かどうか
    private val _isTimerActivated = MutableStateFlow(false)
    val isTimerActivated = _isTimerActivated.asStateFlow()

    // 設定されているタイマープロパティ (3s, 5s, etc...)
    private val _isTimerOn = MutableStateFlow(SelfTimerProperty.TimerOff)
    val isTimerOn = _isTimerOn.asStateFlow()

    // 残り秒数
    private val _isTimerRemainSec = MutableStateFlow(0)
    val isTimerRemainSec = _isTimerRemainSec.asStateFlow()

    // --- viewModelの初期化 ----
    fun initializeViewModel() { }

    fun startSelfTimer() {
        // 現在の設定値に基づいて秒数を決定
        val seconds = when (_isTimerOn.value) {
            SelfTimerProperty.Timer3s -> 3
            SelfTimerProperty.Timer5s -> 5
            SelfTimerProperty.Timer10s -> 10
            else -> 0
        }

        if (seconds > 0) {
            // 既存のタイマーがあればキャンセル
            timerJob?.cancel()

            timerJob = viewModelScope.launch {
                _isTimerActivated.value = true
                _isTimerRemainSec.value = seconds

                // カウントダウンループ
                for (i in seconds downTo 1) {
                    _isTimerRemainSec.value = i
                    delay(1000)
                }

                _isTimerActivated.value = false
                _isTimerRemainSec.value = 0

                // タイマーカウント終了、撮影処理に移る
                onTimerFinished()
            }
        }
    }

    fun abortSelfTimer()
    {
        // タイマーのキャンセル
        timerJob?.cancel()
        _isTimerActivated.value = false
        _isTimerRemainSec.value = 0
    }

    fun changeStatus(property: SelfTimerProperty)
    {
        _isTimerOn.value = property
    }

    private fun onTimerFinished()
    {
        // ----- 撮影処理の実行
        CoroutineScope(Dispatchers.IO).launch {
            try
            {
                AppSingleton.cameraControl.getCaptureControl().doCapture(ICaptureControl.CaptureAction.TOGGLE)
            }
            catch (e: Exception)
            {
                e.printStackTrace()
            }
        }
    }

    enum class SelfTimerProperty {
        TimerOff,
        Timer3s,
        Timer5s,
        Timer10s,
    }
}
