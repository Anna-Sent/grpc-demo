package io.grpc.demo.grpc.interceptors

import io.grpc.CallOptions
import io.grpc.Channel
import io.grpc.ClientCall
import io.grpc.ClientInterceptor
import io.grpc.ForwardingClientCall.SimpleForwardingClientCall
import io.grpc.Metadata
import io.grpc.MethodDescriptor
import io.grpc.Status
import io.grpc.Status.Code.CANCELLED
import io.grpc.Status.Code.OK
import org.slf4j.Logger

class GrpcLogInterceptor(
    private val url: String,
    private val logger: Logger
) : ClientInterceptor {

    companion object {
        private fun <T> List<T>.secondOrNull(): T? = if (isEmpty()) null else this[1]
    }

    override fun <Request, Response> interceptCall(
        method: MethodDescriptor<Request, Response>,
        callOptions: CallOptions,
        next: Channel
    ): ClientCall<Request, Response> = LogClientCall(
        next.newCall(method, callOptions),
        "$url ${method.fullMethodName.split("/").secondOrNull() ?: method.fullMethodName}"
    )

    private inner class LogClientCall<Request, Response>(
        call: ClientCall<Request, Response>,
        private val tag: String
    ) : SimpleForwardingClientCall<Request, Response>(call) {

        private fun log(message: String) = logger.debug(message)

        override fun sendMessage(message: Request) {
            log("$tag => sendMessage $message")
            super.sendMessage(message)
        }

        override fun start(responseListener: Listener<Response>, headers: Metadata) {
            val decorator = object : Listener<Response>() {
                override fun onReady() {
                    log("$tag => onReady")
                    responseListener.onReady()
                }

                override fun onMessage(message: Response) {
                    log("$tag => onMessage $message")
                    responseListener.onMessage(message)
                }

                override fun onHeaders(headers: Metadata?) {
                    responseListener.onHeaders(headers)
                }

                override fun onClose(status: Status, trailers: Metadata?) {
                    when (status.code) {
                        OK -> log("$tag => closed ${status.description}")
                        CANCELLED -> log("$tag => cancelled ${status.description}")
                        else -> log("$tag => failed ${status.code} ${status.description} ${status.cause}")
                    }
                    responseListener.onClose(status, trailers)
                }
            }

            super.start(decorator, headers)
        }
    }
}
