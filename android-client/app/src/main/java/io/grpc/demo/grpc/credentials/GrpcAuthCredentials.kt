package io.grpc.demo.grpc.credentials

import io.grpc.CallCredentials
import io.grpc.Metadata
import io.grpc.Metadata.Key
import io.grpc.Status
import java.util.concurrent.Executor

class GrpcAuthCredentials(private val accessToken: String) : CallCredentials() {

    @Suppress("TooGenericExceptionCaught")
    override fun applyRequestMetadata(
        requestInfo: RequestInfo,
        appExecutor: Executor,
        applier: MetadataApplier
    ) = appExecutor.execute {
        try {
            val metadata = Metadata()
            putMetadataKeys(metadata)
            applier.apply(metadata)
        } catch (throwable: Throwable) {
            applier.fail(Status.UNAUTHENTICATED.withCause(throwable))
        }
    }

    override fun thisUsesUnstableApi() {
        // no op
    }

    private fun putMetadataKeys(metadata: Metadata) {
        metadata.put(
            Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER),
            "Bearer $accessToken"
        )
    }
}
