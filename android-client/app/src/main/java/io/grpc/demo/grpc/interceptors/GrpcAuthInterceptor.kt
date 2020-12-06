package io.grpc.demo.grpc.interceptors

import io.grpc.CallOptions
import io.grpc.Channel
import io.grpc.ClientCall
import io.grpc.ClientInterceptor
import io.grpc.ForwardingClientCall.SimpleForwardingClientCall
import io.grpc.Metadata
import io.grpc.Metadata.Key
import io.grpc.MethodDescriptor
import io.grpc.Status
import io.grpc.Status.Code.UNAUTHENTICATED
import org.slf4j.Logger

class GrpcAuthInterceptor(
    private val logger: Logger
) : ClientInterceptor {

    override fun <Request : Any?, Response : Any?> interceptCall(
        method: MethodDescriptor<Request, Response>,
        callOptions: CallOptions,
        next: Channel
    ): ClientCall<Request, Response> = AuthClientCall(next.newCall(method, callOptions))

    private inner class AuthClientCall<Request, Response>(
        call: ClientCall<Request, Response>
    ) : SimpleForwardingClientCall<Request, Response>(call) {

        override fun start(responseListener: Listener<Response>, headers: Metadata) {
            // TODO val token = accessToken()
            //  if (token.isNullOrBlank()) {
            //     throw NotAuthorizedException("access token is null")
            //  }
            //  addCredentials(headers, token)
            val decorator = object : Listener<Response>() {
                override fun onReady() {
                    responseListener.onReady()
                }

                override fun onMessage(message: Response) {
                    responseListener.onMessage(message)
                }

                override fun onHeaders(headers: Metadata?) {
                    responseListener.onHeaders(headers)
                }

                override fun onClose(status: Status, trailers: Metadata?) {
                    responseListener.onClose(status, trailers)
                    if (status.code == UNAUTHENTICATED) {
                        // TODO refreshToken(token)
                    }
                }
            }
            super.start(decorator, headers)
        }

        @Suppress("TooGenericExceptionCaught", "unused")
        private fun addCredentials(headers: Metadata, token: String) {
            try {
                headers.put(
                    Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER),
                    "Bearer $token"
                )
            } catch (throwable: Throwable) {
                // TODO refreshToken(token)
            }
        }
    }
}
