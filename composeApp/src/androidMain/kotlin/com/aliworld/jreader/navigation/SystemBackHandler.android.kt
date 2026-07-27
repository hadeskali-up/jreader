package com.aliworld.jreader.navigation
import androidx.compose.runtime.Composable
import androidx.activity.compose.BackHandler
@Composable actual fun SystemBackHandler(enabled:Boolean,onBack:()->Unit)=BackHandler(enabled,onBack)
