package com.aliworld.jreader
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.aliworld.jreader.repository.AppContainer
import com.aliworld.jreader.storage.PlatformStorage
class MainActivity:ComponentActivity(){private lateinit var container:AppContainer;override fun onCreate(state:Bundle?){super.onCreate(state);PlatformStorage.init(applicationContext);container=AppContainer();setContent{App(container)}};override fun onDestroy(){container.close();super.onDestroy()}}
