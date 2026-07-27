package com.aliworld.jreader.repository
import com.aliworld.jreader.core.model.SourceId
import com.aliworld.jreader.downloads.DownloadManager
import com.aliworld.jreader.source.*
import com.aliworld.jreader.storage.JsonStore
import io.ktor.client.*
import kotlinx.coroutines.*
class AppContainer {val client=HttpClient{expectSuccess=true};val sources:Map<SourceId,SourceAdapter> = listOf(MangaDexAdapter(client),EhentaiAdapter(client)).associateBy{it.id};val store=JsonStore();val library=LibraryRepository(store);val scope=CoroutineScope(SupervisorJob()+Dispatchers.Default);val downloads=DownloadManager(store,sources,scope);fun close(){scope.cancel();client.close()}}
