package gegeparty.guigol.com

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import gegeparty.guigol.com.notifications.Notifier
import gegeparty.guigol.com.ui.AppScaffold
import gegeparty.guigol.com.ui.MainViewModel
import gegeparty.guigol.com.ui.screens.LoginScreen
import gegeparty.guigol.com.ui.theme.GegeTheme

class MainActivity : ComponentActivity() {

    private val startRoute = mutableStateOf<String?>(null)

    private val notifPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        startRoute.value = intent?.getStringExtra(EXTRA_ROUTE)
        setContent {
            GegeTheme {
                val vm: MainViewModel = viewModel()
                val user by vm.user.collectAsStateWithLifecycle()
                LaunchedEffect(user) {
                    if (user != null && Build.VERSION.SDK_INT >= 33 && !Notifier.canNotify(this@MainActivity)) {
                        notifPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
                AnimatedContent(user != null, transitionSpec = { fadeIn() togetherWith fadeOut() }, label = "auth") { logged ->
                    if (logged) AppScaffold(vm, startRoute.value) { startRoute.value = null }
                    else LoginScreen(vm)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart(); gegeparty.guigol.com.notifications.AppState.inForeground = true
    }

    override fun onStop() {
        super.onStop(); gegeparty.guigol.com.notifications.AppState.inForeground = false
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        startRoute.value = intent.getStringExtra(EXTRA_ROUTE)
    }

    companion object {
        const val EXTRA_ROUTE = "route"
    }
}
