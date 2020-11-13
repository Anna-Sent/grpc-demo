/*
 * Copyright 2015 The gRPC Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.grpc.helloworldexample;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import grpc.demo.BinaryOperation;
import grpc.demo.CalculationResult;
import grpc.demo.GreeterGrpc;
import grpc.demo.Number;
import grpc.demo.Operation;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

public class HelloworldActivity extends AppCompatActivity {
    //private Button sendButton;
    private EditText hostEdit;
    private EditText portEdit;
    private EditText messageEdit;
    private TextView resultText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_helloworld);
        //sendButton = (Button) findViewById(R.id.send_button);
        hostEdit = (EditText) findViewById(R.id.host_edit_text);
        portEdit = (EditText) findViewById(R.id.port_edit_text);
        messageEdit = (EditText) findViewById(R.id.message_edit_text);
        resultText = (TextView) findViewById(R.id.grpc_response_text);
        resultText.setMovementMethod(new ScrollingMovementMethod());
    }

    public void getFeature(View view) {
        doTask(view, Task.GET_FEATURE);
    }

    public void listFeatures(View view) {
        doTask(view, Task.LIST_FEATURES);
    }

    public void recordRoute(View view) {
        doTask(view, Task.RECORD_ROUTE);
    }

    public void routeChat(View view) {
        doTask(view, Task.ROUTE_CHAT);
    }

    private void doTask(View view, Task task) {
        ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                .hideSoftInputFromWindow(hostEdit.getWindowToken(), 0);
        view.setEnabled(false);
        resultText.setText("");
        new GrpcTask(this, task, view)
                .execute(
                        hostEdit.getText().toString(),
                        messageEdit.getText().toString(),
                        portEdit.getText().toString());
    }

    private enum Task {
        GET_FEATURE, LIST_FEATURES, RECORD_ROUTE, ROUTE_CHAT
    }

    private static class GrpcTask extends AsyncTask<String, Void, String> {
        private final WeakReference<Activity> activityReference;
        StringBuilder sb = new StringBuilder();
        private ManagedChannel channel;
        private Task task;
        private View view;
        StreamObserver<CalculationResult> recordRouteResponseObserver = new StreamObserver<CalculationResult>() {
            @Override
            public void onNext(CalculationResult value) {
                sb.append(value.toString());
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        print(sb.toString());
                    }
                });
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onCompleted() {

            }
        };
        StreamObserver<CalculationResult> routeChatResponseObserver = new StreamObserver<CalculationResult>() {
            @Override
            public void onNext(CalculationResult value) {
                sb.append(value.toString());
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        print(sb.toString());
                    }
                });
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onCompleted() {

            }
        };

        private GrpcTask(Activity activity, Task task, View view) {
            this.task = task;
            this.activityReference = new WeakReference<Activity>(activity);
            this.view = view;
        }

        @Override
        protected String doInBackground(String... params) {
            String host = "192.168.1.20";//params[0];
            String message = params[1];
            String portStr = "50051";//params[2];
            int port = TextUtils.isEmpty(portStr) ? 0 : Integer.valueOf(portStr);
            try {
                channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
                GreeterGrpc.GreeterBlockingStub blockingStub = GreeterGrpc.newBlockingStub(channel);
                // GreeterGrpc.GreeterBlockingStub blockingStub = GreeterGrpc.newBlockingStub(channel);
//        rpc GetFeature(Point) returns (Feature) {}
//        rpc ListFeatures(Rectangle) returns (stream Feature) {}
//        rpc RecordRoute(stream Point) returns (RouteSummary) {}
//        rpc RouteChat(stream RouteNote) returns (stream RouteNote) {}
                if (task == Task.GET_FEATURE)
                    return blockingStub.calculate(
                            BinaryOperation.newBuilder()
                                    .setOperation(Operation.ADD)
                                    .setFirstOperand(10)
                                    .setSecondOperand(10)
                                    .build()
                    ).toString();
                else if (task == Task.LIST_FEATURES) {
                    Iterator<CalculationResult> iterator = blockingStub.fibonacci(Number.newBuilder().setValue(4).build());
                    sb.setLength(0);
                    while (iterator.hasNext()) {
                        sb.append(iterator.next().toString());
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                print(sb.toString());
                            }
                        });
                    }
                    return sb.toString();
                } else if (task == Task.RECORD_ROUTE) {
                    GreeterGrpc.GreeterStub stub = GreeterGrpc.newStub(channel);
                    sb.setLength(0);
                    StreamObserver<Number> requestObserver = stub.sum(recordRouteResponseObserver);
                    for (int i = 0; i < 10; ++i) {
                        requestObserver.onNext(Number.newBuilder().setValue(i).build());
                        Thread.sleep(1000);
                    }
                    requestObserver.onCompleted();
                    return sb.toString();
                } else if (task == Task.ROUTE_CHAT) {
                    GreeterGrpc.GreeterStub stub = GreeterGrpc.newStub(channel);
                    sb.setLength(0);
                    StreamObserver<Number> requestObserver = stub.sumRunning(routeChatResponseObserver);
                    for (int i = 0; i < 10; ++i) {
                        requestObserver.onNext(Number.newBuilder().setValue(i).build());
                        Thread.sleep(1000);
                    }
                    requestObserver.onCompleted();
                    return sb.toString();
                } else {
                    return "error";
                }
            } catch (Exception e) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                pw.flush();
                return String.format("Failed... : %n%s", sw);
            }
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                channel.shutdown().awaitTermination(1, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            print(result);
        }

        private void print(String text) {
            Activity activity = activityReference.get();
            if (activity == null) {
                return;
            }
            TextView resultText = activity.findViewById(R.id.grpc_response_text);
            //Button sendButton = (Button) activity.findViewById(R.id.send_button);
            resultText.setText(text);
            view.setEnabled(true);
        }
    }
}
