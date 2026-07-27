package com.aliworld.jreader
import org.jsoup.Jsoup
actual object HtmlParser {
 actual fun feed(html:String)=Jsoup.parse(html,"https://e-hentai.org/").select("table.itg tr").mapNotNull { row ->
  val a=row.selectFirst("a[href*=/g/]") ?: return@mapNotNull null
  val img=row.selectFirst("img")
  Gallery(a.absUrl("href"), row.selectFirst(".glink")?.text() ?: a.text(), image(img), row.selectFirst(".cn,.cs")?.text().orEmpty())
 }.distinctBy{it.url}
 actual fun next(html:String)=Jsoup.parse(html,"https://e-hentai.org/").selectFirst("a[href*='next=']")?.attr("href")?.substringAfter("next=")?.substringBefore('&')
 actual fun detail(html:String,url:String):Pair<Gallery,List<String>> { val d=Jsoup.parse(html,url);val title=d.selectFirst("#gn")?.text().orEmpty().ifBlank{"Untitled"};val cover=image(d.selectFirst("#gd1 img"));val category=d.selectFirst("#gdc")?.text().orEmpty();val uploader=d.selectFirst("#gdn a")?.text().orEmpty();val pages=d.selectFirst("#gdd tr:contains(Length) .gdt2")?.text().orEmpty();val links=d.select("#gdt a").map{it.absUrl("href")}.filter{it.isNotBlank()};return Gallery(url,title,cover,category,uploader,pages) to links }
 actual fun image(html:String)=Jsoup.parse(html).selectFirst("#img")?.absUrl("src")
 private fun image(e:org.jsoup.nodes.Element?):String { if(e==null)return "";val raw=e.attr("data-src").ifBlank{e.attr("src")};return if(raw.startsWith("//"))"https:$raw" else raw }
}
