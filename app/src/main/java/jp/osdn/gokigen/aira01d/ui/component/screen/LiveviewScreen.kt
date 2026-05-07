package jp.osdn.gokigen.aira01d.ui.component.screen

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.navigation.NavHostController
import jp.osdn.gokigen.aira01d.ui.model.CameraStatusViewModel
import jp.osdn.gokigen.aira01d.ui.model.LiveviewViewModel
import jp.osdn.gokigen.aira01d.ui.model.SelfTimerViewModel

@Composable
fun LiveviewScreen(
    navController: NavHostController,
    liveviewModel: LiveviewViewModel,
    cameraStatusViewModel: CameraStatusViewModel,
    selfTimerViewModel: SelfTimerViewModel
) {
    val configuration = LocalConfiguration.current

    // 画面の向きによって表示するコンポーネントを切り替える
    if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)
    {
        // ----- 横向き
        LiveviewScreenLandscape(navController, liveviewModel, cameraStatusViewModel, selfTimerViewModel)
    } else {
        // ----- 縦向き
        LiveviewScreenPortrait(navController, liveviewModel, cameraStatusViewModel, selfTimerViewModel)
    }
}