package io.grpc.demo

import android.app.Activity
import android.os.Bundle
import androidx.core.view.isGone
import androidx.core.view.isVisible
import grpc.demo.Number
import io.grpc.demo.databinding.ActivityMainBinding
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers

class MainActivity : Activity() {

    private lateinit var binding: ActivityMainBinding
    private val disposables = CompositeDisposable()
    private val greeterDataSource = GreeterDataSource()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        binding = ActivityMainBinding.bind(findViewById(R.id.content))
        binding.layoutCalculate.progressBar.isGone = true
        binding.layoutFibonacci.progressBar.isGone = true
        binding.layoutSumCurrent.progressBar.isGone = true
        binding.layoutSumTotal.progressBar.isGone = true
    }

    override fun onStart() {
        super.onStart()
        val number = Number.newBuilder()
            .setValue(0f)
            .build()
        disposables += greeterDataSource.fibonacci(number)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe {
                binding.layoutFibonacci.progressBar.isVisible = true
                binding.layoutFibonacci.perform.isEnabled = false
            }
            // Нужен таймаут между попытками
            .retry { count, throwable ->
                binding.layoutFibonacci.result.text = "$count: $throwable"
                true
            }
            .subscribe(
                {
                    binding.layoutFibonacci.progressBar.isVisible = false
                    binding.layoutFibonacci.result.text = it.result.toString()
                    // save last number
                },
                {
                    binding.layoutFibonacci.progressBar.isVisible = false
                    binding.layoutFibonacci.result.text = it.toString()
                    // print error
                }
            )
    }

    override fun onStop() {
        super.onStop()
        disposables.clear()
    }
}
