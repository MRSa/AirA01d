package jp.osdn.gokigen.aira01d

import android.app.Application
import android.util.Log
import jp.osdn.gokigen.a01lib.camera.omds.OmdsCameraControlSingleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

object AppScope {
    val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
}

class AppSingleton : Application()
{
    override fun onCreate()
    {
        super.onCreate()
        Log.v(TAG, "AppSingleton::create()")
        // 起動時に初期化（この時点でインスタンスが作られます）
        _cameraControl = OmdsCameraControlSingleton()
    }

    companion object
    {
        private val TAG = AppSingleton::class.java.simpleName
        private var _cameraControl: OmdsCameraControlSingleton? = null
        const val CAMERA_BASE_URL = "http://192.168.0.10"

        // 外部公開用の変数は「絶対に Null ではない型」にする
        val cameraControl: OmdsCameraControlSingleton
            get() {
                // --- もし null（終了処理後など）なら、自動的に再生成して返す
                if (_cameraControl == null) {
                    Log.w(TAG, "cameraControl was null, re-initializing.")
                    _cameraControl = OmdsCameraControlSingleton()
                }
                return _cameraControl!!
            }

        val resourceConverter = StringResourceConverter()

        fun destroyCameraControl() {
            Log.v(TAG, "destroyCameraControl() called")

            // 参照をクリア
            _cameraControl = null
        }
    }
}
