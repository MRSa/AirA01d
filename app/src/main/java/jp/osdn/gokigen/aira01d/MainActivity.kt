package jp.osdn.gokigen.aira01d

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import jp.osdn.gokigen.aira01d.preference.PreferenceRepository
import jp.osdn.gokigen.aira01d.ui.component.ViewRootComponent
import jp.osdn.gokigen.aira01d.ui.model.CameraStatusViewModel
import jp.osdn.gokigen.aira01d.ui.model.LiveviewViewModel
import jp.osdn.gokigen.aira01d.ui.model.PreferenceViewModel
import jp.osdn.gokigen.aira01d.ui.model.SelfTimerViewModel
import jp.osdn.gokigen.aira01d.ui.theme.AirA01dTheme

class MainActivity : ComponentActivity()
{
    private val myLiveviewViewModel: LiveviewViewModel by viewModels()
    private val myCameraStatusViewModel: CameraStatusViewModel by viewModels()
    private val mySelfTimerViewModel: SelfTimerViewModel by viewModels()
    private val myPreferenceViewModel: PreferenceViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                // Repository を作成して ViewModel に渡す
                val repository = PreferenceRepository(applicationContext)
                return PreferenceViewModel(repository) as T
            }
        }
    }

    // 権限リクエストのランチャーは、onCreateの直下（またはプロパティ初期化時）で登録する
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (!allGranted && !allPermissionsGranted()) {
            Toast.makeText(this, getString(R.string.permission_not_granted), Toast.LENGTH_SHORT).show()
            Log.v(TAG, "----- APPLICATION LAUNCH ABORTED -----")
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        // ----- Splash Screenの表示
        installSplashScreen()

        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        // --- ステータスバーを非表示にする処理 ---
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.let { controller ->
                // ステータスバー（および必要ならナビゲーションバー）を隠す
                controller.hide(WindowInsets.Type.statusBars())
                // 画面端をスワイプした時に一時的に表示される設定（没入モード）
                controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            // API 29以前の古いデバイス向けで画面を広げる
            @Suppress("DEPRECATION")
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        // ------------------------------------------

        // ----- 画面の常時点灯設定
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // 初期化処理
        if (savedInstanceState == null) {
            // ViewModelの初期化 (イベント登録の関係から、CameraControl の方を先に初期化する必要あり...）
            AppSingleton.cameraControl.initialize(myLiveviewViewModel)
            myLiveviewViewModel.initializeViewModel(applicationContext)
            myCameraStatusViewModel.initializeViewModel()
            mySelfTimerViewModel.initializeViewModel()
            myPreferenceViewModel.initializeViewModel()
        }

        // Composeのセットアップ
        val rootComponent = ViewRootComponent(applicationContext)
        rootComponent.setViewModels(myLiveviewViewModel, myCameraStatusViewModel, mySelfTimerViewModel, myPreferenceViewModel)

        setContent {
            AirA01dTheme {
                rootComponent.Content()
            }
        }

        // 権限チェックとリクエスト
        checkAndRequestPermissions()
    }

    private fun checkAndRequestPermissions() {
        if (!allPermissionsGranted()) {
            requestPermissionLauncher.launch(REQUIRED_PERMISSIONS)
        }
    }

    private fun allPermissionsGranted(): Boolean {
        return REQUIRED_PERMISSIONS.all { permission ->
            // ----- Android 17 (Cinnamon Bun / SDK 37) 以降の特定権限チェックにも対応
            if (permission == Manifest.permission.ACCESS_LOCAL_NETWORK && Build.VERSION.SDK_INT < Build.VERSION_CODES.CINNAMON_BUN) {
                true
            } else {
                ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean
    {
        when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP,
            KeyEvent.KEYCODE_CAMERA -> {
                // ----- リモートシャッター用のキーダウン情報を拾う
                try
                {
                    if (myLiveviewViewModel.isLiveViewActivated())
                    {
                        // ----- ライブビューが動作中のときのみ、シャッター動作を指示する
                        myCameraStatusViewModel.tryCapture()
                        return true
                    }
                }
                catch (e: Exception)
                {
                    e.printStackTrace()
                }
            }
            else -> { }
        }
        return super.onKeyDown(keyCode, event)
    }

    companion object
    {
        private val TAG = MainActivity::class.java.simpleName
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.VIBRATE,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.ACCESS_LOCAL_NETWORK,
        )
    }
}
