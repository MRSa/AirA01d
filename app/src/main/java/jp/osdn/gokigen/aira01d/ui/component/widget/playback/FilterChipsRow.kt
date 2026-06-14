package jp.osdn.gokigen.aira01d.ui.component.widget.playback

import android.text.format.DateFormat
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import jp.osdn.gokigen.aira01d.R
import jp.osdn.gokigen.aira01d.ui.model.ContentListViewModel

@Composable
fun FilterChipsRow(
    currentSort: ContentListViewModel.SortOrder,
    onSortChange: (ContentListViewModel.SortOrder) -> Unit,
    currentExt: ContentListViewModel.ExtensionFilter,
    onExtChange: (ContentListViewModel.ExtensionFilter) -> Unit,
    startDate: Long?,
    onStartDateChange: (Long?) -> Unit,
    endDate: Long?,
    onEndDateChange: (Long?) -> Unit,
    modifier: Modifier = Modifier
) {
    // ダイアログの表示管理フラグ
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }

    // タイムスタンプを 「yyyy/MM/dd」の文字列に変換する関数
    val formatDate = { time: Long -> DateFormat.format("yyyy/MM/dd", time).toString() }

    Column(modifier = modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp)) {

        // ソート順の切り替え (Row)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = currentSort == ContentListViewModel.SortOrder.NEWEST,
                onClick = { onSortChange(ContentListViewModel.SortOrder.NEWEST) },
                label = { Text(stringResource(R.string.sort_order_newest)) }
            )
            FilterChip(
                selected = currentSort == ContentListViewModel.SortOrder.OLDEST,
                onClick = { onSortChange(ContentListViewModel.SortOrder.OLDEST) },
                label = { Text(stringResource(R.string.sort_order_oldest)) }
            )
        }

        // ─── 日付フィルター ───
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // 開始日チップ
            FilterChip(
                selected = startDate != null,
                onClick = { showStartPicker = true },
                label = {
                    Text(
                        if (startDate != null) {
                            "${stringResource(R.string.specify_start_date)}: ${formatDate(startDate)}"
                        } else {
                            stringResource(R.string.specify_start_date)
                        }
                    )
                },
                trailingIcon = if (startDate != null) {
                    {
                        IconButton(
                            onClick = { onStartDateChange(null) },
                            modifier = Modifier.size(18.dp)
                        ) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear start date")
                        }
                    }
                } else null
            )

            // 終了日チップ
            FilterChip(
                selected = endDate != null,
                onClick = { showEndPicker = true },
                label = {
                    Text(
                        if (endDate != null) {
                            "${stringResource(R.string.specify_end_date)}: ${formatDate(endDate)}"
                        } else {
                            stringResource(R.string.specify_end_date)
                        }
                    )
                },
                trailingIcon = if (endDate != null) {
                    {
                        IconButton(
                            onClick = { onEndDateChange(null) },
                            modifier = Modifier.size(18.dp)
                        ) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear end date")
                        }
                    }
                } else null
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // 拡張子フィルタの切り替え (横スクロール)
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ContentListViewModel.ExtensionFilter.entries.forEach { filter ->
                FilterChip(
                    selected = currentExt == filter,
                    onClick = { onExtChange(filter) },
                    label = {
                        Text(
                            when (filter) {
                                ContentListViewModel.ExtensionFilter.JPEG -> stringResource(R.string.file_ext_jpeg)
                                ContentListViewModel.ExtensionFilter.RAW -> stringResource(R.string.file_ext_raw)
                                ContentListViewModel.ExtensionFilter.MOV -> stringResource(R.string.file_ext_movie)
                                ContentListViewModel.ExtensionFilter.OTHER -> stringResource(R.string.file_ext_other)
                                ContentListViewModel.ExtensionFilter.ALL -> stringResource(R.string.file_ext_all)
                            }
                        )
                    }
                )
            }
        }
    }

    // ─── ダイアログの表示制御 ───
    if (showStartPicker) {
        MyDatePickerDialog(
            onDateSelected = {
                onStartDateChange(it)
                showStartPicker = false
            },
            onDismiss = { showStartPicker = false }
        )
    }

    if (showEndPicker) {
        MyDatePickerDialog(
            onDateSelected = {
                // 終了日はその日の「23時59分59秒」にしたいので、少し時間を進めるか、
                // フィルターロジック側で調整できるように調整します（後述）
                onEndDateChange(it)
                showEndPicker = false
            },
            onDismiss = { showEndPicker = false }
        )
    }
}