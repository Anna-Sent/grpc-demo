package io.grpc.demo

// Не устанавливаем таймаут для стримов, т.к. будет срабатывать, когда в стриме
// просто нет данных.
const val STREAM_REQUEST_TIMEOUT_SECONDS = 0L

// Taймаут для нестримовых запросов (unary call).
const val SINGLE_REQUEST_TIMEOUT_SECONDS = 30L

// Если не получаем пакетов от сервера, отправляем keepalive-ping чеерез 10 сек.
const val KEEPALIVE_TIME_SECONDS = 10L

// Ждем ответа keepalive-ping от сервера 10 сек.
const val KEEPALIVE_TIMEOUT_SECONDS = 10L

const val URL = "192.168.1.18:50051"
