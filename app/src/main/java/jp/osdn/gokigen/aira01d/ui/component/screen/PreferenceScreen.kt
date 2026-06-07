package jp.osdn.gokigen.aira01d.ui.component.screen

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import jp.osdn.gokigen.aira01d.R
import jp.osdn.gokigen.aira01d.ui.model.PreferenceViewModel

/**
 * 呼び出し元から使うエントリーポイント
 * ViewModelやNavigationとの依存関係をここで解決します
 */
@Composable
fun PreferenceScreen(
    navController: NavHostController,
    prefsModel: PreferenceViewModel
) {
    val cameraAutoConnect by prefsModel.connectCameraAutomatically.collectAsStateWithLifecycle()
    val commandIssueSingle by prefsModel.commandIssueSingle.collectAsStateWithLifecycle()
    val uriHandler = LocalUriHandler.current

    PreferenceScreenMain(
        cameraAutoConnect = cameraAutoConnect,
        commandIssueSingle = commandIssueSingle,
        onBackClick = {
            // 安全なポップバックスタック
            navController.popBackStack()
        },
        onAutoConnectChanged = { isChecked ->
            prefsModel.setConnectCameraAutomatically(isChecked)
        },
        onCommandIssueSingle = { isChecked ->
            prefsModel.setCommandIssueSingle(isChecked)
        },
        onOpenUri = { url ->
            try {
                uriHandler.openUri(url)
            } catch (e: Exception) {
                Log.e("PreferenceScreen", "Could not open URI: $url", e)
            }
        }
    )
}

/**
 * UIの表示のみを担当する Stateless なコンポーザブル
 * プレビューやテストが容易になります
 */
@Composable
fun PreferenceScreenMain(
    cameraAutoConnect: Boolean,
    commandIssueSingle: Boolean,
    onBackClick: () -> Unit,
    onAutoConnectChanged: (Boolean) -> Unit,
    onCommandIssueSingle: (Boolean) -> Unit,
    onOpenUri: (String) -> Unit
) {
    Scaffold(
        topBar = {
            Column {
                // 戻るボタン行
                ReturnToMainScreenRow(onBackClick)
                HorizontalDivider()
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // 自動接続設定
            SettingSwitchItem(
                title = stringResource(R.string.label_switch_connect_camera_automatically),
                description = stringResource(R.string.description_switch_connect_camera_automatically),
                checked = cameraAutoConnect,
                onCheckedChange = onAutoConnectChanged
            )
            HorizontalDivider()

            // コマンド送信を絞る
            SettingSwitchItem(
                title = stringResource(R.string.label_switch_command_issue_single),
                description = stringResource(R.string.description_switch_command_issue_single),
                checked = commandIssueSingle,
                onCheckedChange = onCommandIssueSingle
            )
            HorizontalDivider()

            // 説明書
            val manualUrl = stringResource(R.string.pref_instruction_manual_url)
            SettingClickableItem(
                title = stringResource(R.string.pref_instruction_manual),
                subtitle = manualUrl,
                onClick = { onOpenUri(manualUrl) }
            )
            HorizontalDivider()

            // プライバシーポリシー
            val policyUrl = stringResource(R.string.pref_privacy_policy_url)
            val policyLabel = stringResource(R.string.pref_label_privacy_policy_url)
            SettingClickableItem(
                title = stringResource(R.string.pref_privacy_policy),
                subtitle = policyLabel,
                onClick = { onOpenUri(policyUrl) }
            )
            HorizontalDivider()
        }
    }
}

@Composable
fun ReturnToMainScreenRow(onBackClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onBackClick, onClickLabel = "Return to main screen")
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.baseline_arrow_back_24),
            //imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = null, // 隣にテキストがあるためnull
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = stringResource(R.string.label_return_to_liveview_screen),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun SettingSwitchItem(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(
                value = checked,
                onValueChange = onCheckedChange,
                role = Role.Switch
            )
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Switch(
                checked = checked,
                onCheckedChange = null // 行全体の toggleable で制御するため
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary
        )
    }
}

@Composable
fun SettingClickableItem(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary
        )
    }
}