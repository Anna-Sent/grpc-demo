package io.grpc.demo

import grpc.demo.BinaryOperation
import grpc.demo.CalculationResult
import grpc.demo.GreeterGrpc
import grpc.demo.GreeterGrpc.GreeterBlockingStub
import grpc.demo.Number
import io.grpc.demo.grpc.Endpoint
import io.grpc.demo.grpc.Grpc
import io.grpc.demo.grpc.GrpcOptions
import io.grpc.demo.grpc.GrpcSource
import io.grpc.demo.grpc.interceptors.GrpcLogInterceptor
import io.reactivex.Observable
import io.reactivex.Single
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("gRPCcc")

class GreeterDataSource : GrpcSource(
    Grpc(
        logger,
        GrpcOptions(true, 0),
        Endpoint(URL, false),
        listOf(GrpcLogInterceptor(URL, logger))
    )
) {

    fun calculate(operation: BinaryOperation): Single<CalculationResult> {
        return toSingle(
            null,
            GreeterGrpc::newBlockingStub,
            operation,
            GreeterBlockingStub::calculate
        )
    }

    fun fibonacci(number: Number): Observable<CalculationResult> {
        return toObservable(
            null,
            GreeterGrpc::newBlockingStub,
            number,
            GreeterBlockingStub::fibonacci
        )
    }
}
