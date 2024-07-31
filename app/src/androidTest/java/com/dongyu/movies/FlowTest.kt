package com.dongyu.movies

import android.util.Log
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

// 冷流只有在数据收集的时候才会发射
fun flow1() = flow<Int> {
  for (i in 0 until 10) {
    // 发射数据
    emit(i)
  }
}

// 热流不会管收集着是否工作，也会发送数据，
// 如果收集不及时，将会无法收集全部发送的数据
suspend fun shareFlow1() {
  val shareFlow = MutableSharedFlow<Int>()
  GlobalScope.launch {
    for (i in 0..10) {
      println("shareFlowEmit: $i")
      shareFlow.emit(i)
      delay(100L)
    }
  }

  delay(200)

  GlobalScope.launch {
    shareFlow.collect {
      println("shareFlow Collect: $it")
    }
  }

  delay(2000)
}

// 设置replay为2，如果数据已经发送了，而收集器还没有收集，
// 则还会收集之前未接收的两条数据
// replayCache 缓存区中的数据，及最新的两条已发送的数据
suspend fun shareFlow2() {
  val sharedFlow = MutableSharedFlow<Int>(replay = 2)
  GlobalScope.launch {
    for (i in 0..10) {
      sharedFlow.emit(i)
      println("shareFlow replayCache: ${sharedFlow.replayCache.joinToString()}")
      delay(100)
    }
  }

  delay(1000L)

  GlobalScope.launch {
    sharedFlow.collect {
      println("shareFlow collect: $it")
    }
  }

  delay(2000L)
}

// 设置缓存区大小为 0，缓冲策略为挂起
// 当缓存区不足时，发送数据将等待收集完成时才会继续发送，处于挂起的状态
suspend fun shareFlow3() {
  val sharedFlow = MutableSharedFlow<Int>(extraBufferCapacity = 2, onBufferOverflow = BufferOverflow.SUSPEND)

  val startTime = System.currentTimeMillis()

  GlobalScope.launch {
    sharedFlow.collect {
      val current = System.currentTimeMillis()
      println("shareFlow ${current - startTime} collect: $it")
      delay(300)
    }
  }

  GlobalScope.launch {
    for (i in 0..10) {
      val current = System.currentTimeMillis()
      println("shareFlow ${current - startTime} emit: $i")
      sharedFlow.emit(i)
      delay(100)
    }
  }
  delay(5000)
}

fun main() {
  /*delay(3000)

  flow1().collect {
    println("收集到的数据: $it")
  }*/

  runBlocking {
    shareFlow3()
  }

  flow {
   emit(0)
  }
}