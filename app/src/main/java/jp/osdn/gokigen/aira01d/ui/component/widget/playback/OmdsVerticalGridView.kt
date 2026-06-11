package jp.osdn.gokigen.aira01d.ui.component.widget.playback

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import jp.osdn.gokigen.a01lib.camera.interfaces.playback.ICameraFileInfo

@Composable
fun OmdsVerticalGridView(
    fileList: List<ICameraFileInfo.ImageFileInfo>,
    onItemClick: (Int) -> Unit,
    modifier: Modifier = Modifier
)
{
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 110.dp),
        modifier = modifier,
        contentPadding = PaddingValues(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        itemsIndexed(fileList) { index, file ->
            OmdsFileItemCard(
                file = file,
                onItemClick = { onItemClick(index) }
            )
        }
    }
}
