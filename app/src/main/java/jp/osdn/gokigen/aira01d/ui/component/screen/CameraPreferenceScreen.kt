package jp.osdn.gokigen.aira01d.ui.component.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import jp.osdn.gokigen.a01lib.camera.interfaces.ICameraConnectionStatus
import jp.osdn.gokigen.aira01d.ui.component.widget.property.OmdsCameraPropertySettingScreen
import jp.osdn.gokigen.aira01d.ui.component.widget.property.OpcCameraPropertySettingScreen
import jp.osdn.gokigen.aira01d.ui.model.CameraStatusViewModel

@Composable
fun CameraPreferenceScreen(
    navController: NavHostController,
    viewModel: CameraStatusViewModel,
    modifier: Modifier = Modifier
) {
    val cameraProtocol = viewModel.cameraProtocol.observeAsState()
    Scaffold(
        topBar = {
            Column(
                modifier = modifier.safeDrawingPadding().padding(1.dp)
            ) {
                ReturnToMainScreenRow(onBackClick = { navController.popBackStack() }, modifier = modifier)
                HorizontalDivider()
            }
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            if (cameraProtocol.value == ICameraConnectionStatus.CameraProtocol.OPC)
            {
                OpcCameraPropertySettingScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() },
                    modifier = modifier
                )
            }
            else
            {
                OmdsCameraPropertySettingScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() },
                    modifier = modifier
                )
            }
        }
    }
}
