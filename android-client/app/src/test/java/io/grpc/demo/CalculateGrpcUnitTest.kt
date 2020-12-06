package io.grpc.demo

import grpc.demo.BinaryOperation
import grpc.demo.GreeterGrpc
import grpc.demo.GreeterGrpc.GreeterBlockingStub
import grpc.demo.Operation.ADD
import io.grpc.demo.grpc.Endpoint
import io.grpc.demo.grpc.Grpc
import io.grpc.demo.grpc.GrpcOptions
import io.grpc.demo.grpc.call
import io.grpc.demo.grpc.interceptors.GrpcLogInterceptor
import org.junit.Test

class CalculateGrpcUnitTest {

    private val logger = UnitTestLogger()

    private val grpc = Grpc(
        logger,
        GrpcOptions(false, 10000),
        Endpoint(URL, false),
        listOf(GrpcLogInterceptor(URL, logger))
    )

    @Test
    fun calculate() {
        grpc.call(
            null,
            GreeterGrpc::newBlockingStub,
            BinaryOperation.newBuilder()
                .setFirstOperand(11f)
                .setSecondOperand(22f)
                .setOperation(ADD)
                .build(),
            GreeterBlockingStub::calculate,
            SINGLE_REQUEST_TIMEOUT_SECONDS,
            { result ->
                result.fold(
                    { logger.debug("onSuccess $it") },
                    { logger.debug("onFailure $it") }
                )
            }
        )
    }
}
