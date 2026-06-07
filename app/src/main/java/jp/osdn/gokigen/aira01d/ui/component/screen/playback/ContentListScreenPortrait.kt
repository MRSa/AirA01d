package jp.osdn.gokigen.aira01d.ui.component.screen.playback

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import jp.osdn.gokigen.aira01d.ui.component.screen.preference.ReturnToMainScreenRow
import jp.osdn.gokigen.aira01d.ui.model.ContentListViewModel

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

        }
    }
}
