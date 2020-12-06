package io.grpc.demo

import grpc.demo.BinaryOperation
import grpc.demo.CalculatorServiceGrpc
import grpc.demo.CalculatorServiceGrpc.CalculatorServiceBlockingStub
import grpc.demo.Number
import grpc.demo.Operation.ADD
import io.grpc.StatusRuntimeException
import io.grpc.demo.grpc.Endpoint
import io.grpc.demo.grpc.Grpc
import io.grpc.demo.grpc.GrpcOptions
import io.grpc.demo.grpc.call
import io.grpc.demo.grpc.interceptors.GrpcLogInterceptor
import org.conscrypt.OpenSSLProvider
import org.junit.Assert
import org.junit.Test
import java.security.Security

class CalculateGrpcUnitTest {

    companion object {

        private val logger = UnitTestLogger()

        private val grpc = Grpc(
            logger,
            GrpcOptions(false, 30000),
            Endpoint(URL, false),
            listOf(GrpcLogInterceptor(URL, logger))
        )

        private val streamingErrorHandler = { throwable: Throwable ->
            if (!contains(throwable, "Channel shutdownNow invoked")) {
                Assert.fail("Failed with $throwable")
            }
        }

        private fun initSsl() {
            Security.addProvider(OpenSSLProvider())
        }

        private fun contains(throwable: Throwable, type: String) =
            if (throwable is StatusRuntimeException) {
                val description = throwable.status?.description
                description?.contains(type) ?: false
            } else {
                false
            }
    }

    init {
        initSsl()
    }

    @Test
    fun calculate() {
        grpc.call(
            null,
            CalculatorServiceGrpc::newBlockingStub,
            BinaryOperation.newBuilder()
                .setFirstOperand(11f)
                .setSecondOperand(22f)
                .setOperation(ADD)
                .build(),
            CalculatorServiceBlockingStub::calculate,
            SINGLE_REQUEST_TIMEOUT_SECONDS,
            { result ->
                result.fold(
                    { logger.debug("onSuccess ${it.result}") },
                    { logger.debug("onFailure $it") }
                )
            }
        )
    }

    @Test
    fun fibonacci() {
        grpc.call(
            null,
            CalculatorServiceGrpc::newBlockingStub,
            Number.newBuilder()
                .setValue(5f)
                .build(),
            CalculatorServiceBlockingStub::fibonacci,
            SINGLE_REQUEST_TIMEOUT_SECONDS,
            { result ->
                result.fold(
                    { stream ->
                        var i = 0
                        while (stream.hasNext()) {
                            println("\ti: $i")
                            val response = stream.next()
                            println("\t${response.result}")
                            ++i
                        }
                    },
                    streamingErrorHandler
                )
            }
        )
    }
}
