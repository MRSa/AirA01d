package jp.osdn.gokigen.aira01d.ui.component.widget

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import jp.osdn.gokigen.aira01d.R
import jp.osdn.gokigen.aira01d.ui.component.widget.profile.CameraProfileActionConfirmDialog
import jp.osdn.gokigen.aira01d.ui.component.widget.profile.CameraProfilesDialog
import jp.osdn.gokigen.aira01d.ui.component.widget.profile.ShowActionResultDialog
import jp.osdn.gokigen.aira01d.ui.model.CameraProfilesViewModel
import jp.osdn.gokigen.aira01d.ui.model.CameraProfilesViewModel.CameraProfileOperationStatus

@Composable
fun CameraTuningButton(
    viewModel: CameraProfilesViewModel,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val fileNameToPull = viewModel.fileNameToPull.observeAsState()

    // ----- ダイアログの表示状態を管理する State -----
    val showDialog by viewModel.cameraProfileOperationStatus.observeAsState(initial = CameraProfileOperationStatus.NoDialog)

    // ----- ステータスに合わせてアイコンと色を決める -----
    val iconId = R.drawable.outline_tune_24
    val iconColor = MaterialTheme.colorScheme.primary

    // ----- ボタンの表示 -----
    IconButton(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
            viewModel.proceedCameraProfileOperation()
        },
        enabled = true,
        modifier = modifier.size(48.dp)
    ) {
        Icon(
            painter = painterResource(iconId),
            contentDescription = "Camera tuning",
            tint = iconColor
        )
    }

    // ----- ダイアログの表示(状態によって表示を変える) -----
    val showFileNameToPull = fileNameToPull.value ?: ""
    when (showDialog)
    {
        CameraProfileOperationStatus.OpenedDialog -> {
            CameraProfilesDialog(
                viewModel = viewModel,
                onDismissRequest = {
                    // --- ダイアログを閉じる
                    viewModel.proceedCameraProfileOperation()
                },
                onImport = { fileName ->
                    // TODO: ViewModelで Import 処理を呼ぶ
                    // viewModel.exportProfile(fileName)
                    viewModel.proceedCameraProfileOperation()
                },
                onExport = { fileName ->
                    // TODO: ViewModelで Export 処理を呼ぶ
                    // viewModel.exportProfile(fileName)
                    viewModel.proceedCameraProfileOperation()
                },
                onDelete = { fileName ->
                    // TODO: ViewModelで Delete 処理を呼ぶ
                    // viewModel.deleteProfile(fileName)
                },
                onRename = { fileName ->
                    // TODO: ViewModelで Rename 処理を呼ぶ
                    // viewModel.renameProfile(fileName)
                },
                onApply = { fileName ->
                    // TODO: ViewModelで Apply 処理を呼ぶ
                    // viewModel.applyProfile(fileName)
                    viewModel.proceedCameraProfileOperation()
                },
                onPull = { fileName ->
                    haptic.performHapticFeedback(HapticFeedbackType.ContextClick)

                    // --- ViewModel で Pull 処理を呼ぶ
                    viewModel.pullProfile(fileName)
                }
            )
        }
        CameraProfileOperationStatus.PullActionConfirmation -> {
            CameraProfileActionConfirmDialog(
                title = stringResource(R.string.dialog_title_confirm_pull),
                message = stringResource(id = R.string.dialog_message_confirm_pull, showFileNameToPull),
                onConfirm = { viewModel.confirmPullProfile() },
                onDismiss = { viewModel.cancelAction() }
            )
        }
        CameraProfileOperationStatus.FinishedPullAction -> {
            ShowActionResultDialog(
                isSuccess = true,
                title = stringResource(R.string.dialog_title_finish_pull),
                message = stringResource(id = R.string.dialog_message_finish_pull, showFileNameToPull),
                onDismiss = { viewModel.cancelAction() }
            )
        }
        else -> { }
    }
}
