package grpc.demo.server

import grpc.demo.BinaryOperation
import grpc.demo.CalculationResult
import grpc.demo.CalculatorServiceGrpc.CalculatorServiceImplBase
import grpc.demo.Number
import grpc.demo.Operation.ADD
import grpc.demo.Operation.SUBTRACT
import io.grpc.stub.StreamObserver
import java.util.concurrent.TimeUnit
import java.util.logging.Level
import java.util.logging.Logger

class CalculatorServiceImpl(private val logger: Logger) : CalculatorServiceImplBase() {

    companion object {

        private val TIMEOUT_MS = TimeUnit.SECONDS.toMillis(1)
    }

    override fun calculate(request: BinaryOperation, responseObserver: StreamObserver<CalculationResult>) {
        val result = with(request) {
            when (operation) {
                ADD -> firstOperand.value + secondOperand.value
                SUBTRACT -> firstOperand.value - secondOperand.value
                else -> {
                    responseObserver.onError(UnsupportedOperationException("Unsupported $operation"))
                    return
                }
            }
        }
        responseObserver.onNext(result)
        responseObserver.onCompleted()
    }

    override fun fibonacci(request: Number, responseObserver: StreamObserver<CalculationResult>) {
        var count = request.value.toInt()
        if (count >= 1) {
            Thread.sleep(TIMEOUT_MS)
            responseObserver.onNext(0)
        }
        if (count >= 2) {
            Thread.sleep(TIMEOUT_MS)
            responseObserver.onNext(1)
        }
        var state = 0L to 1L
        while (count >= 3) {
            state = state.second to state.first + state.second
            Thread.sleep(TIMEOUT_MS)
            responseObserver.onNext(state.second)
            --count
        }
        responseObserver.onCompleted()
    }

    override fun sumTotal(responseObserver: StreamObserver<CalculationResult>) =
        object : StreamObserver<Number> {

            var sum = 0L

            override fun onNext(number: Number) {
                sum += number.value
            }

            override fun onError(t: Throwable) {
                logger.log(Level.WARNING, "sumTotal cancelled")
            }

            override fun onCompleted() {
                responseObserver.onNext(sum)
                responseObserver.onCompleted()
            }
        }

    override fun sumCurrent(responseObserver: StreamObserver<CalculationResult>) =
        object : StreamObserver<Number> {

            var sum = 0L

            override fun onNext(number: Number) {
                sum += number.value
                responseObserver.onNext(sum)
            }

            override fun onError(t: Throwable) {
                logger.log(Level.WARNING, "sumCurrent cancelled")
            }

            override fun onCompleted() {
                responseObserver.onCompleted()
            }
        }

    private fun StreamObserver<CalculationResult>.onNext(value: Long) =
        onNext(
            CalculationResult.newBuilder()
                .setResult(
                    Number.newBuilder()
                        .setValue(value)
                        .build()
                )
                .build()
        )
}
