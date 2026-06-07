package jp.osdn.gokigen.aira01d.ui.component.screen.liveview

import android.content.res.Configuration
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalConfiguration
import androidx.navigation.NavHostController
import jp.osdn.gokigen.a01lib.camera.interfaces.ICameraConnectionStatus
import jp.osdn.gokigen.aira01d.AppSingleton
import jp.osdn.gokigen.aira01d.ui.model.CameraStatusViewModel
import jp.osdn.gokigen.aira01d.ui.model.LiveviewViewModel
import jp.osdn.gokigen.aira01d.ui.model.PreferenceViewModel
import jp.osdn.gokigen.aira01d.ui.model.SelfTimerViewModel

@Composable
fun LiveviewScreen(
    navController: NavHostController,
    liveviewModel: LiveviewViewModel,
    cameraStatusViewModel: CameraStatusViewModel,
    selfTimerViewModel: SelfTimerViewModel,
    preferenceViewModel: PreferenceViewModel
) {
    val configuration = LocalConfiguration.current

    // ----- Liveview 画面を開いたときに、未接続だったら接続する
    LaunchedEffect(Unit) {

        // -----
        val isEnabled = preferenceViewModel.getConnectCameraAutomaticallySync()
        if (isEnabled) {
            when (val connectionStatus = AppSingleton.cameraControl.getCameraConnectionStatus()) {
                ICameraConnectionStatus.CameraConnectionStatus.EXCEPTION,
                ICameraConnectionStatus.CameraConnectionStatus.DISCONNECTED,
                ICameraConnectionStatus.CameraConnectionStatus.NOT_FOUND,
                ICameraConnectionStatus.CameraConnectionStatus.ERROR -> {
                    // ---- パラメータが自動接続 ONで、カメラが接続されていない場合...
                    Log.v("LiveviewScreen", " $isEnabled : $connectionStatus")
                    AppSingleton.cameraControl.connectToCamera()
                }
                else -> {}
            }
        }
    }

    // 画面の向きによって表示するコンポーネントを切り替える
    if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)
    {
        // ----- 横向き
        LiveviewScreenLandscape(
            navController,
            liveviewModel,
            cameraStatusViewModel,
            selfTimerViewModel
        )
    } else {
        // ----- 縦向き
        LiveviewScreenPortrait(
            navController,
            liveviewModel,
            cameraStatusViewModel,
            selfTimerViewModel
        )
    }
}