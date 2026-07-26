package com.aliworld.jreader
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
class MainActivity: ComponentActivity() { override fun onCreate(state: Bundle?) { super.onCreate(state); Store.init(applicationContext); setContent { App() } } }
