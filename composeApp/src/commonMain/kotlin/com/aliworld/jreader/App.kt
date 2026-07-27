package com.aliworld.jreader

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.aliworld.jreader.core.model.SourceId
import com.aliworld.jreader.downloads.DownloadStatus
import com.aliworld.jreader.navigation.Navigator
import com.aliworld.jreader.navigation.Route
import com.aliworld.jreader.navigation.SystemBackHandler
import com.aliworld.jreader.reader.ReaderScreen
import com.aliworld.jreader.repository.AppContainer
import com.aliworld.jreader.screens.BrowseScreen
import com.aliworld.jreader.screens.ContinueScreen
import com.aliworld.jreader.screens.DetailScreen
import com.aliworld.jreader.screens.DownloadScreen
import com.aliworld.jreader.screens.LibraryScreen
import com.aliworld.jreader.screens.SettingsScreen

@Composable
fun App(container: AppContainer) {
    val navigator = remember { Navigator() }
    val stack by navigator.routes.collectAsState()
    var tab by remember { mutableIntStateOf(0) }
    SystemBackHandler(enabled = stack.size > 1) { navigator.back() }

    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Color(0xFFC6A969),
            background = Color(0xFF0E1012),
            surface = Color(0xFF171A1D),
            surfaceVariant = Color(0xFF22262A),
            onBackground = Color(0xFFE8E3DA),
            onSurface = Color(0xFFE8E3DA),
        ),
    ) {
        Surface(modifier = Modifier.fillMaxSize()) {
            when (val route = stack.last()) {
                Route.Root -> RootScreen(tab = tab, setTab = { tab = it }, container = container, navigator = navigator)
                is Route.Detail -> DetailScreen(
                    m0 = route.manga,
                    c = container,
                    back = { navigator.back() },
                    read = { manga, chapter -> navigator.push(Route.Reader(manga, chapter)) },
                )
                is Route.Reader -> {
                    val local = container.downloads.queue.value.firstOrNull {
                        it.chapter.id == route.chapter.id && it.status == DownloadStatus.COMPLETED
                    }?.pagePaths.orEmpty()
                    ReaderScreen(
                        manga = route.manga,
                        chapter = route.chapter,
                        source = container.sources.getValue(route.manga.source),
                        store = container.store,
                        offline = local,
                        back = { navigator.back() },
                    )
                }
            }
        }
    }
}

@Composable
private fun RootScreen(tab: Int, setTab: (Int) -> Unit, container: AppContainer, navigator: Navigator) {
    val names = listOf("E-Hentai", "MangaDex", "Library", "Continue", "Downloads", "Settings")
    val icons = listOf("E", "M", "★", "◷", "↓", "⚙")
    Scaffold(
        bottomBar = {
            NavigationBar {
                names.forEachIndexed { index, name ->
                    NavigationBarItem(
                        selected = tab == index,
                        onClick = { setTab(index) },
                        icon = { Text(icons[index]) },
                        label = { Text(name) },
                    )
                }
            }
        },
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (tab) {
                0 -> BrowseScreen(container.sources.getValue(SourceId.EHENTAI)) { navigator.push(Route.Detail(it)) }
                1 -> BrowseScreen(container.sources.getValue(SourceId.MANGADEX)) { navigator.push(Route.Detail(it)) }
                2 -> LibraryScreen(container) { navigator.push(Route.Detail(it)) }
                3 -> ContinueScreen(container) { navigator.push(Route.Detail(it)) }
                4 -> DownloadScreen(container)
                else -> SettingsScreen(container)
            }
        }
    }
}
