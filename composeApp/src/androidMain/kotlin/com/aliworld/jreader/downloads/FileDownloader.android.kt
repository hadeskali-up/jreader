package com.aliworld.jreader.downloads
import com.aliworld.jreader.storage.PlatformStorage
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import java.io.File
actual object FileDownloader {private val client=HttpClient();actual suspend fun download(url:String,path:String):String{val f=File(path);f.parentFile?.mkdirs();val temp=File("$path.part");temp.writeBytes(client.get(url).readRawBytes());check(temp.renameTo(f)){"Could not finalize $path"};return f.absolutePath};actual fun chapterDir(mangaId:String,chapterId:String)=File(PlatformStorage.backupPath()).parentFile!!.parentFile!!.resolve("downloads/${safe(mangaId)}/${safe(chapterId)}").absolutePath;private fun safe(v:String)=v.replace(Regex("[^A-Za-z0-9._-]"),"_")}
