package jp.osdn.gokigen.aira01d.ui.component.widget.playback.omds

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import jp.osdn.gokigen.a01lib.camera.interfaces.playback.ICameraFileInfo

@Composable
fun OmdsColumnView(
    fileList: List<ICameraFileInfo.ImageFileInfo>,
    onItemClick: (Int) -> Unit,
    modifier: Modifier = Modifier
)
{
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        itemsIndexed(fileList) { index, file ->
            OmdsFileItemRow(
                file = file,
                onItemClick = { onItemClick(index) }
            )
        }
    }
}
