package com.ashasuresh.edgedetectionviewer

object NativeBridge {
    init {
        System.loadLibrary("native-lib")
    }

    external fun processEdges(inputMatAddr: Long, outputMatAddr: Long)
}
