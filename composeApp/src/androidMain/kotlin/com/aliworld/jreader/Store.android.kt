package com.aliworld.jreader
import android.content.Context
actual object Store {
 private lateinit var p: android.content.SharedPreferences
 fun init(c: Context) { p=c.getSharedPreferences("jreader", Context.MODE_PRIVATE) }
 actual fun get(key:String)=p.getString(key,"") ?: ""
 actual fun put(key:String,value:String){p.edit().putString(key,value).apply()}
}
