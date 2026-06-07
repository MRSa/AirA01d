package jp.osdn.gokigen.aira01d.ui.component.screen

import android.content.res.Configuration
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import jp.osdn.gokigen.a01lib.camera.interfaces.ICameraConnectionStatus
import jp.osdn.gokigen.aira01d.AppSingleton
import jp.osdn.gokigen.aira01d.ui.model.CameraStatusViewModel
import jp.osdn.gokigen.aira01d.ui.model.ContentListViewModel
import jp.osdn.gokigen.aira01d.ui.model.LiveviewViewModel
import jp.osdn.gokigen.aira01d.ui.model.PreferenceViewModel
import jp.osdn.gokigen.aira01d.ui.model.SelfTimerViewModel

@Composable
fun ContentListScreenPortrait(
    navController: NavHostController,
    viewModel: ContentListViewModel,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            Column(
                modifier = modifier.safeDrawingPadding().padding(1.dp)
            )
            {
                ReturnToMainScreenRow(onBackClick = { navController.popBackStack() }, modifier = modifier)
                HorizontalDivider()
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {

        }
    }
}
