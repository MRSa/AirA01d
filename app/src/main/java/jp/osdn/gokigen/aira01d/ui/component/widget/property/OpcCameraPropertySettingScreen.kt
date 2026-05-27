package jp.osdn.gokigen.aira01d.ui.component.widget.property

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import jp.osdn.gokigen.aira01d.R
import jp.osdn.gokigen.aira01d.StringResourceConverter
import jp.osdn.gokigen.aira01d.preference.camera.OpcProperty
import jp.osdn.gokigen.aira01d.ui.model.CameraStatusViewModel

@Composable
fun OpcCameraPropertySettingScreen(
    viewModel: CameraStatusViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
)
{
    val context = LocalContext.current

    // 起動時に XML からデータをロードする
    val allProperties = remember { StringResourceConverter().loadOpcPropertiesFromXml(context) }

    // カテゴリごとにグループ化（Map<String, List<OpcProperty>>）
    val groupedProperties = remember(allProperties) {
        allProperties.groupBy { it.category }
    }

    Scaffold(
        topBar = {
            Text(
                text = stringResource(R.string.title_camera_properties),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(16.dp)
            )
        }
    ) { innerPadding ->

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(innerPadding),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val resourceConverter = StringResourceConverter()

        // カテゴリごとにアコーディオンを生成
        groupedProperties.forEach { (categoryName, propertiesInGroup) ->

            item(key = categoryName) {
                // カテゴリごとの開閉状態管理
                var isExpanded by remember { mutableStateOf(false) }

                Column {
                    // --- カテゴリヘッダー ---
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isExpanded = !isExpanded }
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val rscId =
                                resourceConverter.getStringResourceId("cameraProp_$categoryName")
                            Text(
                                text = if (rscId == 0) {
                                    categoryName
                                } else {
                                    stringResource(resourceConverter.getStringResourceId("cameraProp_$categoryName"))
                                },
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            // 項目数のバッジ
                            Surface(
                                shape = MaterialTheme.shapes.small,
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = "${propertiesInGroup.size}",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    fontSize = 12.sp
                                )
                            }
                        }
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null
                        )
                    }

                    // --- 子要素のプロパティリスト（開閉アニメーション） ---
                    AnimatedVisibility(visible = isExpanded) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 8.dp, top = 8.dp, bottom = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            propertiesInGroup.forEach { property ->
                                XmlPropertyRowItem(
                                    viewModel = viewModel,
                                    property = property,
                                    modifier
                                )
                            }
                        }
                    }
                    HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
                }
            }
        }
    }
    }
}

@Composable
fun XmlPropertyRowItem(
    viewModel: CameraStatusViewModel,
    property: OpcProperty,
    modifier: Modifier = Modifier)
{
    Column(modifier = modifier) {
        Text(
            text = stringResource(property.labelId),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(2.dp))
        // サブ情報としてキー名を表示
        Text(
            text = property.key,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline
        )

        Spacer(modifier = Modifier.height(6.dp))

        // 設定値の選択ボタン
        CameraPropertyDetailButton(
            controlModel = viewModel,
            titleRscId = property.labelId,
            targetPropertyString = property.key,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
