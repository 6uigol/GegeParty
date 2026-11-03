package gegeparty.guigol.com.ui

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import gegeparty.guigol.com.ui.screens.AgendaScreen
import gegeparty.guigol.com.ui.screens.BudgetScreen
import gegeparty.guigol.com.ui.screens.GuestsScreen
import gegeparty.guigol.com.ui.screens.HomeScreen
import gegeparty.guigol.com.ui.screens.IdeasScreen
import gegeparty.guigol.com.ui.screens.MoreScreen
import gegeparty.guigol.com.ui.screens.NotesScreen
import gegeparty.guigol.com.ui.screens.PlacesScreen
import gegeparty.guigol.com.ui.screens.SettingsScreen
import gegeparty.guigol.com.ui.screens.TasksScreen
import gegeparty.guigol.com.ui.theme.Gege

private data class Tab(val route: String, val label: String, val icon: ImageVector, val selectedIcon: ImageVector)

private val tabs = listOf(
    Tab("home", "Início", Icons.Outlined.Home, Icons.Filled.Home),
    Tab("tasks", "Tarefas", Icons.Outlined.Checklist, Icons.Filled.Checklist),
    Tab("agenda", "Agenda", Icons.Outlined.CalendarMonth, Icons.Filled.CalendarMonth),
    Tab("places", "Lugares", Icons.Outlined.Place, Icons.Filled.Place),
    Tab("more", "Mais", Icons.Outlined.GridView, Icons.Filled.GridView),
)
private val tabRoutes = tabs.map { it.route }.toSet()

fun NavHostController.go(route: String) {
    if (route in tabRoutes) {
        navigate(route) {
            popUpTo(graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    } else navigate(route) { launchSingleTop = true }
}

@Composable
fun AppScaffold(vm: MainViewModel, startRoute: String?, onStartConsumed: () -> Unit) {
    val nav = rememberNavController()
    val entry by nav.currentBackStackEntryAsState()
    val current = entry?.destination?.route

    LaunchedEffect(startRoute) {
        if (startRoute != null) { nav.go(startRoute); onStartConsumed() }
    }

    Scaffold(
        containerColor = Gege.Bg,
        bottomBar = {
            NavigationBar(containerColor = Color.White, tonalElevation = 0.dp) {
                tabs.forEach { t ->
                    val selected = current == t.route ||
                        (t.route == "more" && current != null && current !in tabRoutes)
                    NavigationBarItem(
                        selected = selected,
                        onClick = { nav.go(t.route) },
                        icon = { Icon(if (selected) t.selectedIcon else t.icon, t.label) },
                        label = { Text(t.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Gege.Pink,
                            selectedTextColor = Gege.Pink,
                            indicatorColor = Gege.PinkSoft,
                        ),
                    )
                }
            }
        },
    ) { pad ->
        NavHost(
            nav, startDestination = "home", modifier = Modifier.padding(pad),
            enterTransition = { fadeIn() + slideInHorizontally { it / 12 } },
            exitTransition = { fadeOut() },
            popEnterTransition = { fadeIn() },
            popExitTransition = { fadeOut() + slideOutHorizontally { it / 12 } },
        ) {
            val back: () -> Unit = { nav.popBackStack() }
            val open: (String) -> Unit = { nav.go(it) }
            composable("home") { HomeScreen(vm, open) }
            composable("tasks") { TasksScreen(vm) }
            composable("agenda") { AgendaScreen(vm) }
            composable("places") { PlacesScreen(vm) }
            composable("more") { MoreScreen(vm, open) }
            composable("ideas") { IdeasScreen(vm, back) }
            composable("notes") { NotesScreen(vm, back) }
            composable("guests") { GuestsScreen(vm, back) }
            composable("budget") { BudgetScreen(vm, back) }
            composable("settings") { SettingsScreen(vm, back) }
        }
    }
}

