package io.grpc.demo

import android.app.Activity
import android.os.Bundle
import androidx.core.view.isVisible
import grpc.demo.Number
import io.grpc.demo.databinding.ActivityMainBinding
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.slf4j.LoggerFactory

class MainActivity : Activity() {

    private val logger = LoggerFactory.getLogger(toString())

    private lateinit var binding: ActivityMainBinding
    private var fibonacciDisposable: Disposable? = null
    private val calculatorDataSource = CalculatorDataSource()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        binding = ActivityMainBinding.bind(findViewById(R.id.content))
        binding.layoutFibonacci.numberPicker.minValue = 0
        binding.layoutFibonacci.numberPicker.maxValue = 100
        binding.layoutFibonacci.numberPicker.setOnValueChangedListener { _, _, newValue ->
            // TODO Нужен debounce
            subscribeToFibonacci(newValue)
        }
    }

    override fun onStop() {
        super.onStop()
        unsubscribeFromFibonacci()
    }

    private fun subscribeToFibonacci(number: Int) {
        unsubscribeFromFibonacci()
        val result = mutableListOf<Int>()
        fibonacciDisposable = Single.fromCallable {
            Number.newBuilder()
                .setValue(number.toFloat())
                .build()
        }
            .flatMapObservable { calculatorDataSource.fibonacci(it) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe {
                binding.layoutFibonacci.progressBar.isVisible = true
                binding.layoutFibonacci.perform.isEnabled = false
                binding.layoutFibonacci.result.text = "$number -> ..."
            }
            // TODO Нужен таймаут между попытками
            .retry { count, throwable ->
                binding.layoutFibonacci.result.text = "$count: $throwable"
                true
            }
            .subscribe(
                {
                    result += it.result.toInt()
                    binding.layoutFibonacci.progressBar.isVisible = false
                    binding.layoutFibonacci.perform.isEnabled = true
                    binding.layoutFibonacci.result.text = "$number -> $result"
                    // save last number
                },
                {
                    binding.layoutFibonacci.progressBar.isVisible = false
                    binding.layoutFibonacci.perform.isEnabled = true
                    binding.layoutFibonacci.result.text = it.toString()
                    // print error
                },
                {
                    binding.layoutFibonacci.progressBar.isVisible = false
                    binding.layoutFibonacci.perform.isEnabled = true
                    binding.layoutFibonacci.result.text = "Completed, no data"
                    // save last number
                }
            )
    }

    private fun unsubscribeFromFibonacci() {
        fibonacciDisposable
            ?.run { if (!isDisposed) dispose() }
            .also { fibonacciDisposable = null }
    }
}
