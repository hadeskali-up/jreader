package com.aliworld.jreader.storage
import com.aliworld.jreader.core.model.*
import com.aliworld.jreader.downloads.DownloadRecord
import kotlinx.coroutines.flow.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
class JsonStore { private val json=Json{ignoreUnknownKeys=true;encodeDefaults=true};private val _data=MutableStateFlow(load());val data:StateFlow<Snapshot> = _data.asStateFlow()
 private fun load():Snapshot { val raw=PlatformStorage.read("snapshot_v2");if(raw.isNotBlank())return runCatching{json.decodeFromString<Snapshot>(raw)}.getOrElse{Snapshot()};return migrateLegacy() }
 private fun migrateLegacy(): Snapshot {
     fun records(key: String): List<Manga> = PlatformStorage.read(key)
         .split('\u001d')
         .mapNotNull { row ->
             val parts = row.split('\u001e')
             if (parts.size < 3) null else Manga(
                 id = parts[0],
                 source = SourceId.EHENTAI,
                 title = parts[1],
                 coverUrl = parts[2],
                 category = parts.getOrElse(3) { "" },
                 uploader = parts.getOrElse(4) { "" },
                 pageCount = parts.getOrElse(5) { "" },
             )
         }
     val snapshot = Snapshot(
         library = records("favorites").map { LibraryEntry(it) },
         history = records("history").map { HistoryEntry(it) },
     )
     persist(snapshot)
     return snapshot
 }
 private fun persist(v:Snapshot){PlatformStorage.write("snapshot_v2",json.encodeToString(v));_data.value=v};fun update(block:(Snapshot)->Snapshot)=persist(block(_data.value));fun toggle(m:Manga)=update{s->s.copy(library=if(s.library.any{it.manga.id==m.id&&it.manga.source==m.source})s.library.filterNot{it.manga.id==m.id&&it.manga.source==m.source}else listOf(LibraryEntry(m,addedAt=currentTime()))+s.library)}
 fun history(m:Manga,c:Chapter,page:Int)=update{s->s.copy(history=listOf(HistoryEntry(m,c,page,currentTime()))+s.history.filterNot{it.manga.id==m.id&&it.manga.source==m.source}.take(99))};fun category(name:String)=update{s->s.copy(categories=s.categories+Category("cat_${currentTime()}",name,s.categories.size))};fun renameCategory(id:String,name:String)=update{s->s.copy(categories=s.categories.map{if(it.id==id)it.copy(name=name)else it})};fun deleteCategory(id:String)=update{s->s.copy(categories=s.categories.filterNot{it.id==id},library=s.library.map{it.copy(categoryIds=it.categoryIds-id)})};fun assign(m:Manga,id:String)=update{s->s.copy(library=s.library.map{if(it.manga.id==m.id&&it.manga.source==m.source)it.copy(categoryIds=it.categoryIds+id)else it})};fun downloads(v:List<DownloadRecord>)=update{it.copy(downloads=v)};fun prefs(v:ReaderPrefs)=update{it.copy(prefs=v)}
 fun backup():String=PlatformStorage.backupPath().also{PlatformStorage.writePrivate(it,json.encodeToString(_data.value).encodeToByteArray())};fun restore():Boolean{val b=PlatformStorage.readPrivate(PlatformStorage.backupPath())?:return false;val s=runCatching{json.decodeFromString<Snapshot>(b.decodeToString())}.getOrNull()?:return false;if(s.schema>2)return false;persist(s);return true};private fun currentTime()=PlatformStorage.now()
}
