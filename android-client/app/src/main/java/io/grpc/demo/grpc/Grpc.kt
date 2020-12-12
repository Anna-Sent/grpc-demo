package io.grpc.demo.grpc

import io.grpc.ClientInterceptor
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import io.grpc.demo.KEEPALIVE_TIMEOUT_SECONDS
import io.grpc.demo.KEEPALIVE_TIME_SECONDS
import org.slf4j.Logger
import java.util.concurrent.TimeUnit.SECONDS

class Grpc constructor(
    val logger: Logger,
    val options: GrpcOptions,
    endpoint: GrpcEndpoint,
    private val interceptors: List<ClientInterceptor>
) {

    val channel: ManagedChannel

    init {
        val userAgent = System.getProperty("http.agent")
        channel = createChannel(endpoint, userAgent)
    }

    private fun createChannel(
        endpoint: GrpcEndpoint,
        userAgent: String?
    ) = with(endpoint) {
        ManagedChannelBuilder.forTarget(target)
            .apply { if (usingSsl) useTransportSecurity() else usePlaintext() }
            .userAgent(userAgent)
            .intercept(interceptors)
            .keepAliveTime(KEEPALIVE_TIME_SECONDS, SECONDS)
            .keepAliveTimeout(KEEPALIVE_TIMEOUT_SECONDS, SECONDS)
            .keepAliveWithoutCalls(true)
            .build()
    }
}

data class GrpcOptions(
    val checkInUiThread: Boolean,
    val shutdownTimeoutMs: Long
)

data class GrpcEndpoint(
    val target: String,
    val usingSsl: Boolean
)
