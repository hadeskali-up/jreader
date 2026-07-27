package com.aliworld.jreader

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.*

private const val MD_API="https://api.mangadex.org"
data class MdManga(val id:String,val title:String,val cover:String,val description:String="",val status:String="",val year:String="",val contentRating:String="",val tags:List<String> = emptyList())
data class MdChapter(val id:String,val mangaId:String,val number:String,val title:String,val group:String)

class MangaDexRepo {
 private val client=HttpClient { expectSuccess=true }
 private val json=Json { ignoreUnknownKeys=true }
 private suspend fun get(path:String,params:ParametersBuilder.()->Unit={}):JsonObject {
  val text=client.get("$MD_API$path") { header(HttpHeaders.UserAgent,"JReader/1.0 (MangaDex client)");url { parameters.apply(params) } }.bodyAsText()
  return json.parseToJsonElement(text).jsonObject
 }
 suspend fun manga(query:String=""):List<MdManga>{
  val root=get("/manga") { append("originalLanguage[]","ko");append("includes[]","cover_art");append("order[followedCount]","desc");listOf("safe","suggestive","erotica","pornographic").forEach{append("contentRating[]",it)};if(query.isNotBlank())append("title",query) }
  return root["data"]?.jsonArray.orEmpty().map { parseManga(it.jsonObject) }
 }
 suspend fun detail(id:String)=parseManga(get("/manga/$id") { append("includes[]","cover_art") }["data"]!!.jsonObject)
 suspend fun chapters(id:String):List<MdChapter> = get("/manga/$id/feed") { append("translatedLanguage[]","en");append("order[chapter]","desc");append("includes[]","scanlation_group");listOf("safe","suggestive","erotica","pornographic").forEach{append("contentRating[]",it)} }["data"]?.jsonArray.orEmpty().map { e ->
  val o=e.jsonObject;val a=o["attributes"]!!.jsonObject;val group=o["relationships"]!!.jsonArray.firstOrNull{it.jsonObject["type"]?.jsonPrimitive?.content=="scanlation_group"}?.jsonObject?.get("attributes")?.jsonObject?.get("name")?.jsonPrimitive?.contentOrNull ?: "Unknown scanlation group"
  MdChapter(o["id"]!!.jsonPrimitive.content,id,a["chapter"]?.jsonPrimitive?.contentOrNull?:"?",a["title"]?.jsonPrimitive?.contentOrNull?:"",group)
 }
 suspend fun pages(chapterId:String):List<String>{val r=get("/at-home/server/$chapterId");val base=r["baseUrl"]!!.jsonPrimitive.content;val c=r["chapter"]!!.jsonObject;val hash=c["hash"]!!.jsonPrimitive.content;return c["dataSaver"]!!.jsonArray.map{"$base/data-saver/$hash/${it.jsonPrimitive.content}"}}
 private fun parseManga(o:JsonObject):MdManga { val a=o["attributes"]!!.jsonObject;val id=o["id"]!!.jsonPrimitive.content;val title=localized(a["title"]?.jsonObject,"Untitled");val desc=localized(a["description"]?.jsonObject);val file=o["relationships"]?.jsonArray?.firstOrNull{it.jsonObject["type"]?.jsonPrimitive?.content=="cover_art"}?.jsonObject?.get("attributes")?.jsonObject?.get("fileName")?.jsonPrimitive?.contentOrNull;return MdManga(id,title,if(file==null)"" else "https://uploads.mangadex.org/covers/$id/$file.256.jpg",desc,a["status"]?.jsonPrimitive?.contentOrNull?:"Unknown",a["year"]?.jsonPrimitive?.contentOrNull?:"Unknown",a["contentRating"]?.jsonPrimitive?.contentOrNull?:"Unknown",a["tags"]?.jsonArray.orEmpty().mapNotNull{localized(it.jsonObject["attributes"]?.jsonObject?.get("name")?.jsonObject).takeIf(String::isNotBlank)}) }
 private fun localized(o:JsonObject?,fallback:String=""):String=o?.get("en")?.jsonPrimitive?.contentOrNull?:o?.values?.firstOrNull()?.jsonPrimitive?.contentOrNull?:fallback
}

@Composable fun ManhwaBrowse(open:(String)->Unit){val repo=remember{MangaDexRepo()};var q by remember{mutableStateOf("")};var tick by remember{mutableIntStateOf(0)};var loading by remember{mutableStateOf(true)};var error by remember{mutableStateOf<String?>(null)};var data by remember{mutableStateOf(emptyList<MdManga>())};LaunchedEffect(tick){loading=true;error=null;try{data=repo.manga(q.trim())}catch(e:Exception){error=e.message?:"MangaDex request failed"};loading=false};Column(Modifier.fillMaxSize().padding(16.dp)){Text("Manhwa",style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.Bold);Text("Powered by MangaDex",color=MaterialTheme.colorScheme.primary);Row{OutlinedTextField(q,{q=it},Modifier.weight(1f),singleLine=true,label={Text("Search Korean-origin titles")});Button({tick++},Modifier.padding(start=8.dp)){Text("Go")}};Spacer(Modifier.height(12.dp));when{loading->MdState("Loading MangaDex…");error!=null->MdState(error!!,"Retry"){tick++};data.isEmpty()->MdState("No manhwa found");else->LazyVerticalGrid(GridCells.Adaptive(155.dp),verticalArrangement=Arrangement.spacedBy(12.dp),horizontalArrangement=Arrangement.spacedBy(12.dp)){items(data,key={it.id}){m->ElevatedCard({open(m.id)}){AsyncImage(m.cover,m.title,Modifier.fillMaxWidth().height(210.dp),contentScale=ContentScale.Crop);Text(m.title,Modifier.padding(10.dp),fontWeight=FontWeight.SemiBold)}}}}}}

@Composable fun ManhwaDetailScreen(id:String,back:()->Unit,read:(MdChapter)->Unit){val repo=remember{MangaDexRepo()};var tick by remember{mutableIntStateOf(0)};var manga by remember{mutableStateOf<MdManga?>(null)};var chapters by remember{mutableStateOf(emptyList<MdChapter>())};var error by remember{mutableStateOf<String?>(null)};LaunchedEffect(tick){error=null;try{manga=repo.detail(id);chapters=repo.chapters(id)}catch(e:Exception){error=e.message?:"MangaDex request failed"}};Column(Modifier.fillMaxSize().padding(16.dp)){TextButton(back){Text("‹ Back")};Text("Data provided by MangaDex",color=MaterialTheme.colorScheme.primary);when{error!=null->MdState(error!!,"Retry"){tick++};manga==null->MdState("Loading details…");else->LazyColumn(verticalArrangement=Arrangement.spacedBy(8.dp)){val m=manga!!;item{AsyncImage(m.cover,m.title,Modifier.fillMaxWidth().height(280.dp),contentScale=ContentScale.Fit);Text(m.title,style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold);Text("${m.status} · ${m.year}");Text("Content rating: ${m.contentRating.replaceFirstChar{it.uppercase()}}",color=MaterialTheme.colorScheme.primary);if(m.tags.isNotEmpty())Text(m.tags.joinToString(" · "),color=MaterialTheme.colorScheme.primary);if(m.description.isNotBlank())Text(m.description,Modifier.padding(vertical=10.dp));Text("English chapters",fontWeight=FontWeight.Bold)};if(chapters.isEmpty())item{Text("No English chapters found")};items(chapters,key={it.id}){c->ElevatedCard({read(c)},Modifier.fillMaxWidth()){Column(Modifier.padding(12.dp)){Text("Chapter ${c.number}${if(c.title.isBlank())"" else " · ${c.title}"}");Text("Scanlation: ${c.group}",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.primary)}}}}}}}

@Composable fun ManhwaReaderScreen(chapter:MdChapter,back:()->Unit){val repo=remember{MangaDexRepo()};var tick by remember{mutableIntStateOf(0)};var pages by remember{mutableStateOf<List<String>?>(null)};var error by remember{mutableStateOf<String?>(null)};LaunchedEffect(tick){error=null;try{pages=repo.pages(chapter.id)}catch(e:Exception){error=e.message?:"Chapter request failed"}};when{error!=null->Column{TextButton(back){Text("‹ Close")};MdState(error!!,"Retry"){tick++}};pages==null->MdState("Loading chapter…");pages!!.isEmpty()->Column{TextButton(back){Text("‹ Close")};MdState("No pages available")};else->LazyColumn(Modifier.fillMaxSize()){item{Column(Modifier.padding(12.dp)){TextButton(back){Text("‹ Close")};Text("MangaDex · Scanlation: ${chapter.group}",color=MaterialTheme.colorScheme.primary)}};itemsIndexed(pages!!){i,url->AsyncImage(url,"Page ${i+1}",Modifier.fillMaxWidth(),contentScale=ContentScale.FillWidth)}}}}

@Composable private fun MdState(text:String,action:String?=null,onClick:()->Unit={})=Box(Modifier.fillMaxSize(),contentAlignment=Alignment.Center){Column(horizontalAlignment=Alignment.CenterHorizontally){Text(text);if(action!=null)TextButton(onClick){Text(action)}}}
