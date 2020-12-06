package io.grpc.demo.grpc

import io.grpc.ClientInterceptor
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import org.slf4j.Logger
import java.util.concurrent.TimeUnit.SECONDS

class Grpc constructor(
    val logger: Logger,
    val options: GrpcOptions,
    val endpoint: Endpoint,
    private val interceptors: List<ClientInterceptor>
) {

    val channel: ManagedChannel

    init {
        val userAgent = System.getProperty("http.agent")
        channel = createChannel(endpoint, userAgent)
    }

    private fun createChannel(
        endpoint: Endpoint,
        userAgent: String?
    ) = with(endpoint) {
        ManagedChannelBuilder.forTarget(target)
            .apply { if (usingSsl) useTransportSecurity() else usePlaintext() }
            .userAgent(userAgent)
            .intercept(interceptors)
            // Если не получаем пакетов от сервера, отправляем keepalive-ping чеерез 10 сек.
            .keepAliveTime(10, SECONDS)
            // Ждем ответа keepalive-ping от сервера 10 сек.
            .keepAliveTimeout(10, SECONDS)
            .keepAliveWithoutCalls(true)
            .build()
    }
}

data class Endpoint(
    val target: String,
    val usingSsl: Boolean
)
