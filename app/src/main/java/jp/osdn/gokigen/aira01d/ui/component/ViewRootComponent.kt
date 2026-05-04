package jp.osdn.gokigen.aira01d.ui.component

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.AbstractComposeView
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import jp.osdn.gokigen.aira01d.ui.component.screen.LiveviewScreen
import jp.osdn.gokigen.aira01d.ui.model.CameraStatusViewModel
import jp.osdn.gokigen.aira01d.ui.model.LiveviewViewModel

class ViewRootComponent @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : AbstractComposeView(context, attrs, defStyleAttr)
{
    private lateinit var myLiveviewViewModel : LiveviewViewModel
    private lateinit var myCameraStatusViewModel: CameraStatusViewModel

    fun setViewModels(liveViewModel : LiveviewViewModel, cameraStatusViewModel: CameraStatusViewModel)
    {
        this.myLiveviewViewModel = liveViewModel
        this.myCameraStatusViewModel = cameraStatusViewModel
        Log.v(TAG, " ...setViewModels...")
    }

    @Composable
    override fun Content()
    {
        val navController: NavHostController = rememberNavController()
        Surface {
            NavigationMain(
                navController = navController,
                liveViewModel = this.myLiveviewViewModel,
                cameraStatusViewModel = this.myCameraStatusViewModel)
        }
        Log.v(TAG, " ...NavigationRootComponent...")
    }

    companion object
    {
        private val TAG = ViewRootComponent::class.java.simpleName
    }
}

@Composable
fun NavigationMain(
    navController: NavHostController,
    liveViewModel : LiveviewViewModel,
    cameraStatusViewModel: CameraStatusViewModel
)
{
    MaterialTheme {
        NavHost(
            modifier = Modifier.systemBarsPadding(),
            navController = navController,
            startDestination = "LiveviewScreen"
        ) {
            composable("LiveviewScreen") {
                LiveviewScreen(
                    navController = navController,
                    liveviewModel = liveViewModel,
                    cameraStatusViewModel = cameraStatusViewModel
                )
            }
        }
    }
}
