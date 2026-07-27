package com.aliworld.jreader.navigation
import com.aliworld.jreader.core.model.*
import kotlinx.coroutines.flow.*
sealed interface Route { data object Root:Route; data class Detail(val manga:Manga):Route; data class Reader(val manga:Manga,val chapter:Chapter):Route }
class Navigator(start:Route=Route.Root){ private val stack=MutableStateFlow(listOf(start));val routes:StateFlow<List<Route>> = stack.asStateFlow();val current get()=stack.value.last();fun push(route:Route){stack.value=stack.value+route};fun back():Boolean=if(stack.value.size>1){stack.value=stack.value.dropLast(1);true}else false }
