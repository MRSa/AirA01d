package jp.osdn.gokigen.aira01d.ui.component.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import jp.osdn.gokigen.aira01d.ui.component.widget.property.OpcCameraPropertySettingScreen
import jp.osdn.gokigen.aira01d.ui.model.CameraStatusViewModel

@Composable
fun CameraPreferenceScreen(
    navController: NavHostController,
    viewModel: CameraStatusViewModel,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            Column {
                ReturnToMainScreenRow(onBackClick = { navController.popBackStack() })
                HorizontalDivider()
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            OpcCameraPropertySettingScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() },
                modifier = modifier
            )
        }
    }
}
