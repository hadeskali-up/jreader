package com.aliworld.jreader.source
import com.aliworld.jreader.core.model.*
data class SourceTag(val id:String,val name:String)
data class SourceFilter(val query:String="",val category:String?=null,val tagId:String?=null,val contentRating:String?=null,val limit:Int=50,val offset:Int=0)
interface SourceAdapter { val id:SourceId;val name:String;val supportsDownloads:Boolean;suspend fun tags():List<SourceTag> = emptyList();suspend fun browse(filter:SourceFilter):List<Manga>;suspend fun details(manga:Manga):Manga;suspend fun chapters(manga:Manga):List<Chapter>;suspend fun pages(chapter:Chapter):List<Page>;fun close() }
