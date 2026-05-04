package jp.osdn.gokigen.aira01d.ui.model

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.PointF
import android.graphics.RectF
import android.util.Log
import android.view.MotionEvent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import jp.osdn.gokigen.a01lib.camera.interfaces.liveview.IImageDataReceiver
import jp.osdn.gokigen.a01lib.camera.interfaces.screen.IAutoFocusFrameDisplay

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LiveviewViewModel: ViewModel(), IImageDataReceiver, IAutoFocusFrameDisplay
{
    private val _liveViewBitmap = MutableStateFlow<Bitmap?>(null)
    val liveViewBitmap: StateFlow<Bitmap?> = _liveViewBitmap.asStateFlow()

    private val _receiveCount : MutableLiveData<Int> by lazy { MutableLiveData<Int>() }
    val receiveCount : LiveData<Int> = _receiveCount

    private val _isLvActivated : MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }
    val isLvActivated : LiveData<Boolean> = _isLvActivated

    private val _isMirrorMode : MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }
    val isMirrorMode : LiveData<Boolean> = _isMirrorMode

    fun initializeViewModel(context: android.content.Context)
    {
        Log.v(TAG, "LiveviewViewModel::initializeViewModel()")
        try
        {
            // バックグラウンドスレッドでデコード処理を実行
            viewModelScope.launch(Dispatchers.Default) {
                val options = BitmapFactory.Options().apply {
                    inMutable = true
                }

                // リソースからビットマップデータを生成する
                val dummyBitmap = BitmapFactory.decodeResource(
                    context.resources,
                    jp.osdn.gokigen.aira01d.R.drawable.dummy,
                    options
                )

                if (dummyBitmap != null)
                {
                    // 既存の Bitmap があれば解放し、読み込んだ初期データをセットする
                    _liveViewBitmap.value?.recycle()
                    _liveViewBitmap.value = dummyBitmap
                }
            }
            _receiveCount.postValue(0)
            _isMirrorMode.postValue(false)
            _isLvActivated.postValue(false)
        }
        catch (t: Throwable)
        {
            t.printStackTrace()
        }
    }

    // ----- ライブビューの画像を受信した
    override fun onUpdateLiveView(data: ByteArray, metadata: Map<String, Any>?, degrees: Int)
    {
        try
        {
            if (_isLvActivated.value != true)
            {
                // ----- ライブビュー動作中のマーキング
                _isLvActivated.postValue(true)
            }
            viewModelScope.launch(Dispatchers.Default) {
                val bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)
                if (bitmap != null)
                {
                    val finalBitmap = if ((degrees == 0)&&(_isMirrorMode.value != true))
                    {
                        bitmap
                    }
                    else
                    {
                        modifyBitmap(bitmap, degrees, (_isMirrorMode.value == true))
                    }
                    // UI に反映
                    _liveViewBitmap.value = finalBitmap

                    // ---- イメージの受信カウントを更新
                    _receiveCount.postValue(_receiveCount.value?.plus(1))
                    _receiveCount.value?.let {
                        if (it > LIMIT_COUNT) {
                            // 数が大きくなりすぎたら値を戻す
                            _receiveCount.postValue(1)
                        }
                    }
                }
            }
        }
        catch (t: Throwable)
        {
            t.printStackTrace()
        }
    }

    // Bitmapの回転および鏡像処理
    private fun modifyBitmap(source: Bitmap, degrees: Int, isMirror: Boolean): Bitmap
    {
        // 回転と鏡像を行う変換行列を作る
        val matrix = Matrix()

        // 回転処理
        if (degrees != 0)
        {
            matrix.postRotate(degrees.toFloat())
        }

        // 鏡像（左右反転）処理
        if (isMirror) {
            // X軸を-1倍することで左右反転、Y軸はそのまま(1倍)
            // 反転後の座標ズレを考慮し、中心基準で反転させるためにpostScaleを使用
            matrix.postScale(-1f, 1f)
        }

        // 回転と反転を適用した新しいBitmapを作成
        val modified = Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)

        return modified
    }

    // ----- 鏡像画像にするかどうかの設定
    fun setMirrorMode(isMirrorImage: Boolean)
    {
        _isMirrorMode.postValue(isMirrorImage)
    }

    override fun onCleared()
    {
        super.onCleared()
        _liveViewBitmap.value = null
        _receiveCount.postValue(0)
        _isMirrorMode.postValue(false)
        _isLvActivated.postValue(false)
    }

    override fun getContentSizeWidth(): Float {
        TODO("Not yet implemented")
    }

    override fun getContentSizeHeight(): Float {
        TODO("Not yet implemented")
    }

    override fun getPointWithEvent(event: MotionEvent?): PointF? {
        TODO("Not yet implemented")
    }

    override fun isContainsPoint(point: PointF?): Boolean {
        TODO("Not yet implemented")
    }

    override fun showFocusFrame(
        rect: RectF?,
        status: IAutoFocusFrameDisplay.FocusFrameStatus,
        duration: Float
    ) {
        TODO("Not yet implemented")
    }

    override fun hideFocusFrame() {
        TODO("Not yet implemented")
    }

    companion object
    {
        private val TAG = LiveviewViewModel::class.java.simpleName
        private const val LIMIT_COUNT = 99999999
    }
}
