package jp.osdn.gokigen.aira01d.ui.component.screen

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.navigation.NavHostController
import jp.osdn.gokigen.aira01d.ui.model.CameraStatusViewModel
import jp.osdn.gokigen.aira01d.ui.model.LiveviewViewModel

@Composable
fun LiveviewScreen(navController: NavHostController, liveviewModel: LiveviewViewModel, cameraStatusViewModel: CameraStatusViewModel)
{
    val configuration = LocalConfiguration.current

    // 画面の向きによって表示するコンポーネントを切り替える
    if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)
    {
        // ----- 横向き
        LiveviewScreenLandscape(navController, liveviewModel, cameraStatusViewModel)
    } else {
        // ----- 縦向き
        LiveviewScreenPortrait(navController, liveviewModel, cameraStatusViewModel)
    }
}