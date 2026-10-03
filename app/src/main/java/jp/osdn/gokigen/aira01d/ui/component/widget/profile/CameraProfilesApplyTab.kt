package jp.osdn.gokigen.aira01d.ui.component.widget.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import jp.osdn.gokigen.aira01d.R
import jp.osdn.gokigen.aira01d.ui.model.CameraProfilesViewModel


// -----------------------------------------------------------------------------
// 2. Apply 画面
// -----------------------------------------------------------------------------
@Composable
fun CameraProfilesApplyTab(
    viewModel: CameraProfilesViewModel,
    onDelete: (String) -> Unit,
    onRename: (String) -> Unit,
    onApply: (String) -> Unit,
    onExport: (String) -> Unit,
    onImport: (String) -> Unit
) {
    val fileList = remember { listOf("aaaaa", "bbbbb", "ccccc", "ddddd", "eeeee") }
    var selectedFile by remember { mutableStateOf(fileList.firstOrNull() ?: "") }
    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text(
                text = "File:",
                style = MaterialTheme.typography.bodyMedium,
                color = colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            // ファイルリスト選択ボックス
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .border(1.dp, colorScheme.outline, ShapeDefaults.Small),
                shape = ShapeDefaults.Small,
                color = colorScheme.surface
            ) {
                LazyColumn {
                    items(fileList) { file ->
                        val isSelected = file == selectedFile
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (isSelected) colorScheme.primaryContainer
                                    else colorScheme.surface
                                )
                                .clickable { selectedFile = file }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = file,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isSelected) colorScheme.onPrimaryContainer
                                else colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // アクションボタン（Delete, Rename, Apply）
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
        ) {
            // インポート指示（ローカルから読み出す）
            OutlinedButton(
                onClick = { onImport(selectedFile) }
            ) {
                Text(text = stringResource(R.string.button_camera_profiles_import))
            }
            OutlinedButton(onClick = { onExport(selectedFile) }) {
                Text(text = stringResource(R.string.button_camera_profiles_export))
            }
            OutlinedButton(onClick = { onDelete(selectedFile) }) {
                Text(text = stringResource(R.string.button_camera_profiles_delete))
            }
            OutlinedButton(onClick = { onRename(selectedFile) }) {
                Text(text = stringResource(R.string.button_camera_profiles_rename))
            }
            Button(onClick = { onApply(selectedFile) }) {
                Text(text = stringResource(R.string.button_camera_profiles_apply))
            }
        }
    }
}
