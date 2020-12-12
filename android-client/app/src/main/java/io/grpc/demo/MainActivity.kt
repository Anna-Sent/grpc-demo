package io.grpc.demo

import android.app.Activity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.NumberPicker
import androidx.core.view.isVisible
import grpc.demo.BinaryOperation
import grpc.demo.Number
import grpc.demo.Operation.ADD
import grpc.demo.Operation.SUBTRACT
import io.grpc.demo.databinding.ActivityMainBinding
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.slf4j.LoggerFactory

class MainActivity : Activity() {

    private val logger = LoggerFactory.getLogger(toString())

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: FibonacciAdapter
    private var calculateDisposable: Disposable? = null
    private var fibonacciDisposable: Disposable? = null
    private val calculatorDataSource = CalculatorDataSource()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        binding = ActivityMainBinding.bind(findViewById(R.id.content))
        binding.layoutFibonacci.progressBar.isVisible = false
        binding.layoutFibonacci.numberPicker.setupLimits()
        binding.layoutFibonacci.numberPicker.setOnValueChangedListener { _, _, newValue ->
            // TODO Нужен debounce
            subscribeToFibonacci(newValue)
        }
        adapter = FibonacciAdapter()
        binding.layoutFibonacci.numbers.adapter = adapter

        binding.layoutCalculate.progressBar.isVisible = false
        binding.layoutCalculate.param1.setupLimits()
        binding.layoutCalculate.param2.setupLimits()
        ArrayAdapter.createFromResource(
            this,
            R.array.binary_operation,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.layoutCalculate.spinner.adapter = adapter
        }
        binding.layoutCalculate.equals.setOnClickListener {
            val selectedItem = binding.layoutCalculate.spinner.selectedItem as String
            subscribeToCalculate(
                binding.layoutCalculate.param1.value.toLong(),
                binding.layoutCalculate.param2.value.toLong(),
                selectedItem
            )
        }
    }

    private fun NumberPicker.setupLimits() {
        minValue = 0
        maxValue = 50
    }

    override fun onStop() {
        super.onStop()
        unsubscribeFromFibonacci()
    }

    private fun subscribeToFibonacci(number: Int) {
        unsubscribeFromFibonacci()
        val result = mutableListOf<Long>()
        fibonacciDisposable = Single.fromCallable {
            Number.newBuilder()
                .setValue(number.toLong())
                .build()
        }
            .flatMapObservable { calculatorDataSource.fibonacci(it) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe {
                result.clear()
                submitList(result)
                binding.layoutFibonacci.progressBar.isVisible = true
                binding.layoutFibonacci.info.text = ""
            }
            // TODO Нужен таймаут между попытками
            .retry { count, throwable ->
                binding.layoutFibonacci.info.text = "$count: $throwable"
                true
            }
            .subscribe(
                {
                    result += it.result.value
                    binding.layoutFibonacci.progressBar.isVisible = false
                    //binding.layoutFibonacci.result.text = "$number -> $result"
                    submitList(result)
                    // save last number
                },
                {
                    binding.layoutFibonacci.progressBar.isVisible = false
                    binding.layoutFibonacci.info.text = it.toString()
                    // print error
                },
                {
                    binding.layoutFibonacci.progressBar.isVisible = false
                    binding.layoutFibonacci.info.text = "Completed, no data"
                    // save last number
                }
            )
    }

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
                binding.layoutFibonacci.info.text = ""
            }
            // TODO Нужен таймаут между попытками
            .retry { count, throwable ->
                binding.layoutCalculate.info.text = "$count: $throwable"
                true
            }
            .subscribe(
                {
                    binding.layoutCalculate.progressBar.isVisible = false
                    binding.layoutCalculate.info.text =
                        "$number1 $operation $number2 = ${it.result.value}"
                    // save last number
                },
                {
                    binding.layoutCalculate.progressBar.isVisible = false
                    binding.layoutCalculate.info.text = it.toString()
                    // print error
                }
            )
    }

    private fun unsubscribeFromCalculate() {
        calculateDisposable
            ?.run { if (!isDisposed) dispose() }
            .also { calculateDisposable = null }
    }
}
