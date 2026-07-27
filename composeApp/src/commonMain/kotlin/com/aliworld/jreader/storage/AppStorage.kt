package com.aliworld.jreader.storage
import com.aliworld.jreader.core.model.*
import com.aliworld.jreader.downloads.DownloadRecord
import kotlinx.serialization.Serializable

expect object PlatformStorage { fun init(context:Any?=null);fun read(key:String):String;fun write(key:String,value:String);fun backupPath():String;fun writePrivate(path:String,bytes:ByteArray);fun readPrivate(path:String):ByteArray?;fun deleteTree(path:String):Boolean;fun size(path:String):Long;fun now():Long }
@Serializable data class Snapshot(val schema:Int=2,val library:List<LibraryEntry> = emptyList(),val history:List<HistoryEntry> = emptyList(),val categories:List<Category> = emptyList(),val downloads:List<DownloadRecord> = emptyList(),val prefs:ReaderPrefs=ReaderPrefs())
