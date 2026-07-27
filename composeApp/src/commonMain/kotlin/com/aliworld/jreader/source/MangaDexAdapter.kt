package com.aliworld.jreader.source
import com.aliworld.jreader.core.model.*
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.*
class MangaDexAdapter(private val client:HttpClient):SourceAdapter {override val id=SourceId.MANGADEX;override val name="MangaDex";override val supportsDownloads=true;private val json=Json{ignoreUnknownKeys=true};private suspend fun get(path:String,p:ParametersBuilder.()->Unit={}):JsonObject=json.parseToJsonElement(client.get("https://api.mangadex.org$path"){header(HttpHeaders.UserAgent,"JReader/2.0 (MangaDex client)");url{parameters.apply(p)}}.bodyAsText()).jsonObject
 override suspend fun tags():List<SourceTag> = get("/manga/tag")["data"]?.jsonArray.orEmpty().mapNotNull { element ->
  val item=element.jsonObject;val attributes=item["attributes"]?.jsonObject?:return@mapNotNull null
  if(attributes["group"]?.jsonPrimitive?.contentOrNull!="genre")return@mapNotNull null
  SourceTag(item["id"]!!.jsonPrimitive.content,localized(attributes["name"]?.jsonObject)).takeIf{it.name.isNotBlank()}
 }.sortedBy{it.name}
 fun query(filter:SourceFilter)=buildList{add("limit=${filter.limit}");add("originalLanguage[]=ko");add("includes[]=cover_art");val ratings=filter.contentRating?.let(::listOf)?:listOf("safe","suggestive","erotica","pornographic");ratings.forEach{add("contentRating[]=$it")};if(filter.query.isNotBlank())add("title=${filter.query}");filter.tagId?.let{add("includedTags[]=$it")}}
 override suspend fun browse(filter:SourceFilter)=get("/manga"){query(filter).forEach{val(a,v)=it.split('=',limit=2);append(a,v)}}["data"]?.jsonArray.orEmpty().map{manga(it.jsonObject)}
 override suspend fun details(m:Manga)=manga(get("/manga/${m.id}"){append("includes[]","cover_art")}["data"]!!.jsonObject)
 override suspend fun chapters(m:Manga)=get("/manga/${m.id}/feed"){append("translatedLanguage[]","en");append("order[chapter]","desc");append("includes[]","scanlation_group")}["data"]?.jsonArray.orEmpty().map{o0->val o=o0.jsonObject;val a=o["attributes"]!!.jsonObject;val g=o["relationships"]!!.jsonArray.firstOrNull{it.jsonObject["type"]?.jsonPrimitive?.content=="scanlation_group"}?.jsonObject?.get("attributes")?.jsonObject?.get("name")?.jsonPrimitive?.contentOrNull?:"Unknown scanlation group";Chapter(o["id"]!!.jsonPrimitive.content,m.id,id,a["chapter"]?.jsonPrimitive?.contentOrNull?:"?",a["title"]?.jsonPrimitive?.contentOrNull?:"",g)}
 override suspend fun pages(c:Chapter):List<Page>{val r=get("/at-home/server/${c.id}");val base=r["baseUrl"]!!.jsonPrimitive.content;val x=r["chapter"]!!.jsonObject;val hash=x["hash"]!!.jsonPrimitive.content;return x["dataSaver"]!!.jsonArray.mapIndexed{i,n->Page(i,"$base/data-saver/$hash/${n.jsonPrimitive.content}")}}
 private fun manga(o:JsonObject):Manga{val a=o["attributes"]!!.jsonObject;val mid=o["id"]!!.jsonPrimitive.content;val file=o["relationships"]?.jsonArray?.firstOrNull{it.jsonObject["type"]?.jsonPrimitive?.content=="cover_art"}?.jsonObject?.get("attributes")?.jsonObject?.get("fileName")?.jsonPrimitive?.contentOrNull;return Manga(mid,id,localized(a["title"]?.jsonObject,"Untitled"),file?.let{"https://uploads.mangadex.org/covers/$mid/$it.256.jpg"}.orEmpty(),localized(a["description"]?.jsonObject),a["status"]?.jsonPrimitive?.contentOrNull.orEmpty(),a["year"]?.jsonPrimitive?.contentOrNull.orEmpty(),a["contentRating"]?.jsonPrimitive?.contentOrNull.orEmpty(),a["tags"]?.jsonArray.orEmpty().mapNotNull{localized(it.jsonObject["attributes"]?.jsonObject?.get("name")?.jsonObject).takeIf(String::isNotBlank)})};private fun localized(o:JsonObject?,fallback:String="")=o?.get("en")?.jsonPrimitive?.contentOrNull?:o?.values?.firstOrNull()?.jsonPrimitive?.contentOrNull?:fallback;override fun close(){}
}
