package com.aliworld.jreader.core.model

import kotlinx.serialization.Serializable

@Serializable enum class SourceId { MANGADEX, EHENTAI }
@Serializable data class Manga(val id:String,val source:SourceId,val title:String,val coverUrl:String="",val description:String="",val status:String="",val year:String="",val contentRating:String="",val tags:List<String> = emptyList(),val category:String="",val uploader:String="",val pageCount:String="")
@Serializable data class Chapter(val id:String,val mangaId:String,val source:SourceId,val number:String="",val title:String="",val scanlationGroup:String="",val pageLinks:List<String> = emptyList())
@Serializable data class Page(val index:Int,val remoteUrl:String="",val localPath:String?=null)
@Serializable data class Category(val id:String,val name:String,val order:Int)
@Serializable data class LibraryEntry(val manga:Manga,val favorite:Boolean=true,val categoryIds:Set<String> = emptySet(),val addedAt:Long=0)
@Serializable data class HistoryEntry(val manga:Manga,val chapter:Chapter?=null,val page:Int=0,val readAt:Long=0,val completedChapterIds:Set<String> = emptySet())
@Serializable enum class ReaderMode { WEBTOON, PAGED_LTR, PAGED_RTL }
@Serializable enum class ReaderFit { WIDTH, CONTAIN, FILL }
@Serializable data class ReaderPrefs(val mode:ReaderMode=ReaderMode.WEBTOON,val fit:ReaderFit=ReaderFit.WIDTH)
