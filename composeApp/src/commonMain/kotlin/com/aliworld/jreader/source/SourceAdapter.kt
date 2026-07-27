package com.aliworld.jreader.source
import com.aliworld.jreader.core.model.*
data class SourceFilter(val query:String="",val category:String?=null,val tagId:String?=null,val limit:Int=30)
interface SourceAdapter { val id:SourceId;val name:String;val supportsDownloads:Boolean;suspend fun browse(filter:SourceFilter):List<Manga>;suspend fun details(manga:Manga):Manga;suspend fun chapters(manga:Manga):List<Chapter>;suspend fun pages(chapter:Chapter):List<Page>;fun close() }
