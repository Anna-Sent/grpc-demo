package io.grpc.demo

import android.R.layout
import android.app.Activity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.NumberPicker
import androidx.core.view.isVisible
import grpc.demo.BinaryOperation
import grpc.demo.CalculationResult
import grpc.demo.Number
import grpc.demo.Operation.ADD
import grpc.demo.Operation.SUBTRACT
import io.grpc.demo.R.array
import io.grpc.demo.databinding.ActivityMainBinding
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

@Suppress("TooManyFunctions")
class MainActivity : Activity() {

    companion object {

        @Suppress("MagicNumber")
        private fun FibonacciAdapter.submitList(result: MutableList<Long>) {
            submitList(
                result.withIndex()
                    .toList()
                    .takeLast(3)
                    .map { item -> Fibonacci(item.index, item.value) }
            )
        }

        @Suppress("MagicNumber")
        private fun NumberPicker.setupLimits() {
            minValue = 0
            maxValue = 50
        }

        private fun Int.toNumber() =
            Number.newBuilder()
                .setValue(toLong())
                .build()

        private fun Long.toNumber() =
            Number.newBuilder()
                .setValue(this)
                .build()

        private fun binaryOperation(number1: Long, number2: Long, operation: String) =
            BinaryOperation.newBuilder()
                .setFirstOperand(number1.toNumber())
                .setSecondOperand(number2.toNumber())
                .setOperation(operation.toOperation())
                .build()

        private fun String.toOperation() =
            when (this) {
                "+" -> ADD
                "-" -> SUBTRACT
                else -> throw UnsupportedOperationException("$this not supported")
            }
    }

    private lateinit var binding: ActivityMainBinding

    private val calculatorDataSource = CalculatorDataSource()

    private var calculateDisposable: Disposable? = null

    private var fibonacciDisposable: Disposable? = null
    private lateinit var adapter: FibonacciAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        binding = ActivityMainBinding.bind(findViewById(R.id.content))

        setupCalculate()
        setupFibonacci()
    }

    override fun onStop() {
        super.onStop()
        unsubscribeFromFibonacci()
    }

    override fun onDestroy() {
        super.onDestroy()
        unsubscribeFromCalculate()
    }

    private fun setupCalculate() {
        with(binding.layoutCalculate) {
            progressBar.isVisible = false
            param1.setupLimits()
            param2.setupLimits()
            ArrayAdapter.createFromResource(
                this@MainActivity,
                array.binary_operation,
                layout.simple_spinner_item
            ).also { adapter ->
                adapter.setDropDownViewResource(layout.simple_spinner_dropdown_item)
                operation.adapter = adapter
            }
            request.setOnClickListener {
                val operation = operation.selectedItem as String
                subscribeToCalculate(
                    param1.value.toLong(),
                    param2.value.toLong(),
                    operation
                )
            }
        }
    }

    private fun setupFibonacci() {
        with(binding.layoutFibonacci) {
            progressBar.isVisible = false
            param.setupLimits()
            request.setOnClickListener {
                subscribeToFibonacci(param.value)
            }
            adapter = FibonacciAdapter()
            result.adapter = adapter
        }
    }

    private fun subscribeToCalculate(first: Long, second: Long, operation: String) {
        unsubscribeFromCalculate()
        calculateDisposable =
            Single.fromCallable { binaryOperation(first, second, operation) }
                .flatMap { calculatorDataSource.calculate(it) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { handleCalculateStart(first, second, operation) }
                // TODO Нужен таймаут между попытками
                .retry { count, throwable -> handleCalculateRetry(count, throwable) }
                .subscribe(
                    { handleCalculateSuccess(first, second, operation, it) },
                    { handleCalculateError(it) }
                )
    }

    private fun unsubscribeFromCalculate() {
        calculateDisposable
            ?.run { if (!isDisposed) dispose() }
            .also { calculateDisposable = null }
    }

    private val fibonacci = mutableListOf<Long>()

    private fun subscribeToFibonacci(fibonacciCount: Int) {
        unsubscribeFromFibonacci()
        fibonacciDisposable = Single.fromCallable { fibonacciCount.toNumber() }
            .flatMapObservable { calculatorDataSource.fibonacci(it) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { handleFibonacciStart(fibonacciCount) }
            // TODO Нужен таймаут между попытками и ограничение на количество попыток
            .retry { count, throwable -> handleFibonacciRetry(count, throwable) }
            .subscribe(
                { handleFibonacciSuccess(fibonacciCount, it) },
                { handleFibonacciError(it) },
                { handleFibonacciCompleted() }
            )
    }

    private fun unsubscribeFromFibonacci() {
        fibonacciDisposable
            ?.run { if (!isDisposed) dispose() }
            .also { fibonacciDisposable = null }
    }

    private fun handleCalculateStart(
        first: Long,
        second: Long,
        operation: String,
    ) {
        with(binding.layoutCalculate) {
            progressBar.isVisible = true
            info.text = "$first $operation $second ="
            result.content.text = ""
        }
    }

    private fun handleCalculateRetry(count: Int, throwable: Throwable): Boolean {
        binding.layoutCalculate.info.text = "$count: $throwable"
        return true
    }

    private fun handleCalculateSuccess(
        first: Long,
        second: Long,
        operation: String,
        result: CalculationResult
    ) {
        with(binding.layoutCalculate) {
            progressBar.isVisible = false
            info.text = "$first $operation $second ="
            this.result.content.text = result.result.value.toString()
        }
    }

    private fun handleCalculateError(throwable: Throwable) {
        with(binding.layoutCalculate) {
            progressBar.isVisible = false
            info.text = throwable.toString()
        }
    }

    private fun handleFibonacciStart(fibonacciCount: Int) {
        with(binding.layoutFibonacci) {
            progressBar.isVisible = true
            info.text = "$fibonacciCount ->"
            fibonacci.clear()
            adapter.submitList(fibonacci)
        }
    }

    private fun handleFibonacciRetry(count: Int, throwable: Throwable): Boolean {
        binding.layoutFibonacci.info.text = "$count: $throwable"
        return true
    }

    private fun handleFibonacciSuccess(fibonacciCount: Int, result: CalculationResult) {
        with(binding.layoutFibonacci) {
            progressBar.isVisible = false
            info.text = "$fibonacciCount ->"
            fibonacci += result.result.value
            adapter.submitList(fibonacci)
        }
    }

    private fun handleFibonacciError(throwable: Throwable) {
        with(binding.layoutFibonacci) {
            progressBar.isVisible = false
            info.text = throwable.toString()
        }
    }

    private fun handleFibonacciCompleted() {
        with(binding.layoutFibonacci) {
            progressBar.isVisible = false
            info.text = "Completed. No more data."
        }
    }
}
