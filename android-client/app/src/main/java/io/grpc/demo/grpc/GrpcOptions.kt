package io.grpc.demo.grpc

data class GrpcOptions(
    val checkInUiThread: Boolean,
    val shutdownTimeoutMs: Long
)
