package jp.osdn.gokigen.aira01d.ui.component.screen.playback

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavHostController
import jp.osdn.gokigen.aira01d.ui.model.ContentListViewModel

@Composable
fun ContentListScreen(
    navController: NavHostController,
    viewModel: ContentListViewModel,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val context = LocalContext.current
    val lifecycleOwner = navController.currentBackStackEntry ?: LocalLifecycleOwner.current

    LaunchedEffect(Unit) {
        // ----- 画面を開いたときに実行する処理 (playbackモードに切り替える)
        viewModel.changeRunModeToPlayback()
    }

    DisposableEffect(lifecycleOwner) {
        // ----- 画面を閉じたときに実行する処理 (recモードに切り替える)
        val observer = LifecycleEventObserver { _, event ->
            // ON_STOP: 別画面に進んだ、またはBackボタンで戻って、画面が完全に見えなくなったとき
            if (event == Lifecycle.Event.ON_STOP) {
                viewModel.changeRunModeToRecord(context)
            }
        }

        // 監視を開始
        lifecycleOwner.lifecycle.addObserver(observer)

        // コンポーザブルが破棄されたら監視を解除
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // 画面の向きによって表示するコンポーネントを切り替える
    if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)
    {
        // ----- 横向き
        ContentListScreenLandscape(navController, viewModel, modifier)
    } else {
        // ----- 縦向き
        ContentListScreenPortrait(navController, viewModel, modifier)
    }
}