package jp.osdn.gokigen.aira01d

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import jp.osdn.gokigen.aira01d.preference.PreferenceValueInitializer
import jp.osdn.gokigen.aira01d.ui.component.ViewRootComponent
import jp.osdn.gokigen.aira01d.ui.model.CameraStatusViewModel
import jp.osdn.gokigen.aira01d.ui.model.LiveviewViewModel
import jp.osdn.gokigen.aira01d.ui.theme.AirA01dTheme

class MainActivity : ComponentActivity()
{
    private val myLiveviewViewModel: LiveviewViewModel by viewModels()
    private val myCameraStatusViewModel: CameraStatusViewModel by viewModels()

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
        // ----- Splash Screenは super.onCreate() の前に呼ぶ
        installSplashScreen()
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        // ----- 画面の常時点灯設定
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // 初期化処理
        if (savedInstanceState == null) {
            PreferenceValueInitializer().initializePreferences(this)

            // ViewModelの初期化 (イベント登録の関係から、CameraControl の方を先に初期化する必要あり...）
            AppSingleton.cameraControl.initialize(
                myLiveviewViewModel,
                myLiveviewViewModel,
                myCameraStatusViewModel
            )
            myLiveviewViewModel.initializeViewModel(applicationContext)
            myCameraStatusViewModel.initializeViewModel()

        }

        // Composeのセットアップ
        val rootComponent = ViewRootComponent(applicationContext)
        rootComponent.setViewModels(myLiveviewViewModel, myCameraStatusViewModel)

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
