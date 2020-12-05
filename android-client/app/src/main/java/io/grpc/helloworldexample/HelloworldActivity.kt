package io.grpc.helloworldexample

import android.app.Activity
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import grpc.demo.*
import grpc.demo.Number
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import io.grpc.stub.StreamObserver
import java.io.PrintWriter
import java.io.StringWriter
import java.lang.ref.WeakReference
import java.util.concurrent.TimeUnit

class HelloworldActivity : AppCompatActivity() {
    private var hostEdit: EditText? = null
    private var portEdit: EditText? = null
    private var messageEdit: EditText? = null
    private var resultText: TextView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_helloworld)
        hostEdit = findViewById(R.id.host_edit_text)
        portEdit = findViewById(R.id.port_edit_text)
        messageEdit = findViewById(R.id.message_edit_text)
        resultText = findViewById<TextView>(R.id.grpc_response_text)
                .apply {
                    movementMethod = ScrollingMovementMethod()
                }
    }

    fun getFeature(view: View) {
        doTask(view, Task.GET_FEATURE)
    }

    fun listFeatures(view: View) {
        doTask(view, Task.LIST_FEATURES)
    }

    fun recordRoute(view: View) {
        doTask(view, Task.RECORD_ROUTE)
    }

    fun routeChat(view: View) {
        doTask(view, Task.ROUTE_CHAT)
    }

    private fun doTask(view: View, task: Task) {
        (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager)
                .hideSoftInputFromWindow(hostEdit!!.windowToken, 0)
        view.isEnabled = false
        resultText!!.text = ""
        GrpcTask(this, task, view)
                .execute(
                        hostEdit!!.text.toString(),
                        messageEdit!!.text.toString(),
                        portEdit!!.text.toString())
    }

    private enum class Task {
        GET_FEATURE, LIST_FEATURES, RECORD_ROUTE, ROUTE_CHAT
    }

    private class GrpcTask(activity: Activity, private val task: Task, view: View) : AsyncTask<String?, Void?, String>() {
        private val activityReference: WeakReference<Activity>
        var sb = StringBuilder()
        private var channel: ManagedChannel? = null
        private val view: View
        var recordRouteResponseObserver: StreamObserver<CalculationResult> = object : StreamObserver<CalculationResult> {
            override fun onNext(value: CalculationResult) {
                sb.append(value.toString())
                Handler(Looper.getMainLooper()).post { print(sb.toString()) }
            }

            override fun onError(t: Throwable) {}
            override fun onCompleted() {}
        }
        var routeChatResponseObserver: StreamObserver<CalculationResult> = object : StreamObserver<CalculationResult> {
            override fun onNext(value: CalculationResult) {
                sb.append(value.toString())
                Handler(Looper.getMainLooper()).post { print(sb.toString()) }
            }

            override fun onError(t: Throwable) {}
            override fun onCompleted() {}
        }

        override fun doInBackground(vararg p0: String?): String? {
            val host = "192.168.1.20" //params[0];
            val portStr = "50051" //params[2];
            val port = if (TextUtils.isEmpty(portStr)) 0 else Integer.valueOf(portStr)
            return try {
                channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build()
                val blockingStub = GreeterGrpc.newBlockingStub(channel)
                if (task == Task.GET_FEATURE) blockingStub.calculate(
                        BinaryOperation.newBuilder()
                                .setOperation(Operation.ADD)
                                .setFirstOperand(10f)
                                .setSecondOperand(10f)
                                .build()
                ).toString() else if (task == Task.LIST_FEATURES) {
                    val iterator = blockingStub.fibonacci(Number.newBuilder().setValue(4f).build())
                    sb.setLength(0)
                    while (iterator.hasNext()) {
                        sb.append(iterator.next().toString())
                        Handler(Looper.getMainLooper()).post { print(sb.toString()) }
                    }
                    sb.toString()
                } else if (task == Task.RECORD_ROUTE) {
                    val stub = GreeterGrpc.newStub(channel)
                    sb.setLength(0)
                    val requestObserver = stub.sum(recordRouteResponseObserver)
                    for (i in 0..9) {
                        requestObserver.onNext(Number.newBuilder().setValue(i.toFloat()).build())
                        Thread.sleep(1000)
                    }
                    requestObserver.onCompleted()
                    sb.toString()
                } else if (task == Task.ROUTE_CHAT) {
                    val stub = GreeterGrpc.newStub(channel)
                    sb.setLength(0)
                    val requestObserver = stub.sumRunning(routeChatResponseObserver)
                    for (i in 0..9) {
                        requestObserver.onNext(Number.newBuilder().setValue(i.toFloat()).build())
                        Thread.sleep(1000)
                    }
                    requestObserver.onCompleted()
                    sb.toString()
                } else {
                    "error"
                }
            } catch (e: Exception) {
                val sw = StringWriter()
                val pw = PrintWriter(sw)
                e.printStackTrace(pw)
                pw.flush()
                String.format("Failed... : %n%s", sw)
            }
        }

        override fun onPostExecute(result: String) {
            try {
                channel!!.shutdown().awaitTermination(1, TimeUnit.SECONDS)
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
            }
            print(result)
        }

        private fun print(text: String) {
            val activity = activityReference.get() ?: return
            val resultText = activity.findViewById<TextView>(R.id.grpc_response_text)
            resultText.text = text
            view.isEnabled = true
        }

        init {
            activityReference = WeakReference(activity)
            this.view = view
        }
    }
}
