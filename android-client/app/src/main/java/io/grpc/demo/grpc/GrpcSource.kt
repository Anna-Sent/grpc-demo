package io.grpc.demo.grpc

import io.grpc.CallCredentials
import io.grpc.Channel
import io.grpc.demo.SINGLE_REQUEST_TIMEOUT_SECONDS
import io.grpc.demo.STREAM_REQUEST_TIMEOUT_SECONDS
import io.grpc.demo.grpc.exceptions.GrpcCancelledException
import io.grpc.stub.AbstractStub
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.Single
import io.reactivex.SingleEmitter

open class GrpcSource(protected val grpc: Grpc) : UrlProvider {

    final override val url = grpc.endpoint.target

    protected fun <Request, Response, S : AbstractStub<S>> toSingle(
        getBlockingStub: (Channel) -> S,
        request: Request,
        rpcCall: (S, Request) -> Response
    ): Single<Response> = toSingle(null, getBlockingStub, request, rpcCall)

    protected fun <Request, Response, S : AbstractStub<S>> toSingle(
        credentials: CallCredentials?,
        getBlockingStub: (Channel) -> S,
        request: Request,
        rpcCall: (S, Request) -> Response
    ): Single<Response> = Single.create { source ->
        grpc.call(
            credentials, getBlockingStub, request, rpcCall,
            SINGLE_REQUEST_TIMEOUT_SECONDS,
            {
                it.fold(
                    { response -> onSuccess(source, response) },
                    { throwable -> onError(source, throwable) }
                )
            })
    }

    protected fun <Request, Response, S : AbstractStub<S>> toObservable(
        getStub: (Channel) -> S,
        request: Request,
        rpcCall: (S, Request) -> Iterator<Response>
    ): Observable<Response> = toObservable(null, getStub, request, rpcCall)

    protected fun <Request, Response, S : AbstractStub<S>> toObservable(
        credentials: CallCredentials?,
        getStub: (Channel) -> S,
        request: Request,
        rpcCall: (S, Request) -> Iterator<Response>
    ): Observable<Response> = Observable.create { source ->
        grpc.call(
            credentials, getStub, request, rpcCall,
            STREAM_REQUEST_TIMEOUT_SECONDS,
            {
                it.fold(
                    { iterator ->
                        while (iterator.hasNext()) {
                            val response: Response = iterator.next()
                            onNext(source, response)
                        }
                        onComplete(source)
                    },
                    { throwable -> onError(source, throwable) }
                )
            })
    }

    companion object {

        private fun <T> onSuccess(emitter: SingleEmitter<T>, data: T) {
            if (!emitter.isDisposed) {
                emitter.onSuccess(data)
            }
        }

        private fun <T> onError(emitter: SingleEmitter<T>, throwable: Throwable) {
            if (!emitter.isDisposed) {
                emitter.onError(throwable)
            }
        }

        private fun <T> onNext(emitter: ObservableEmitter<T>, data: T) {
            if (!emitter.isDisposed) {
                emitter.onNext(data)
            } else {
                throw GrpcCancelledException()
            }
        }

        private fun <T> onComplete(emitter: ObservableEmitter<T>) {
            if (!emitter.isDisposed) {
                emitter.onComplete()
            }
        }

        private fun <T> onError(emitter: ObservableEmitter<T>, throwable: Throwable) {
            if (!emitter.isDisposed) {
                emitter.onError(throwable)
            }
        }
    }
}
