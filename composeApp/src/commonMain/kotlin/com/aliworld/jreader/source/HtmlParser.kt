package com.aliworld.jreader.source
import com.aliworld.jreader.core.model.Manga
expect object HtmlParser { fun feed(html:String):List<Manga>;fun next(html:String):String?;fun detail(html:String,url:String):Pair<Manga,List<String>>;fun image(html:String):String? }
