package jp.osdn.gokigen.aira01d

import android.app.Application
import android.util.Log
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import jp.osdn.gokigen.a01lib.camera.omds.OmdsCameraControlSingleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

object AppScope {
    val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
}

class AppSingleton : Application()
{
    override fun onCreate()
    {
        super.onCreate()
        Log.v(TAG, "AppSingleton::create()")
        instance = this

        // 起動時に初期化（この時点でインスタンスが作られます）
        _cameraControl = OmdsCameraControlSingleton()

        SingletonImageLoader.setSafe { cameraImageLoader } // 基本の通信は制限をかける
    }

    companion object
    {
        private val TAG = AppSingleton::class.java.simpleName
        private var _cameraControl: OmdsCameraControlSingleton? = null
        const val CAMERA_BASE_URL = "http://192.168.0.10"
        private lateinit var instance: AppSingleton

        val cameraImageLoader: ImageLoader by lazy {
            val dispatcher = Dispatcher(Executors.newFixedThreadPool(3)).apply {
                maxRequests = 3          // 同時実行数制限
                maxRequestsPerHost = 3
            }

            val okHttpClient = OkHttpClient.Builder()
                .dispatcher(dispatcher)
                .connectTimeout(5, TimeUnit.SECONDS) // タイムアウト値
                .readTimeout(7, TimeUnit.SECONDS)
                .build()

            ImageLoader.Builder(instance.applicationContext) // 安全な ApplicationContext を使用
                .components {
                    add(OkHttpNetworkFetcherFactory(callFactory = okHttpClient))
                }
                .build()
        }

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
