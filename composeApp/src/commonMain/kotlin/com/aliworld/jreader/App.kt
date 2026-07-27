package com.aliworld.jreader
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import com.aliworld.jreader.core.model.SourceId
import com.aliworld.jreader.navigation.*
import com.aliworld.jreader.reader.ReaderScreen
import com.aliworld.jreader.repository.AppContainer
import com.aliworld.jreader.screens.*
@Composable fun App(container:AppContainer){val nav=remember{Navigator()};val stack by nav.routes.collectAsState();var tab by remember{mutableIntStateOf(0)};SystemBackHandler(stack.size>1){nav.back()};MaterialTheme(darkColorScheme(primary=Color(0xFFC6A969),background=Color(0xFF0E1012),surface=Color(0xFF171A1D),surfaceVariant=Color(0xFF22262A),onBackground=Color(0xFFE8E3DA),onSurface=Color(0xFFE8E3DA))){Surface(Modifier.fillMaxSize()){when(val route=stack.last()){Route.Root->Scaffold(bottomBar={NavigationBar{listOf("E-Hentai","MangaDex","Library","Continue","Downloads","Settings").forEachIndexed{i,n->NavigationBarItem(tab==i,{tab=i},{Text(listOf("E","M","★","◷","↓","⚙")[i])},{Text(n)})}}}){p->Box(Modifier.padding(p)){when(tab){0->BrowseScreen(container.sources.getValue(SourceId.EHENTAI)){nav.push(Route.Detail(it))};1->BrowseScreen(container.sources.getValue(SourceId.MANGADEX)){nav.push(Route.Detail(it))};2->LibraryScreen(container){nav.push(Route.Detail(it))};3->ContinueScreen(container){nav.push(Route.Detail(it))};4->DownloadScreen(container);else->SettingsScreen(container)}}};is Route.Detail->DetailScreen(route.manga,container,{nav.back()}){m,c->nav.push(Route.Reader(m,c))};is Route.Reader->{val local=container.downloads.queue.value.firstOrNull{it.chapter.id==route.chapter.id&&it.status.name=="COMPLETED"}?.pagePaths.orEmpty();ReaderScreen(route.manga,route.chapter,container.sources.getValue(route.manga.source),container.store,local){nav.back()}}}}}}
