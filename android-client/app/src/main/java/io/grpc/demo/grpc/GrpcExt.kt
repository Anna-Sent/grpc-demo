package io.grpc.demo.grpc

import android.os.NetworkOnMainThreadException
import io.grpc.CallCredentials
import io.grpc.Channel
import io.grpc.Context
import io.grpc.Deadline
import io.grpc.stub.AbstractStub
import java.util.concurrent.TimeUnit.SECONDS

@Suppress("TooGenericExceptionCaught", "LongParameterList")
fun <Request, Response, S : AbstractStub<S>> Grpc.call(
    credentials: CallCredentials?,
    getStub: (Channel) -> S,
    request: Request,
    rpcCall: (S, Request) -> Response,
    timeoutInSeconds: Long,
    handleResult: (Result<Response>) -> Unit
) {
    if (options.checkInUiThread && isOnUiThread()) {
        throw NetworkOnMainThreadException()
    }
    val cancellableContext = Context.current().withCancellation()
    try {
        cancellableContext.run {
            var stub = getStub(channel)
                .withDeadline(deadline(timeoutInSeconds))
            if (credentials != null) {
                stub = stub.withCallCredentials(credentials)
            }
            val response = rpcCall(stub, request)

            startClosingThread()

            handleResult(Result.success(response))
        }
    } catch (throwable: Throwable) {
        cancellableContext.cancel(throwable)
        handleResult(Result.failure(throwable))
    } finally {
        if (options.shutdownTimeoutMs > 0) {
            close()
        }
    }
}

private fun deadline(timeoutInSeconds: Long) =
    if (timeoutInSeconds > 0) {
        Deadline.after(timeoutInSeconds, SECONDS)
    } else {
        null
    }

private fun Grpc.startClosingThread() {
    if (options.shutdownTimeoutMs > 0) {
        Thread {
            try {
                Thread.sleep(options.shutdownTimeoutMs)
            } catch (e: InterruptedException) {
                logger.debug("startClosingThread: interrupted ", e)
                Thread.currentThread().interrupt()
            } finally {
                close()
            }
        }.start()
    }
}

@Suppress("TooGenericExceptionCaught")
fun Grpc.close() {
    try {
        channel.shutdownNow()
    } catch (throwable: Throwable) {
        logger.debug("close: failed ", throwable)
    }
}
