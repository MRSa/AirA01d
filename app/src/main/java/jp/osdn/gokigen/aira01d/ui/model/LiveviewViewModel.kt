package jp.osdn.gokigen.aira01d.ui.model

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.RectF
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import jp.osdn.gokigen.a01lib.camera.interfaces.ICameraEventNotify
import jp.osdn.gokigen.a01lib.camera.interfaces.IGetRecordImage
import jp.osdn.gokigen.a01lib.camera.interfaces.liveview.IImageDataReceiver
import jp.osdn.gokigen.aira01d.AppSingleton

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LiveviewViewModel(application: Application): ViewModel(), IImageDataReceiver, ICameraEventNotify, IGetRecordImage.RecordImageCallback
{
    private val _liveViewBitmap = MutableStateFlow<Bitmap?>(null)
    val liveViewBitmap: StateFlow<Bitmap?> = _liveViewBitmap.asStateFlow()

    private val _lastCapturedImage = MutableStateFlow<Bitmap?>(null)
    val lastCapturedImage: StateFlow<Bitmap?> = _lastCapturedImage.asStateFlow()

    private val _receiveCount : MutableLiveData<Int> by lazy { MutableLiveData<Int>() }
    val receiveCount : LiveData<Int> = _receiveCount

    private val _isLvActivated : MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }
    val isLvActivated : LiveData<Boolean> = _isLvActivated

    private val _isMirrorMode : MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }
    val isMirrorMode : LiveData<Boolean> = _isMirrorMode

    private val _isGridOn : MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }
    val isGridOn : LiveData<Boolean> = _isGridOn

    private val _isShowFocusFrame : MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }
    val isShowFocusFrame : LiveData<Boolean> = _isShowFocusFrame

    private val _focusFrameStatus: MutableLiveData<FocusFrameStatus> by lazy { MutableLiveData<FocusFrameStatus>() }
    val focusFrameStatus : LiveData<FocusFrameStatus> = _focusFrameStatus

    private val _focusFrameRectangle: MutableLiveData<RectF> by lazy { MutableLiveData<RectF>() }
    val focusFrameRectangle : LiveData<RectF> = _focusFrameRectangle

    init {
        try
        {
            // ----- バックグラウンドスレッドでデコード処理を実行
            viewModelScope.launch(Dispatchers.Default) {
                val options = BitmapFactory.Options().apply {
                    inMutable = true
                }

                // リソースからビットマップデータを生成する
                val dummyBitmap = BitmapFactory.decodeResource(
                    application.resources,
                    jp.osdn.gokigen.aira01d.R.drawable.screen_background,
                    options
                )

                if (dummyBitmap != null)
                {
                    // 既存の Bitmap があれば解放し、読み込んだ初期データをセットする
                    _liveViewBitmap.value?.recycle()
                    _liveViewBitmap.value = dummyBitmap
                }
                // リソースからビットマップデータを生成する
                val lastImageBitmap = BitmapFactory.decodeResource(
                    application.resources,
                    jp.osdn.gokigen.aira01d.R.drawable.outline_image_24,
                    options
                )
                if (lastImageBitmap != null)
                {
                    // 既存の Bitmap があれば解放し、読み込んだ初期データをセットする
                    _lastCapturedImage.value?.recycle()
                    _lastCapturedImage.value = lastImageBitmap
                }
            }

            // ----- データの初期化
            _receiveCount.postValue(0)
            _isMirrorMode.postValue(false)
            _isLvActivated.postValue(false)
            _isGridOn.postValue(false)
            _isShowFocusFrame.postValue(false)
            _focusFrameStatus.postValue(FocusFrameStatus.None)
        }
        catch (t: Throwable)
        {
            t.printStackTrace()
        }
    }

    fun subscribeEvents()
    {
        // ----- イベント受信を設定する
        AppSingleton.cameraControl.subscribeEventReceiver(this)
    }

    // ----- ライブビューが動作中かどうか
    fun isLiveViewActivated() : Boolean
    {
        return (_isLvActivated.value ?: false)
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

    // ----- グリッド表示をするかどうかの設定
    fun setGridOn(isGridOn: Boolean)
    {
        _isGridOn.postValue(isGridOn)
    }

    // ----- 画面がタッチされた。(0.0～1.0の間で)
    fun onTouchPosition(posX: Float, posY: Float)
    {
        Log.d(TAG, "Touch Position: x=${"%.3f".format(posX)}, y=${"%.3f".format(posY)}")
        try
        {
            if (_isLvActivated.value == true)
            {
                // ---- ライブビューが有効なときのみ AFを駆動させる
                AppSingleton.cameraControl.getFocusingControl().driveAutoFocus(posX, posY)
                _isShowFocusFrame.postValue(true)
                _focusFrameRectangle.postValue(createFocusRectangle(posX, posY))
                _focusFrameStatus.postValue(FocusFrameStatus.Running)
            }
            else
            {
                Log.v(TAG, "Liveview is not activated...")
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    fun unlockFocus()
    {
        Log.d(TAG, "Unlock Focus")
        try
        {
            if (_isLvActivated.value == true)
            {
                // ---- ライブビューが有効なときのみ AF Lockを解除させる
                AppSingleton.cameraControl.getFocusingControl().unlockAutoFocus()

                // ----- イベントは来ないので解除する
                _isShowFocusFrame.postValue(false)
                _focusFrameStatus.postValue(FocusFrameStatus.None)
            }
            else
            {
                Log.v(TAG, "Liveview is not activated...")
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun createFocusRectangle(posX: Float, posY: Float): RectF
    {
        // --- AF駆動時の枠 （固定値... 正方形にする)
        val focusWidth = 0.125f // 0.125 is rough estimate.
        val focusHeight = 0.125f * IMAGE_SCALE_X / IMAGE_SCALE_Y
        return RectF(posX - focusWidth / 2.0f, posY - focusHeight / 2.0f, posX + focusWidth / 2.0f, posY + focusHeight / 2.0f)
    }

    override fun onCleared()
    {
        super.onCleared()
        _liveViewBitmap.value = null
        _lastCapturedImage.value = null
        _receiveCount.postValue(0)
        _isMirrorMode.postValue(false)
        _isLvActivated.postValue(false)
        _isGridOn.postValue(false)
        _isShowFocusFrame.postValue(false)
        _focusFrameStatus.postValue(FocusFrameStatus.None)
    }

    override fun getSubscribeId(): String { return ("LiveviewViewModel") }

    override fun receivedCameraEvent(eventMessage: ByteArray)
    {
        if (eventMessage.size < 4) return  // 期待した文字列の長さがなかったので解析はしない
        val appId = eventMessage[0]
        val event = (eventMessage[1].toInt() and 0xFF).toByte() // eventMessage[1]
        val length = ((eventMessage[2].toInt() and 0xFF) shl 8) or (eventMessage[3].toInt() and 0xFF)
        val dataBody = String(eventMessage, 4, eventMessage.size - 4, Charsets.UTF_8)
        //Log.v(TAG, "receivedCameraEvent(LiveviewViewModel) : $appId [evt:$event] len:$length  $dataBody ")

        when (checkCameraEvent(appId, event, length))
        {
            CameraEvent.FocusResult -> {
                val focusFrame = parseFocusResult(dataBody)
                if (focusFrame == null) { updateFocusStatus(false, FocusFrameStatus.Failed, null) } else { updateFocusStatus(true, FocusFrameStatus.Focused, focusFrame) } // 合焦結果通知
            }
            CameraEvent.FocusUnknown -> { updateFocusStatus(false, FocusFrameStatus.Failed, null) }  // 現物合わせ
            CameraEvent.FinishCreateLatestImage -> { getLastCapturedImage() }
            else -> { }  // 処理しない
        }
    }

    override fun statusWatcherConsecutiveErrorCount(count: Int)
    {
        //_consecutiveErrorCount.postValue(count)
    }

    fun getLastRecordImage()
    {
        getLastCapturedImage()
    }

    private fun getLastCapturedImage()
    {
        Log.v(TAG, " Get last captured image...")
        AppSingleton.cameraControl.getGetRecordImage().getCapturedImage(false, this)
        updateFocusStatus(false, FocusFrameStatus.None, null)
    }

    private fun updateFocusStatus(isShowFrame: Boolean, status: FocusFrameStatus, focusFrame: RectF?)
    {
        // 合焦成功、フォーカスフレームを表示する
        _isShowFocusFrame.postValue(isShowFrame)
        _focusFrameStatus.postValue(status)
        _focusFrameRectangle.postValue(focusFrame)
    }

    // カメラからのイベント通知を解析し、イベント分岐を行う
    private fun checkCameraEvent(appId: Byte, event: Byte, length: Int) : CameraEvent
    {
        if ((appId == 2.toByte())&&(event == 101.toByte())&&(length > 0))
        {
            // --- 101: 合焦結果通知
            return CameraEvent.FocusResult
        }
        if (event == 0x82.toByte()&&(length == 0))
        {
            // --- ??(0x82 == -126): フォーカス合わせに失敗？ (1000 0010)
            return CameraEvent.FocusUnknown
        }
        if (event == 108.toByte())
        {
            // --- 108: 撮影確認画像生成完了
            return CameraEvent.FinishCreateLatestImage
        }
        return CameraEvent.Unknown
    }

    private fun parseFocusResult(dataBody: String): RectF?
    {
        // フォーカシング結果通知の解釈 (dataBodyに以下の値が入ってくる)
        //  失敗: <?xml version="1.0"?><root><result>ng</result></root>
        //  成功: <?xml version="1.0"?><root><result>ok</result><location>0316x0228</location><size>0072x0072</size></root>

        // --- そもそも成功(ok)でなければ null を返して終了
        if (!dataBody.contains("<result>ok</result>")) return null

        val frame = try {
            // --- <location>タグの中身を切り出し (例: "0316x0228")
            val locationStr = dataBody.substringAfter("<location>").substringBefore("</location>")

            // --- <size>タグの中身を切り出し (例: "0072x0072")
            val sizeStr = dataBody.substringAfter("<size>").substringBefore("</size>")

            // --- 'x' で分割して数値に変換
            val locParts = locationStr.split('x')
            val sizeParts = sizeStr.split('x')

            // --- 640x480 の座標系で取得
            val rawX = locParts[0].toFloat()
            val rawY = locParts[1].toFloat()
            val rawW = sizeParts[0].toFloat()
            val rawH = sizeParts[1].toFloat()

            // --- 640x480 の座標系を 0.0 ~ 1.0 に正規化
            val left = rawX / IMAGE_SCALE_X
            val top = rawY / IMAGE_SCALE_Y
            val width = rawW / IMAGE_SCALE_X
            val height = rawH / IMAGE_SCALE_Y

            // --- RectF を作成 (left, top, right, bottom) ：　値が 1.0 を超えないようにする
            RectF(
                left,
                top,
                (left + width).coerceAtMost(1.0f),
                (top + height).coerceAtMost(1.0f)
            )
        }
        catch (_: Exception)
        {
            // パース失敗（タグがない、数値じゃない等）の場合は null
            null
        }
        Log.v(TAG, "Focus Frame: $frame")
        return frame
    }

    override fun receivedRecordImage(isLastJpeg: Boolean, receivedContent: ByteArray?)
    {
        Log.v(TAG, "receivedRecordImage() : length: ${receivedContent?.size}")
        viewModelScope.launch {
            val bitmap = if (receivedContent != null) {
                // 重いデコード処理を Default ディスパッチャ（バックグラウンド）で実行
                withContext(Dispatchers.Default) {
                    try {
                        BitmapFactory.decodeByteArray(receivedContent, 0, receivedContent.size)
                    }
                    catch (e: Exception)
                    {
                        Log.e(TAG, "Error decoding bitmap", e)
                        null
                    }
                }
            } else {
                null
            }
            // StateFlow の更新（メインスレッドで安全に実行される）
            _lastCapturedImage.value = bitmap
        }
    }

    private enum class CameraEvent {
        Unknown, FocusResult, FocusUnknown, FinishCreateLatestImage
    }

    // フォーカスフレームの状態
    enum class FocusFrameStatus {
        Running, Focused, Failed, Errored, None
    }

    companion object
    {
        private val TAG = LiveviewViewModel::class.java.simpleName
        private const val LIMIT_COUNT = 99999999

        private const val IMAGE_SCALE_X = 640.0f
        private const val IMAGE_SCALE_Y = 480.0f

        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                // アプリケーションのContextを取得
                val application = extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]!!

                return LiveviewViewModel(application = application) as T
            }
        }
    }
}
