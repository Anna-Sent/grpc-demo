package grpc.demo.server

import grpc.demo.BinaryOperation
import grpc.demo.CalculationResult
import grpc.demo.GreeterGrpc.GreeterImplBase
import grpc.demo.Number
import grpc.demo.Operation.ADD
import grpc.demo.Operation.SUBTRACT
import io.grpc.stub.StreamObserver
import java.util.logging.Level
import java.util.logging.Logger

class GreeterImpl(private val logger: Logger) : GreeterImplBase() {

    override fun calculate(request: BinaryOperation, responseObserver: StreamObserver<CalculationResult>) {
        val result = with(request) {
            when (operation) {
                ADD -> firstOperand + secondOperand
                SUBTRACT -> firstOperand - secondOperand
                else -> throw UnsupportedOperationException("Unsupported $operation")
            }
        }
        responseObserver.onNext(CalculationResult.newBuilder().setResult(result).build())
        responseObserver.onCompleted()
    }

    override fun fibonacci(request: Number, responseObserver: StreamObserver<CalculationResult>) {
        var count = request.value.toInt()
        if (count >= 1) {
            responseObserver.onNext(CalculationResult.newBuilder().setResult(0f).build())
        }
        if (count >= 2) {
            Thread.sleep(5000)
            responseObserver.onNext(CalculationResult.newBuilder().setResult(1f).build())
        }
        var state = 0 to 1
        while (count >= 3) {
            state = state.second to state.first + state.second
            Thread.sleep(5000)
            responseObserver.onNext(
                    CalculationResult.newBuilder()
                            .setResult(state.second.toFloat())
                            .build())
            --count
        }
        responseObserver.onCompleted()
    }

    override fun sum(responseObserver: StreamObserver<CalculationResult>) =
            object : StreamObserver<Number> {

                var sum = 0f

                override fun onNext(number: Number) {
                    sum += number.value
                }

                override fun onError(t: Throwable) {
                    logger.log(Level.WARNING, "sum cancelled")
                }

                override fun onCompleted() {
                    responseObserver.onNext(CalculationResult.newBuilder().setResult(sum).build())
                    responseObserver.onCompleted()
                }
            }

    override fun sumRunning(responseObserver: StreamObserver<CalculationResult>) =
            object : StreamObserver<Number> {

                var sum = 0f

                override fun onNext(number: Number) {
                    sum += number.value
                    responseObserver.onNext(CalculationResult.newBuilder().setResult(sum).build())
                }

                override fun onError(t: Throwable) {
                    logger.log(Level.WARNING, "sumRunning cancelled")
                }

                override fun onCompleted() {
                    responseObserver.onCompleted()
                }
            }
}
