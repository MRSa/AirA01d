package jp.osdn.gokigen.aira01d.ui.component.widget.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import jp.osdn.gokigen.aira01d.R
import jp.osdn.gokigen.aira01d.ui.model.CameraProfilesViewModel
import jp.osdn.gokigen.aira01d.ui.model.LiveviewViewModel

enum class CameraProfilesDialogTab{ PULL, APPLY }

@Composable
fun CameraProfilesDialog(
    liveViewModel: LiveviewViewModel,
    viewModel: CameraProfilesViewModel,
    onDismissRequest: () -> Unit = {},
    onPull: (String) -> Unit = {},
    onDelete: (String) -> Unit = {},
    onRename: (String) -> Unit = {},
    onApply: (String) -> Unit = {},
    onImport: (String) -> Unit = {},
    onExport: (String) -> Unit = {}
) {
    val colorScheme = MaterialTheme.colorScheme
    val selectedTabIndex = viewModel.executionModeIndex.observeAsState()

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .wrapContentHeight()
                .border(1.dp, colorScheme.outline, ShapeDefaults.Medium),
            shape = ShapeDefaults.Medium,
            colors = CardDefaults.cardColors(
                containerColor = colorScheme.surface
            )
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // --- ヘッダーバー ---
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .background(colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.dialog_camera_profiles),
                        color = colorScheme.onPrimary,
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                HorizontalDivider(color = colorScheme.outline)

                // --- メインコンテンツエリア ---
                val tabs = CameraProfilesDialogTab.entries.toTypedArray()

                // Row の高さを右側コンテンツの最小必要高さ (IntrinsicSize.Min) に自動追従
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min)
                ) {
                    // 左側：Tab コンポーネントを使用したサイドナビゲーション
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(colorScheme.surfaceContainer)
                    ) {
                        tabs.forEachIndexed { index, tab ->
                            val isSelected = selectedTabIndex.value == index
                            val tabTitle = when (tab)
                            {
                                CameraProfilesDialogTab.PULL -> stringResource(R.string.tab_title_camera_profiles_pull)
                                CameraProfilesDialogTab.APPLY -> stringResource(R.string.tab_title_camera_profiles_apply)
                            }
                            Tab(
                                selected = isSelected,
                                onClick = { viewModel.setExecutionMode(index) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .background(
                                        if (isSelected) colorScheme.surface
                                        else colorScheme.surfaceContainer
                                    ),
                                unselectedContentColor = colorScheme.onSurfaceVariant,
                                selectedContentColor = colorScheme.primary
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Text(
                                        text = tabTitle,
                                        style = MaterialTheme.typography.labelLarge,
                                        modifier = Modifier.padding(start = 12.dp)
                                    )
                                }
                            }
                            HorizontalDivider(color = colorScheme.outlineVariant)
                        }

                        // 下部の余白エリア
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .background(colorScheme.surfaceContainer)
                        )
                    }

                    VerticalDivider(color = colorScheme.outline)

                    // 右側：選択されたタブに対応する画面を表示
                    Box(
                        modifier = Modifier
                            .weight(3f)
                            .padding(16.dp)
                    ) {
                        when (tabs[selectedTabIndex.value ?: 0]) {
                            CameraProfilesDialogTab.PULL -> CameraProfilesPullTab(
                                liveViewModel = liveViewModel,
                                onPull = onPull,
                            )
                            CameraProfilesDialogTab.APPLY -> CameraProfilesApplyTab(
                                liveViewModel = liveViewModel,
                                viewModel = viewModel,
                                onDelete = onDelete,
                                onRename = onRename,
                                onApply = onApply,
                                onExport = onExport,
                                onImport = onImport
                            )
                        }
                    }
                }
            }
        }
    }
}
