package jp.osdn.gokigen.aira01d.ui.component.widget.property

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import jp.osdn.gokigen.a01lib.camera.interfaces.ICameraStatus
import jp.osdn.gokigen.aira01d.R
import jp.osdn.gokigen.aira01d.ui.model.CameraStatusViewModel

@Composable
fun OmdsCameraPropertySettingScreen(
    viewModel: CameraStatusViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 💡画面起動時に1回だけAPI（プロパティ取得）を呼び出す
    LaunchedEffect(Unit) {
        viewModel.getPropertyDescriptorList()
    }

    // ダイアログ表示用のステート（タップされた descriptor を保持する）
    var selectedDescriptor by remember { mutableStateOf<ICameraStatus.CameraPropertyDescriptor?>(null) }

    // ViewModelからパース済みの全リストを取得
    val descriptorList = viewModel.propertyDescriptorList

    Scaffold(
        topBar = {
            Text(
                text = stringResource(R.string.title_camera_properties),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(16.dp)
            )
        }
    ) { innerPadding ->
        // ----- リストが空（通信中など）の場合の処理
        if (descriptorList.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Text(stringResource(R.string.message_getting_properties))
            }
        } else {
            // 解析されたリストの数だけ、縦にずらりと並べる
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                // items にリストを渡すと、要素の数だけ自動ループします
                items(descriptorList) { descriptor ->

                    // 各プロパティのカード（タップ可能）
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .clickable {
                                if (descriptor.attribute.contains("set"))
                                {
                                    // ----- アトリビュートの設定ができるので、ダイアログを表示する
                                    // タップされた要素をそのままステートに入れる
                                    selectedDescriptor = descriptor
                                }
                            }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // プロパティ名（例: takemode）
                            Text(
                                text = descriptor.propertyName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            // 現在値（例: P）
                            Text(
                                text = "${stringResource(R.string.message_current)} ${descriptor.current}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            // 属性（編集可否）
                            Text(
                                text = if (descriptor.attribute.contains(other = "set")) {
                                    stringResource(R.string.message_read_write) }
                                else { stringResource(R.string.message_read_only) },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            }
        }
    }

    // いずれかの項目がタップされたらダイアログを開く
    selectedDescriptor?.let { descriptor ->
        CameraPropertyDescriptorListSelectionDialog(
            controlModel = viewModel,
            propertyTitle = "${descriptor.propertyName} ",
            propertyDescriptor = descriptor,
            keyNameHeader = "",
            onClose = {
                selectedDescriptor = null
                viewModel.getPropertyDescriptorList()  // 再度プロパティリストを更新
            }
        )
    }
}
