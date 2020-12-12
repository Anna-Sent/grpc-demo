package io.grpc.demo

import android.app.Application
import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.android.LogcatAppender
import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.core.util.StatusPrinter
import org.slf4j.LoggerFactory

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        setupLogging()
    }

    private fun setupLogging() {
        val context = LoggerFactory.getILoggerFactory() as LoggerContext
        context.reset()

        val rootLogger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as Logger
        rootLogger.level = if (BuildConfig.DEBUG) Level.TRACE else Level.DEBUG

        val logcatAppender = logcatAppender(context)
        rootLogger.addAppender(logcatAppender)

        StatusPrinter.print(context)
    }

    private fun logcatAppender(context: LoggerContext) =
        LogcatAppender().apply {
            this.context = context
            name = "logcat"
            encoder = PatternLayoutEncoder().apply {
                this.context = context
                pattern = "%msg"
                start()
            }
            start()
        }
}
