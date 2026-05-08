package jp.osdn.gokigen.aira01d.ui.component.widget

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import jp.osdn.gokigen.aira01d.R
import jp.osdn.gokigen.aira01d.ui.model.CameraStatusViewModel
import jp.osdn.gokigen.aira01d.ui.model.LiveviewViewModel


@Composable
fun RemainBatteryArea(
    viewModel: LiveviewViewModel,
    controlModel: CameraStatusViewModel,
    modifier: Modifier = Modifier
) {
    // ----- ステータスを監視する
    val isLvActivated = viewModel.isLvActivated.observeAsState()
    val batteryLevel = controlModel.batteryLevel.observeAsState()

    // ----- ステータスに合わせてアイコンをと色を決める
    val iconId = when (batteryLevel.value)
    {
        "SUPPLY_FULL" -> R.drawable.outline_battery_charging_90_24
        "SUPPLY_LOW" -> R.drawable.outline_battery_charging_60_24
        "SUPPLY_WARNING" -> R.drawable.outline_battery_charging_30_24
        "EMPTY_AC" -> R.drawable.outline_battery_charging_20_24
        "FULL" -> R.drawable.outline_battery_full_24
        "LOW" -> R.drawable.outline_battery_3_bar_24
        "WARNING" -> R.drawable.outline_battery_1_bar_24
        "EMPTY" -> R.drawable.outline_battery_0_bar_24
        "CHARGE" -> R.drawable.outline_battery_charging_full_24
        "UNKNOWN" -> R.drawable.outline_battery_unknown_24
        else -> R.drawable.outline_battery_unknown_24
    }
    val iconColor = if (isLvActivated.value == true) {
        when (batteryLevel.value)
        {
            "SUPPLY_FULL" -> MaterialTheme.colorScheme.onSurfaceVariant
            "SUPPLY_LOW" -> MaterialTheme.colorScheme.onSurfaceVariant
            "SUPPLY_WARNING" -> MaterialTheme.colorScheme.onSurfaceVariant
            "EMPTY_AC" -> MaterialTheme.colorScheme.onSurfaceVariant
            "FULL" -> MaterialTheme.colorScheme.primary
            "LOW" -> MaterialTheme.colorScheme.primary
            "WARNING" -> MaterialTheme.colorScheme.tertiary
            "EMPTY" -> MaterialTheme.colorScheme.error
            "CHARGE" -> MaterialTheme.colorScheme.primary
            "UNKNOWN" -> MaterialTheme.colorScheme.error
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        }
    } else {
        MaterialTheme.colorScheme.primary
    }

    // ----- ボタンの表示
    IconButton(
        onClick = { },
        modifier = modifier.size(48.dp)
    ) {
        Icon(
            painter = painterResource(iconId),
            contentDescription = "battery status",
            tint = iconColor
        )
    }

}
