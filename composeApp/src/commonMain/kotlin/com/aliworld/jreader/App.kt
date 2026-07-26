package com.aliworld.jreader

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.launch

data class Gallery(val url:String,val title:String,val cover:String,val category:String="",val uploader:String="",val pages:String="")
expect object HtmlParser { fun feed(html:String):List<Gallery>; fun detail(html:String,url:String):Pair<Gallery,List<String>>; fun image(html:String):String? }
expect object Store { fun get(key:String):String; fun put(key:String,value:String) }

class Repo { private val client=HttpClient { expectSuccess=true }
 suspend fun feed(query:String):List<Gallery>{ val u=if(query.isBlank()) "https://e-hentai.org/" else "https://e-hentai.org/?f_search=${encode(query)}"; return HtmlParser.feed(client.get(u){header("User-Agent","Mozilla/5.0 JReader/1.0")}.bodyAsText()) }
 suspend fun detail(url:String)=HtmlParser.detail(client.get(url){header("User-Agent","Mozilla/5.0 JReader/1.0")}.bodyAsText(),url)
 suspend fun pageImage(url:String)=HtmlParser.image(client.get(url){header("User-Agent","Mozilla/5.0 JReader/1.0")}.bodyAsText())
 private fun encode(s:String)=s.encodeToByteArray().joinToString(""){ if(it.toInt().toChar().isLetterOrDigit()) it.toInt().toChar().toString() else "%${it.toUByte().toString(16).uppercase().padStart(2,'0')}" }
}

enum class Tab(val label:String){HOME("Browse"),FAVORITES("Favorites"),HISTORY("History"),DOWNLOADS("Downloads"),SETTINGS("Settings")}
sealed interface Page { data object Root:Page; data class Detail(val g:Gallery):Page; data class Reader(val title:String,val links:List<String>):Page }
private fun Gallery.pack()=listOf(url,title,cover,category,uploader,pages).joinToString("\u001e"){it.replace("\u001d","")}
private fun unpack(s:String)=s.split("\u001d").filter{it.isNotBlank()}.mapNotNull{r->r.split("\u001e").takeIf{it.size>=3}?.let{Gallery(it[0],it[1],it[2],it.getOrElse(3){""},it.getOrElse(4){""},it.getOrElse(5){""})}}
private fun saved(key:String)=unpack(Store.get(key)); private fun save(key:String,x:List<Gallery>)=Store.put(key,x.distinctBy{it.url}.joinToString("\u001d"){it.pack()})

@Composable fun App(){
 var theme by remember{mutableStateOf(Store.get("theme").ifBlank{"system"})}; val dark=theme=="dark"||(theme=="system"&&isSystemInDarkTheme())
 val colors=if(dark) darkColorScheme(background=Color(0xFF101214),surface=Color(0xFF191C1F),primary=Color(0xFF90CAF9)) else lightColorScheme(primary=Color(0xFF315F7D))
 MaterialTheme(colors){ Surface(Modifier.fillMaxSize()){ var page by remember{mutableStateOf<Page>(Page.Root)}; when(val p=page){Page.Root->Root{page=Page.Detail(it)};is Page.Detail->Detail(p.g,{page=Page.Root}){t,l->page=Page.Reader(t,l)};is Page.Reader->Reader(p.title,p.links){page=Page.Root}} } }
}
@Composable private fun Root(open:(Gallery)->Unit){
 var tab by remember{mutableStateOf(Tab.HOME)}
 Scaffold(bottomBar={NavigationBar{Tab.entries.forEach{NavigationBarItem(tab==it,{tab=it},icon={Text(when(it){Tab.HOME->"⌂";Tab.FAVORITES->"★";Tab.HISTORY->"◷";Tab.DOWNLOADS->"↓";Tab.SETTINGS->"⚙"})},label={Text(it.label)})}}}){pad->Box(Modifier.padding(pad)){when(tab){Tab.HOME->Feed(open);Tab.FAVORITES->SavedList("favorites","No favorites yet",open);Tab.HISTORY->SavedList("history","No reading history",open);Tab.DOWNLOADS->Downloads();Tab.SETTINGS->Settings()}}}
}
@Composable private fun Feed(open:(Gallery)->Unit){
 val repo=remember{Repo()}; var q by remember{mutableStateOf("")};var loading by remember{mutableStateOf(true)};var error by remember{mutableStateOf<String?>(null)};var items by remember{mutableStateOf(emptyList<Gallery>())};var tick by remember{mutableIntStateOf(0)}
 LaunchedEffect(tick){loading=true;error=null;try{items=repo.feed(q)}catch(e:Exception){error=e.message?:"Request failed"};loading=false}
 Column(Modifier.fillMaxSize().padding(16.dp)){Text("JReader",style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.Bold);Spacer(Modifier.height(10.dp));Row{OutlinedTextField(q,{q=it},Modifier.weight(1f),singleLine=true,label={Text("Search public galleries")});Button({tick++},Modifier.padding(start=8.dp)){Text("Go")}};Spacer(Modifier.height(12.dp));when{loading->State("Loading…");error!=null->State(error!!,"Retry"){tick++};items.isEmpty()->State("No galleries found");else->GalleryList(items,Store.get("layout")!="list",open)}}
}
@Composable private fun GalleryList(items:List<Gallery>,grid:Boolean,open:(Gallery)->Unit){if(grid) LazyVerticalGrid(GridCells.Adaptive(155.dp),verticalArrangement=Arrangement.spacedBy(12.dp),horizontalArrangement=Arrangement.spacedBy(12.dp)){items(items,key={it.url}){Card(it,open)}} else LazyColumn(verticalArrangement=Arrangement.spacedBy(10.dp)){items(items,key={it.url}){Card(it,open,true)}}}
@Composable private fun Card(g:Gallery,open:(Gallery)->Unit,row:Boolean=false){val source=remember{androidx.compose.foundation.interaction.MutableInteractionSource()};val down by source.collectIsPressedAsState();val scale by animateFloatAsState(if(down).97f else 1f,spring());ElevatedCard({open(g)},Modifier.fillMaxWidth().graphicsLayer{scaleX=scale;scaleY=scale},interactionSource=source){if(row)Row{Cover(g,Modifier.width(100.dp).height(140.dp));Info(g,Modifier.padding(12.dp).weight(1f))}else{Cover(g,Modifier.fillMaxWidth().height(210.dp));Info(g,Modifier.padding(10.dp))}}}
@Composable private fun Cover(g:Gallery,m:Modifier)=AsyncImage(g.cover,g.title,m,contentScale=ContentScale.Crop)
@Composable private fun Info(g:Gallery,m:Modifier){Column(m){Text(g.title,maxLines=3,fontWeight=FontWeight.SemiBold);if(g.category.isNotBlank())Text(g.category,color=MaterialTheme.colorScheme.primary,style=MaterialTheme.typography.labelMedium)}}
@Composable private fun Detail(g0:Gallery,back:()->Unit,read:(String,List<String>)->Unit){val repo=remember{Repo()};var value by remember{mutableStateOf<Pair<Gallery,List<String>>?>(null)};var error by remember{mutableStateOf<String?>(null)};var tick by remember{mutableIntStateOf(0)};LaunchedEffect(tick){try{value=repo.detail(g0.url);val h=listOf(g0)+saved("history");save("history",h.take(100))}catch(e:Exception){error=e.message?:"Failed"}};Column(Modifier.fillMaxSize().padding(16.dp)){TextButton(back){Text("‹ Back")};when{error!=null->State(error!!,"Retry"){tick++};value==null->State("Loading details…");else->{val(g,links)=value!!;Cover(g,Modifier.fillMaxWidth().height(280.dp));Text(g.title,style=MaterialTheme.typography.headlineSmall,modifier=Modifier.padding(vertical=12.dp));Text(listOf(g.category,g.uploader,g.pages).filter{it.isNotBlank()}.joinToString(" · "));Row(Modifier.padding(top=14.dp)){var fav by remember{mutableStateOf(saved("favorites").any{it.url==g.url})};Button({fav=!fav;save("favorites",if(fav)listOf(g)+saved("favorites") else saved("favorites").filter{it.url!=g.url})}){Text(if(fav)"★ Favorited" else "☆ Favorite")};Spacer(Modifier.width(8.dp));Button({read(g.title,links)},enabled=links.isNotEmpty()){Text("Read ${links.size} pages")}}}}}}
@Composable private fun Reader(title:String,links:List<String>,back:()->Unit){val repo=remember{Repo()};LazyColumn(Modifier.fillMaxSize()){item{Row(Modifier.padding(8.dp),verticalAlignment=Alignment.CenterVertically){TextButton(back){Text("‹ Close")};Text(title,maxLines=1)}};itemsIndexed(links){i,u->var image by remember{mutableStateOf<String?>(null)};var failed by remember{mutableStateOf(false)};LaunchedEffect(u){try{image=repo.pageImage(u)}catch(_:Exception){failed=true}};Box(Modifier.fillMaxWidth().heightIn(min=300.dp),contentAlignment=Alignment.Center){when{failed->Text("Page ${i+1} failed");image==null->CircularProgressIndicator();else->AsyncImage(image,"Page ${i+1}",Modifier.fillMaxWidth(),contentScale=ContentScale.FillWidth)}}}}}
@Composable private fun SavedList(key:String,empty:String,open:(Gallery)->Unit){var x by remember{mutableStateOf(saved(key))};Column(Modifier.fillMaxSize().padding(16.dp)){Text(if(key=="favorites")"Favorites" else "History",style=MaterialTheme.typography.headlineMedium);Spacer(Modifier.height(12.dp));if(x.isEmpty())State(empty)else GalleryList(x,false,open)}}
@Composable private fun Downloads(){Column(Modifier.fillMaxSize().padding(16.dp)){Text("Downloads",style=MaterialTheme.typography.headlineMedium);State("No downloads queued\nOffline downloading is not available in this MVP.")}}
@Composable private fun Settings(){var theme by remember{mutableStateOf(Store.get("theme").ifBlank{"system"})};var layout by remember{mutableStateOf(Store.get("layout").ifBlank{"grid"})};Column(Modifier.padding(16.dp)){Text("Settings",style=MaterialTheme.typography.headlineMedium);Text("Theme",Modifier.padding(top=20.dp));SingleChoiceSegmentedButtonRow{listOf("system","light","dark").forEachIndexed{i,v->SegmentedButton(theme==v,{theme=v;Store.put("theme",v)},SegmentedButtonDefaults.itemShape(i,3)){Text(v.replaceFirstChar{it.uppercase()})}}};Text("Gallery layout",Modifier.padding(top=20.dp));SingleChoiceSegmentedButtonRow{listOf("grid","list").forEachIndexed{i,v->SegmentedButton(layout==v,{layout=v;Store.put("layout",v)},SegmentedButtonDefaults.itemShape(i,2)){Text(v.replaceFirstChar{it.uppercase()})}}};Text("Theme changes apply after reopening screen/app.",style=MaterialTheme.typography.bodySmall,modifier=Modifier.padding(top=12.dp))}}
@Composable private fun State(text:String,action:String?=null,onClick:()->Unit={})=Box(Modifier.fillMaxSize(),contentAlignment=Alignment.Center){Column(horizontalAlignment=Alignment.CenterHorizontally){Text(text);if(action!=null)TextButton(onClick){Text(action)}}}
