package com.aliworld.jreader.source
import com.aliworld.jreader.core.model.*
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
class EhentaiAdapter(private val client:HttpClient):SourceAdapter {override val id=SourceId.EHENTAI;override val name="E-Hentai";override val supportsDownloads=false
 override suspend fun browse(filter:SourceFilter):List<Manga>{val out=linkedMapOf<String,Manga>();var cursor:String?=null;repeat(8){val ps=buildList{if(filter.query.isNotBlank())add("f_search=${encode(filter.query)}");filter.category?.toIntOrNull()?.let{add("f_cats=${1023 xor it}")};cursor?.let{add("next=$it")}}.joinToString("&");val html=client.get("https://e-hentai.org/${if(ps.isBlank())"" else "?$ps"}"){header("User-Agent","Mozilla/5.0 JReader/2.0")}.bodyAsText();val got=HtmlParser.feed(html);got.forEach{out.putIfAbsent(it.id,it)};cursor=HtmlParser.next(html);if(out.size>=filter.limit||got.isEmpty()||cursor==null)return out.values.take(filter.limit)};return out.values.take(filter.limit)}
 override suspend fun details(manga:Manga)=HtmlParser.detail(client.get(manga.id).bodyAsText(),manga.id).first
 override suspend fun chapters(manga:Manga):List<Chapter>{val (_,links)=HtmlParser.detail(client.get(manga.id).bodyAsText(),manga.id);return listOf(Chapter(manga.id,manga.id,id,"Gallery",manga.title,pageLinks=links))}
 override suspend fun pages(chapter:Chapter)=chapter.pageLinks.mapIndexed{i,u->Page(i,HtmlParser.image(client.get(u).bodyAsText()).orEmpty())};override fun close(){};private fun encode(s:String)=s.encodeToByteArray().joinToString(""){if(it.toInt().toChar().isLetterOrDigit())it.toInt().toChar().toString() else "%${it.toUByte().toString(16).uppercase().padStart(2,'0')}"}}
