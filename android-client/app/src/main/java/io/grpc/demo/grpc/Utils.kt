package io.grpc.demo.grpc

import android.os.Build
import android.os.Looper

fun isOnUiThread() =
    if (isAtLeastMarshmallow()) {
        Looper.getMainLooper().isCurrentThread
    } else {
        Thread.currentThread() === Looper.getMainLooper().thread
    }

private fun isAtLeastMarshmallow() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
