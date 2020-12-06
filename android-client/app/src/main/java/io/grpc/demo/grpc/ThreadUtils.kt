package io.grpc.demo.grpc

import android.os.Build
import android.os.Looper

fun isAtLeastMarshmallow() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M

fun isOnUiThread() =
    if (isAtLeastMarshmallow()) {
        Looper.getMainLooper().isCurrentThread
    } else {
        Thread.currentThread() === Looper.getMainLooper().thread
    }
