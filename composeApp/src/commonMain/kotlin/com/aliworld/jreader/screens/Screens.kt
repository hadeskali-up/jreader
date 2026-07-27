package com.aliworld.jreader.screens
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.aliworld.jreader.core.model.*
import com.aliworld.jreader.core.state.UiState
import com.aliworld.jreader.repository.AppContainer
import com.aliworld.jreader.source.*
import kotlinx.coroutines.launch

data class EhCategory(val name:String,val bit:Int)
val EH_CATEGORIES=listOf(EhCategory("Doujinshi",2),EhCategory("Manga",4),EhCategory("Artist CG",8),EhCategory("Game CG",16),EhCategory("Western",512),EhCategory("Non-H",256),EhCategory("Image Set",32),EhCategory("Cosplay",64),EhCategory("Asian Porn",128),EhCategory("Misc",1))
@Composable fun BrowseScreen(source:SourceAdapter,open:(Manga)->Unit){var q by remember{mutableStateOf("")};var category by remember{mutableStateOf<String?>(null)};var tick by remember{mutableIntStateOf(0)};var state by remember{mutableStateOf<UiState<List<Manga>>>(UiState.Loading)};LaunchedEffect(tick,source.id){state=UiState.Loading;state=try{source.browse(SourceFilter(q.trim(),category,limit=30)).let{if(it.isEmpty())UiState.Empty("No results")else UiState.Data(it)}}catch(e:Exception){UiState.Error(e.message?:"Request failed")}};Column(Modifier.fillMaxSize().padding(horizontal=14.dp)){Text(source.name,style=MaterialTheme.typography.headlineLarge,fontWeight=FontWeight.Bold);if(source.id==SourceId.MANGADEX)Text("Data provided by MangaDex",color=MaterialTheme.colorScheme.primary);Row{OutlinedTextField(q,{q=it},Modifier.weight(1f),label={Text("Search")},singleLine=true);Button({tick++},Modifier.padding(start=8.dp)){Text("Go")}};if(source.id==SourceId.EHENTAI)LazyRow{item{FilterChip(category==null,{category=null;tick++},{Text("All")})};items(EH_CATEGORIES){c->Spacer(Modifier.width(6.dp));FilterChip(category==c.bit.toString(),{category=c.bit.toString();tick++},{Text(c.name)})}};StateContent(state,{tick++}){MangaGrid(it,open)}}}
@Composable fun MangaGrid(items:List<Manga>,open:(Manga)->Unit){LazyVerticalGrid(GridCells.Adaptive(150.dp),verticalArrangement=Arrangement.spacedBy(12.dp),horizontalArrangement=Arrangement.spacedBy(12.dp)){items(items,key={"${it.source}:${it.id}"}){m->val interaction=remember{androidx.compose.foundation.interaction.MutableInteractionSource()};val pressed by interaction.collectIsPressedAsState();val scale by animateFloatAsState(if(pressed).97f else 1f,spring(),label="press");ElevatedCard({open(m)},Modifier.graphicsLayer{scaleX=scale;scaleY=scale},interactionSource=interaction){AsyncImage(m.coverUrl,m.title,Modifier.fillMaxWidth().height(210.dp),contentScale=ContentScale.Crop);Text(m.title,Modifier.padding(10.dp),fontWeight=FontWeight.SemiBold,maxLines=3)}}}}
@Composable fun DetailScreen(m0:Manga,c:AppContainer,back:()->Unit,read:(Manga,Chapter)->Unit){val source=c.sources.getValue(m0.source);var state by remember{mutableStateOf<UiState<Pair<Manga,List<Chapter>>>>(UiState.Loading)};var tick by remember{mutableIntStateOf(0)};val snapshot by c.store.data.collectAsState();LaunchedEffect(tick){state=try{val m=source.details(m0);UiState.Data(m to source.chapters(m))}catch(e:Exception){UiState.Error(e.message?:"Detail failed")}};Column(Modifier.fillMaxSize().padding(14.dp)){TextButton(back){Text("Back")};StateContent(state,{tick++}){(m,chapters)->LazyColumn(verticalArrangement=Arrangement.spacedBy(8.dp)){item{AsyncImage(m.coverUrl,m.title,Modifier.fillMaxWidth().height(260.dp),contentScale=ContentScale.Fit);Text(m.title,style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold);Text(listOf(m.status,m.year,m.contentRating).filter{it.isNotBlank()}.joinToString(" · "));Text(m.description);Button({c.library.toggle(m)}){Text(if(snapshot.library.any{it.manga.id==m.id&&it.manga.source==m.source})"Remove from library" else "Add to library")}};items(chapters,key={it.id}){ch->ElevatedCard({read(m,ch)},Modifier.fillMaxWidth()){Row(Modifier.padding(12.dp),verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f)){Text("Chapter ${ch.number} ${ch.title}");Text("Scanlation: ${ch.scanlationGroup}",color=MaterialTheme.colorScheme.primary)};if(source.supportsDownloads)TextButton({c.downloads.enqueue(m,ch)}){Text("Download")}}}}}}}}
@Composable fun LibraryScreen(c:AppContainer,open:(Manga)->Unit){val s by c.store.data.collectAsState();Column(Modifier.fillMaxSize().padding(14.dp)){Text("Library",style=MaterialTheme.typography.headlineLarge);if(s.library.isEmpty())EmptyState("Library empty")else MangaGrid(s.library.map{it.manga},open)}}
@Composable fun ContinueScreen(c:AppContainer,open:(Manga)->Unit){val s by c.store.data.collectAsState();Column(Modifier.fillMaxSize().padding(14.dp)){Text("Continue reading",style=MaterialTheme.typography.headlineLarge);if(s.history.isEmpty())EmptyState("No reading history")else MangaGrid(s.history.sortedByDescending{it.readAt}.map{it.manga},open)}}
@Composable fun DownloadScreen(c:AppContainer){val q by c.downloads.queue.collectAsState();Column(Modifier.padding(14.dp)){Text("Downloads",style=MaterialTheme.typography.headlineLarge);Text("Foreground-only queue",color=MaterialTheme.colorScheme.primary);if(q.isEmpty())EmptyState("No downloads")else LazyColumn{items(q,key={it.id}){d->ListItem(headlineContent={Text("${d.manga.title} · ${d.chapter.number}")},supportingContent={Text("${d.status}${if(d.error.isBlank())"" else " · ${d.error}"}")},trailingContent={Row{if(d.status.name=="FAILED")TextButton({c.downloads.retry(d.id)}){Text("Retry")};TextButton({c.downloads.delete(d.id)}){Text("Delete")}}})}}}}
@Composable fun SettingsScreen(c:AppContainer){var name by remember{mutableStateOf("")};val s by c.store.data.collectAsState();Column(Modifier.padding(14.dp)){Text("Settings & categories",style=MaterialTheme.typography.headlineLarge);Row{OutlinedTextField(name,{name=it},Modifier.weight(1f),label={Text("New category")});Button({if(name.isNotBlank()){c.store.category(name.trim());name=""}}){Text("Add")}};LazyColumn(Modifier.weight(1f)){items(s.categories,key={it.id}){cat->CategoryRow(cat,{c.store.renameCategory(cat.id,it)},{c.store.deleteCategory(cat.id)})}};Button({c.store.backup()}){Text("Create private JSON backup")};OutlinedButton({c.store.restore()}){Text("Restore backup (overwrite)")}}}
@Composable private fun CategoryRow(cat:Category,rename:(String)->Unit,delete:()->Unit){var text by remember(cat.id){mutableStateOf(cat.name)};ListItem(headlineContent={OutlinedTextField(text,{text=it;rename(it)},singleLine=true)},supportingContent={Text("Category assignment available from library records")},trailingContent={TextButton(delete){Text("Delete")}})}
@Composable
fun <T> StateContent(s: UiState<T>, retry: () -> Unit, content: @Composable (T) -> Unit) {
    when (s) {
        UiState.Loading -> EmptyState("Loading…")
        is UiState.Empty -> EmptyState(s.message)
        is UiState.Error -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(s.message)
            Button(onClick = retry) { Text("Retry") }
        }
        is UiState.Data -> content(s.value)
    }
}
@Composable fun EmptyState(text:String)=Box(Modifier.fillMaxSize(),contentAlignment=Alignment.Center){Text(text)}
