package com.aliworld.jreader.reader
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.aliworld.jreader.core.model.*
import com.aliworld.jreader.core.state.UiState
import com.aliworld.jreader.source.SourceAdapter
import com.aliworld.jreader.storage.JsonStore
@Composable fun ReaderScreen(manga:Manga,chapter:Chapter,source:SourceAdapter,store:JsonStore,offline:List<String> = emptyList(),back:()->Unit){var state by remember{mutableStateOf<UiState<List<Page>>>(UiState.Loading)};var prefs by remember{mutableStateOf(store.data.value.prefs)};val saved=store.data.value.history.firstOrNull{it.manga.id==manga.id}?.page?:0;LaunchedEffect(chapter.id){state=try{val p=if(offline.isNotEmpty())offline.mapIndexed{i,v->Page(i,localPath=v)}else source.pages(chapter);if(p.isEmpty())UiState.Empty("No pages") else UiState.Data(p)}catch(e:Exception){UiState.Error(e.message?:"Page request failed")}};Column(Modifier.fillMaxSize()){Row(Modifier.fillMaxWidth().padding(6.dp),verticalAlignment=Alignment.CenterVertically){TextButton(back){Text("Close")};Text("${source.name} · ${chapter.scanlationGroup}",Modifier.weight(1f),maxLines=1);ReaderMenu(prefs){prefs=it;store.prefs(it)}};when(val s=state){UiState.Loading->ReaderState("Loading chapter…");is UiState.Error->ReaderState(s.message);is UiState.Empty->ReaderState(s.message);is UiState.Data->Pages(s.value,prefs,saved){store.history(manga,chapter,it)}}}}
@Composable private fun Pages(pages:List<Page>,prefs:ReaderPrefs,start:Int,onPage:(Int)->Unit){val list=rememberLazyListState(if(prefs.mode==ReaderMode.PAGED_RTL)pages.lastIndex-start else start);LaunchedEffect(list.firstVisibleItemIndex){val i=if(prefs.mode==ReaderMode.PAGED_RTL)pages.lastIndex-list.firstVisibleItemIndex else list.firstVisibleItemIndex;onPage(i)};Box(Modifier.fillMaxSize()){LazyRowOrColumn(prefs.mode,list){val ordered=if(prefs.mode==ReaderMode.PAGED_RTL)pages.reversed()else pages;items(ordered,key={it.index}){p->ZoomPage(p,prefs.fit,prefs.mode==ReaderMode.WEBTOON)}};Text("${if(prefs.mode==ReaderMode.PAGED_RTL)pages.size-list.firstVisibleItemIndex else list.firstVisibleItemIndex+1} / ${pages.size}",Modifier.align(Alignment.BottomCenter).padding(12.dp),color=MaterialTheme.colorScheme.primary)}}
@Composable private fun LazyRowOrColumn(mode:ReaderMode,state:LazyListState,content:LazyListScope.()->Unit){if(mode==ReaderMode.WEBTOON)LazyColumn(Modifier.fillMaxSize(),state=state,content=content)else LazyRow(Modifier.fillMaxSize(),state=state,content=content)}
@Composable
private fun ZoomPage(p: Page, fit: ReaderFit, webtoon: Boolean) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    var retry by remember { mutableIntStateOf(0) }
    val source = p.localPath ?: p.remoteUrl
    key(retry) {
        AsyncImage(
            model = source,
            contentDescription = "Page ${p.index + 1}",
            modifier = Modifier
                .then(if (webtoon) Modifier.fillMaxWidth() else Modifier.fillMaxSize())
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offsetX
                    translationY = offsetY
                }
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(1f, 5f)
                        offsetX += pan.x
                        offsetY += pan.y
                    }
                },
            contentScale = when (fit) {
                ReaderFit.WIDTH -> ContentScale.FillWidth
                ReaderFit.CONTAIN -> ContentScale.Fit
                ReaderFit.FILL -> ContentScale.Crop
            },
            onError = { retry++ },
        )
    }
}
@Composable private fun ReaderMenu(p:ReaderPrefs,set:(ReaderPrefs)->Unit){var open by remember{mutableStateOf(false)};TextButton({open=true}){Text("Mode")};DropdownMenu(open,{open=false}){ReaderMode.entries.forEach{DropdownMenuItem({Text(it.name.replace('_',' '))},{set(p.copy(mode=it));open=false})};HorizontalDivider();ReaderFit.entries.forEach{DropdownMenuItem({Text("Fit ${it.name.lowercase()}")},{set(p.copy(fit=it));open=false})}}}
@Composable private fun ReaderState(s:String)=Box(Modifier.fillMaxSize(),contentAlignment=Alignment.Center){Text(s)}
