package io.grpc.demo

import android.R.layout
import android.app.Activity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.NumberPicker
import androidx.core.view.isVisible
import grpc.demo.BinaryOperation
import grpc.demo.Number
import grpc.demo.Operation.ADD
import grpc.demo.Operation.SUBTRACT
import io.grpc.demo.R.array
import io.grpc.demo.databinding.ActivityMainBinding
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class MainActivity : Activity() {

    private lateinit var binding: ActivityMainBinding

    private val calculatorDataSource = CalculatorDataSource()

    private var calculateDisposable: Disposable? = null

    private var fibonacciDisposable: Disposable? = null
    private lateinit var adapter: FibonacciAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        binding = ActivityMainBinding.bind(findViewById(R.id.content))

        setupFibonacci()

        setupCalculate()
    }

    private fun setupCalculate() {
        binding.layoutCalculate.progressBar.isVisible = false
        binding.layoutCalculate.param1.setupLimits()
        binding.layoutCalculate.param2.setupLimits()
        ArrayAdapter.createFromResource(
            this,
            array.binary_operation,
            layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(layout.simple_spinner_dropdown_item)
            binding.layoutCalculate.spinner.adapter = adapter
        }
        binding.layoutCalculate.request.setOnClickListener {
            val selectedItem = binding.layoutCalculate.spinner.selectedItem as String
            subscribeToCalculate(
                binding.layoutCalculate.param1.value.toLong(),
                binding.layoutCalculate.param2.value.toLong(),
                selectedItem
            )
        }
    }

    private fun setupFibonacci() {
        binding.layoutFibonacci.progressBar.isVisible = false
        binding.layoutFibonacci.numberPicker.setupLimits()
        binding.layoutFibonacci.request.setOnClickListener {
            subscribeToFibonacci(binding.layoutFibonacci.numberPicker.value)
        }
        adapter = FibonacciAdapter()
        binding.layoutFibonacci.numbers.adapter = adapter
    }

    @Suppress("MagicNumber")
    private fun NumberPicker.setupLimits() {
        minValue = 0
        maxValue = 50
    }

    override fun onStop() {
        super.onStop()
        unsubscribeFromFibonacci()
    }

    private fun subscribeToFibonacci(number: Int) {
        val fibonacci = mutableListOf<Long>()
        unsubscribeFromFibonacci()
        fibonacciDisposable = Single.fromCallable {
            Number.newBuilder()
                .setValue(number.toLong())
                .build()
        }
            .flatMapObservable { calculatorDataSource.fibonacci(it) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe {
                fibonacci.clear()
                submitList(fibonacci)
                binding.layoutFibonacci.progressBar.isVisible = true
            }
            // TODO Нужен таймаут между попытками и ограничение на количество попыток
            .retry { count, throwable ->
                binding.layoutFibonacci.info.text = "$count: $throwable"
                true
            }
            .subscribe(
                {
                    fibonacci += it.result.value
                    binding.layoutFibonacci.progressBar.isVisible = false
                    binding.layoutFibonacci.info.text = ""
                    submitList(fibonacci)
                },
                {
                    binding.layoutFibonacci.progressBar.isVisible = false
                    binding.layoutFibonacci.info.text = it.toString()
                },
                {
                    binding.layoutFibonacci.progressBar.isVisible = false
                    binding.layoutFibonacci.info.text = "Completed, no data"
                }
            )
    }

    @Suppress("MagicNumber")
    private fun submitList(result: MutableList<Long>) {
        adapter.submitList(
            result.withIndex()
                .toList()
                .takeLast(3)
                .map { item -> Fibonacci(item.index, item.value) }
        )
    }

    private fun unsubscribeFromFibonacci() {
        fibonacciDisposable
            ?.run { if (!isDisposed) dispose() }
            .also { fibonacciDisposable = null }
    }

    private fun subscribeToCalculate(number1: Long, number2: Long, operation: String) {
        unsubscribeFromCalculate()
        calculateDisposable = Single.fromCallable {
            BinaryOperation.newBuilder()
                .setFirstOperand(
                    Number.newBuilder()
                        .setValue(number1)
                        .build()
                )
                .setSecondOperand(
                    Number.newBuilder()
                        .setValue(number2)
                        .build()
                )
                .setOperation(
                    when (operation) {
                        "+" -> ADD
                        "-" -> SUBTRACT
                        else -> throw UnsupportedOperationException("$operation not supported")
                    }
                )
                .build()
        }
            .flatMap { calculatorDataSource.calculate(it) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe {
                binding.layoutCalculate.progressBar.isVisible = true
            }
            // TODO Нужен таймаут между попытками
            .retry { count, throwable ->
                binding.layoutCalculate.info.text = "$count: $throwable"
                true
            }
            .subscribe(
                {
                    binding.layoutCalculate.progressBar.isVisible = false
                    binding.layoutCalculate.card.content.text =
                        "$number1 $operation $number2 = ${it.result.value}"
                },
                {
                    binding.layoutCalculate.progressBar.isVisible = false
                    binding.layoutCalculate.info.text = it.toString()
                }
            )
    }

    private fun unsubscribeFromCalculate() {
        calculateDisposable
            ?.run { if (!isDisposed) dispose() }
            .also { calculateDisposable = null }
    }
}
