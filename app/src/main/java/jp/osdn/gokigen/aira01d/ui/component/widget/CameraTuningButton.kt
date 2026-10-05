package jp.osdn.gokigen.aira01d.ui.component.widget

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import jp.osdn.gokigen.aira01d.ui.component.widget.profile.RenameFileDialog
import jp.osdn.gokigen.aira01d.ui.component.widget.profile.ShowActionResultDialog
import jp.osdn.gokigen.aira01d.ui.component.widget.profile.ShowBusyActionDialog
import jp.osdn.gokigen.aira01d.ui.model.CameraProfilesViewModel
import jp.osdn.gokigen.aira01d.ui.model.CameraProfilesViewModel.CameraProfileOperationStatus

@Composable
fun CameraTuningButton(
    viewModel: CameraProfilesViewModel,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val fileNameToPull = viewModel.fileNameToPull.observeAsState()
    val fileNameToDelete = viewModel.fileNameToDelete.observeAsState()
    val fileNameToExport = viewModel.fileNameToExport.observeAsState()
    val fileNameToApply = viewModel.fileNameToApply.observeAsState()
    val fileNameToRename = viewModel.fileNameToRename.observeAsState()

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

    // SAF (CreateDocument) のランチャー定義
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        // ユーザーが保存先を選択した後に呼ばれる処理
        if (uri != null) {
            // ファイルのexport実処理...
            viewModel.exportCameraPropertyFile(uri)
        }
        else
        {
            viewModel.abortExport()
        }
    }

    // ファイル選択ダイアログ用ランチャー
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.importCameraPropertyFile(uri)
        }
        else
        {
            // --- ファイル選択（インポート処理）をキャンセル
            viewModel.cancelAction()
        }
    }

    // ----- ダイアログの表示(状態によって表示を変える) -----
    val showFileNameToPull = fileNameToPull.value ?: ""
    val showFileNameToDelete = fileNameToDelete.value ?: ""
    val showFileNameToExport = fileNameToExport.value ?: ""
    val showFileNameToApply = fileNameToApply.value ?: ""
    val showFileNameToRename = fileNameToRename.value ?: ""
    when (showDialog)
    {
        CameraProfileOperationStatus.OpenedDialog -> {
            CameraProfilesDialog(
                viewModel = viewModel,
                onDismissRequest = {
                    // --- ダイアログを閉じる
                    viewModel.proceedCameraProfileOperation()
                },
                onImport = { _ ->
                    // インポートするファイルの選択
                    importLauncher.launch(arrayOf("application/json"))
                },
                onExport = { fileName ->
                    // Export 処理 : export先を指定する
                    viewModel.exportProfileFile(fileName)
                    exportLauncher.launch(fileName)
                },
                onDelete = { fileName ->
                    // ファイルをDeleteする処理を呼ぶ
                    viewModel.deleteProfile(fileName)
                },
                onRename = { fileName ->
                    // ファイル名のリネーム処理を開始する
                    viewModel.renameProfile(fileName)
                },
                onApply = { fileName ->
                    // カメラへ設定を反映させる処理の実行
                    haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                    viewModel.applyProfile(fileName)
                },
                onPull = { fileName ->
                    // カメラからの設定読み出しと保存処理の実行
                    haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
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
        CameraProfileOperationStatus.ApplyActionConfirmation -> {
            CameraProfileActionConfirmDialog(
                title = stringResource(R.string.dialog_title_confirm_apply),
                message = stringResource(id = R.string.dialog_message_confirm_apply, showFileNameToApply),
                onConfirm = { viewModel.confirmApplyProfile() },
                onDismiss = { viewModel.cancelAction() }
            )
        }
        CameraProfileOperationStatus.ReadingProfileFromCamera -> {
            ShowBusyActionDialog(
                isAbortable = false,
                title = stringResource(R.string.dialog_title_executing_pull),
                message = stringResource(id = R.string.dialog_message_executing_pull, showFileNameToPull),
                onDismiss = { viewModel.cancelAction() }
            )
        }
        CameraProfileOperationStatus.FinishedPullAction -> {
            ShowActionResultDialog(
                isSuccess = true,
                title = stringResource(R.string.dialog_title_finish_pull),
                message = stringResource(id = R.string.dialog_message_finish_pull, showFileNameToPull),
                onDismiss = { viewModel.proceedCameraProfileOperation() }
            )
        }
        CameraProfileOperationStatus.FailedPullAction -> {
            ShowActionResultDialog(
                isSuccess = false,
                title = stringResource(R.string.dialog_title_failed_pull),
                message = stringResource(id = R.string.dialog_message_failed_pull),
                onDismiss = { viewModel.proceedCameraProfileOperation() }
            )
        }
        CameraProfileOperationStatus.ReadingFileList -> {
            ShowBusyActionDialog(
                isAbortable = false,
                title = stringResource(R.string.dialog_title_reading_file),
                message = stringResource(id = R.string.dialog_message_reading_file),
                onDismiss = { viewModel.cancelAction() }
            )
        }
        CameraProfileOperationStatus.SelectExportDirectory -> {
            ShowBusyActionDialog(
                isAbortable = false,
                title = stringResource(R.string.dialog_title_reading_file),
                message = stringResource(id = R.string.dialog_message_reading_file),
                onDismiss = { viewModel.cancelAction() }
            )
        }
        CameraProfileOperationStatus.DeleteActionConfirmation -> {
            CameraProfileActionConfirmDialog(
                title = stringResource(R.string.dialog_title_confirm_delete),
                message = stringResource(id = R.string.dialog_message_confirm_delete, showFileNameToDelete),
                onConfirm = { viewModel.confirmDeleteProfile() },
                onDismiss = { viewModel.cancelAction() }
            )
        }
        CameraProfileOperationStatus.FinishedDeleteAction -> {
            ShowActionResultDialog(
                isSuccess = true,
                title = stringResource(R.string.dialog_title_finish_delete),
                message = stringResource(id = R.string.dialog_message_finish_delete, showFileNameToPull),
                onDismiss = { viewModel.proceedCameraProfileOperation() }
            )
        }
        CameraProfileOperationStatus.FailedDeleteAction -> {
            ShowActionResultDialog(
                isSuccess = false,
                title = stringResource(R.string.dialog_title_failed_delete),
                message = stringResource(id = R.string.dialog_message_failed_delete),
                onDismiss = { viewModel.proceedCameraProfileOperation() }
            )
        }
        CameraProfileOperationStatus.ExportingProfileToFile -> {
            ShowBusyActionDialog(
                isAbortable = false,
                title = stringResource(R.string.dialog_title_exporting_file),
                message = stringResource(id = R.string.dialog_message_exporting_file, showFileNameToExport),
                onDismiss = { viewModel.cancelAction() }
            )
        }
        CameraProfileOperationStatus.FinishedExportToFile -> {
            ShowActionResultDialog(
                isSuccess = true,
                title = stringResource(R.string.dialog_title_finish_export),
                message = stringResource(id = R.string.dialog_message_finish_export, showFileNameToExport),
                onDismiss = { viewModel.proceedCameraProfileOperation() }
            )
        }
        CameraProfileOperationStatus.FailedExportToFile -> {
            ShowActionResultDialog(
                isSuccess = false,
                title = stringResource(R.string.dialog_title_failed_export),
                message = stringResource(id = R.string.dialog_message_failed_export, showFileNameToExport),
                onDismiss = { viewModel.proceedCameraProfileOperation() }
            )
        }
        CameraProfileOperationStatus.ReadingProfileFromFile -> {
            ShowBusyActionDialog(
                isAbortable = false,
                title = stringResource(R.string.dialog_title_reading_file),
                message = stringResource(id = R.string.dialog_message_reading_file),
                onDismiss = { viewModel.cancelAction() }
            )
        }
        CameraProfileOperationStatus.ApplyingProfileToCamera -> {
            ShowBusyActionDialog(
                isAbortable = false,
                title = stringResource(R.string.dialog_title_applying_profile),
                message = stringResource(id = R.string.dialog_message_applying_profile, showFileNameToApply),
                onDismiss = { viewModel.cancelAction() }
            )
        }
        CameraProfileOperationStatus.FinishedApplyAction -> {
            ShowActionResultDialog(
                isSuccess = true,
                title = stringResource(R.string.dialog_title_finish_apply),
                message = stringResource(id = R.string.dialog_message_finish_apply, showFileNameToApply),
                onDismiss = { viewModel.proceedCameraProfileOperation() }
            )
        }
        CameraProfileOperationStatus.FailedApplyAction -> {
            ShowActionResultDialog(
                isSuccess = false,
                title = stringResource(R.string.dialog_title_failed_apply),
                message = stringResource(id = R.string.dialog_message_failed_apply, showFileNameToApply),
                onDismiss = { viewModel.proceedCameraProfileOperation() }
            )
        }
        CameraProfileOperationStatus.AbortExportAction -> {
            ShowActionResultDialog(
                isSuccess = false,
                title = stringResource(R.string.dialog_title_abort_export),
                message = stringResource(id = R.string.dialog_message_abort_export),
                onDismiss = { viewModel.proceedCameraProfileOperation() }
            )
        }
        CameraProfileOperationStatus.SetRenameFileName -> {
            RenameFileDialog(
                currentName = showFileNameToRename,
                onDismissRequest = { viewModel.proceedCameraProfileOperation() },
                onConfirm = { newName -> viewModel.confirmRenameProfile(newName) }
            )
        }
        CameraProfileOperationStatus.FinishedRenameFile -> {
            ShowActionResultDialog(
                isSuccess = true,
                title = stringResource(R.string.dialog_title_finish_rename),
                message = stringResource(id = R.string.dialog_message_finish_rename),
                onDismiss = { viewModel.proceedCameraProfileOperation() }
            )
        }
        CameraProfileOperationStatus.FailedRenameFile -> {
            ShowActionResultDialog(
                isSuccess = false,
                title = stringResource(R.string.dialog_title_failed_rename),
                message = stringResource(id = R.string.dialog_message_failed_rename),
                onDismiss = { viewModel.proceedCameraProfileOperation() }
            )
        }
        CameraProfileOperationStatus.ImportingProfileFromStorage -> {
            ShowBusyActionDialog(
                isAbortable = false,
                title = stringResource(R.string.dialog_title_importing_profile),
                message = stringResource(id = R.string.dialog_message_importing_profile),
                onDismiss = { viewModel.cancelAction() }
            )
        }
        CameraProfileOperationStatus.FinishedImportAction -> {
            ShowActionResultDialog(
                isSuccess = true,
                title = stringResource(R.string.dialog_title_finish_import),
                message = stringResource(id = R.string.dialog_message_finish_import),
                onDismiss = { viewModel.proceedCameraProfileOperation() }
            )
        }
        CameraProfileOperationStatus.FailedImportAction -> {
            ShowActionResultDialog(
                isSuccess = false,
                title = stringResource(R.string.dialog_title_failed_import),
                message = stringResource(id = R.string.dialog_message_failed_import),
                onDismiss = { viewModel.proceedCameraProfileOperation() }
            )
        }
        else -> { }
    }
}
