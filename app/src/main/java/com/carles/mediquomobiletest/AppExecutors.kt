package com.carles.mediquomobiletest

import android.os.Handler
import android.os.Looper
import java.util.concurrent.Executor

class AppExecutors(val workerThread: Executor, val mainThread: Executor)

class MainThreadExecutor : Executor {
    private val mainThreadHandler = Handler(Looper.getMainLooper())
    override fun execute(command: Runnable) {
        mainThreadHandler.post(command)
    }
}