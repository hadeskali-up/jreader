package com.aliworld.jreader.downloads
import com.aliworld.jreader.core.model.*
import com.aliworld.jreader.source.SourceAdapter
import com.aliworld.jreader.storage.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.Serializable
@Serializable enum class DownloadStatus { QUEUED, DOWNLOADING, COMPLETED, FAILED }
@Serializable data class DownloadRecord(val id:String,val manga:Manga,val chapter:Chapter,val status:DownloadStatus,val directory:String="",val pagePaths:List<String> = emptyList(),val error:String="")
expect object FileDownloader { suspend fun download(url:String,path:String):String;fun chapterDir(mangaId:String,chapterId:String):String }
class DownloadManager(private val store:JsonStore,private val sources:Map<SourceId,SourceAdapter>,private val scope:CoroutineScope){private val _queue=MutableStateFlow(store.data.value.downloads);val queue:StateFlow<List<DownloadRecord>> = _queue.asStateFlow();private var job:Job?=null
 fun enqueue(m:Manga,c:Chapter){require(m.source==SourceId.MANGADEX){"Downloads unsupported for ${m.source}"};val r=DownloadRecord("${m.source}:${c.id}",m,c,DownloadStatus.QUEUED);save(_queue.value.filterNot{it.id==r.id}+r);run()};fun retry(id:String){save(_queue.value.map{if(it.id==id)it.copy(status=DownloadStatus.QUEUED,error="")else it});run()};fun cancel(id:String){save(_queue.value.filterNot{it.id==id})};fun delete(id:String){_queue.value.firstOrNull{it.id==id}?.directory?.takeIf(String::isNotBlank)?.let{PlatformStorage.deleteTree(it)};cancel(id)};fun bytes(id:String)=_queue.value.firstOrNull{it.id==id}?.directory?.let{PlatformStorage.size(it)}?:0
 private fun run(){if(job?.isActive==true)return;job=scope.launch{while(true){val r=_queue.value.firstOrNull{it.status==DownloadStatus.QUEUED}?:break;save(_queue.value.map{if(it.id==r.id)it.copy(status=DownloadStatus.DOWNLOADING)else it});try{val pages=sources.getValue(r.manga.source).pages(r.chapter);val dir=FileDownloader.chapterDir(r.manga.id,r.chapter.id);val paths=pages.map{FileDownloader.download(it.remoteUrl,"$dir/${it.index.toString().padStart(4,'0')}.jpg")};save(_queue.value.map{if(it.id==r.id)it.copy(status=DownloadStatus.COMPLETED,directory=dir,pagePaths=paths)else it})}catch(e:Exception){save(_queue.value.map{if(it.id==r.id)it.copy(status=DownloadStatus.FAILED,error=e.message?:"Download failed")else it})}}}}
 private fun save(v:List<DownloadRecord>){_queue.value=v;store.downloads(v)}
}
