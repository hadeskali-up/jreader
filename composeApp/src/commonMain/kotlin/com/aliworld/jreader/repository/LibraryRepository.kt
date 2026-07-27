package com.aliworld.jreader.repository
import com.aliworld.jreader.core.model.*
import com.aliworld.jreader.storage.JsonStore
class LibraryRepository(private val store:JsonStore){val state=store.data;fun toggle(m:Manga)=store.toggle(m);fun saveProgress(m:Manga,c:Chapter,page:Int)=store.history(m,c,page);fun unread(m:Manga,chapters:List<Chapter>)=chapters.count{c->state.value.history.firstOrNull{it.manga.id==m.id}?.completedChapterIds?.contains(c.id)!=true}}
