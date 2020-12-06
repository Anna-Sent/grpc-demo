package grpc.demo.server

import grpc.demo.BinaryOperation
import grpc.demo.CalculationResult
import grpc.demo.CalculatorServiceGrpc.CalculatorServiceImplBase
import grpc.demo.Number
import grpc.demo.Operation.ADD
import grpc.demo.Operation.SUBTRACT
import io.grpc.stub.StreamObserver
import java.util.logging.Level
import java.util.logging.Logger

class CalculatorServiceImpl(private val logger: Logger) : CalculatorServiceImplBase() {

    override fun calculate(request: BinaryOperation, responseObserver: StreamObserver<CalculationResult>) {
        val result = with(request) {
            when (operation) {
                ADD -> firstOperand + secondOperand
                SUBTRACT -> firstOperand - secondOperand
                else -> {
                    responseObserver.onError(UnsupportedOperationException("Unsupported $operation"))
                    return
                }
            }
        }
        responseObserver.onNext(CalculationResult.newBuilder().setResult(result).build())
        responseObserver.onCompleted()
    }

    override fun fibonacci(request: Number, responseObserver: StreamObserver<CalculationResult>) {
        var count = request.value.toInt()
        if (count >= 1) {
            Thread.sleep(5000)
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
            responseObserver.onNext(CalculationResult.newBuilder().setResult(state.second.toFloat()).build())
            --count
        }
        responseObserver.onCompleted()
    }

    override fun sumTotal(responseObserver: StreamObserver<CalculationResult>) =
        object : StreamObserver<Number> {

            var sum = 0f

            override fun onNext(number: Number) {
                sum += number.value
            }

            override fun onError(t: Throwable) {
                logger.log(Level.WARNING, "sumTotal cancelled")
            }

            override fun onCompleted() {
                responseObserver.onNext(CalculationResult.newBuilder().setResult(sum).build())
                responseObserver.onCompleted()
            }
        }

    override fun sumCurrent(responseObserver: StreamObserver<CalculationResult>) =
        object : StreamObserver<Number> {

            var sum = 0f

            override fun onNext(number: Number) {
                sum += number.value
                responseObserver.onNext(CalculationResult.newBuilder().setResult(sum).build())
            }

            override fun onError(t: Throwable) {
                logger.log(Level.WARNING, "sumCurrent cancelled")
            }

            override fun onCompleted() {
                responseObserver.onCompleted()
            }
        }
}
