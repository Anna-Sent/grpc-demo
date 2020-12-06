package io.grpc.demo

// Не устанавливаем таймаут для стримов, т.к. будет срабатывать, когда в стриме
// просто нет данных.
const val STREAM_REQUEST_TIMEOUT_SECONDS = 0L
const val SINGLE_REQUEST_TIMEOUT_SECONDS = 30L

const val URL = "192.168.1.27:50051"
