package io.grpc.demo

import org.slf4j.Logger
import org.slf4j.Marker

class UnitTestLogger : Logger {

    override fun getName(): String {
        throw UnsupportedOperationException()
    }

    override fun isTraceEnabled(): Boolean {
        throw UnsupportedOperationException()
    }

    override fun isTraceEnabled(marker: Marker?): Boolean {
        throw UnsupportedOperationException()
    }

    override fun trace(msg: String?) {
        throw UnsupportedOperationException()
    }

    override fun trace(format: String?, arg: Any?) {
        throw UnsupportedOperationException()
    }

    override fun trace(format: String?, arg1: Any?, arg2: Any?) {
        throw UnsupportedOperationException()
    }

    override fun trace(format: String?, vararg arguments: Any?) {
        throw UnsupportedOperationException()
    }

    override fun trace(msg: String?, t: Throwable?) {
        throw UnsupportedOperationException()
    }

    override fun trace(marker: Marker?, msg: String?) {
        throw UnsupportedOperationException()
    }

    override fun trace(marker: Marker?, format: String?, arg: Any?) {
        throw UnsupportedOperationException()
    }

    override fun trace(marker: Marker?, format: String?, arg1: Any?, arg2: Any?) {
        throw UnsupportedOperationException()
    }

    override fun trace(marker: Marker?, format: String?, vararg argArray: Any?) {
        throw UnsupportedOperationException()
    }

    override fun trace(marker: Marker?, msg: String?, t: Throwable?) {
        throw UnsupportedOperationException()
    }

    override fun isDebugEnabled(): Boolean {
        throw UnsupportedOperationException()
    }

    override fun isDebugEnabled(marker: Marker?): Boolean {
        throw UnsupportedOperationException()
    }

    override fun debug(msg: String) {
        println(msg)
    }

    override fun debug(format: String, arg: Any?) {
        println(format.format(arg))
    }

    override fun debug(format: String?, arg1: Any?, arg2: Any?) {
        throw UnsupportedOperationException()
    }

    override fun debug(format: String?, vararg arguments: Any?) {
        throw UnsupportedOperationException()
    }

    override fun debug(msg: String?, t: Throwable?) {
        throw UnsupportedOperationException()
    }

    override fun debug(marker: Marker?, msg: String?) {
        throw UnsupportedOperationException()
    }

    override fun debug(marker: Marker?, format: String?, arg: Any?) {
        throw UnsupportedOperationException()
    }

    override fun debug(marker: Marker?, format: String?, arg1: Any?, arg2: Any?) {
        throw UnsupportedOperationException()
    }

    override fun debug(marker: Marker?, format: String?, vararg arguments: Any?) {
        throw UnsupportedOperationException()
    }

    override fun debug(marker: Marker?, msg: String?, t: Throwable?) {
        throw UnsupportedOperationException()
    }

    override fun isInfoEnabled(): Boolean {
        throw UnsupportedOperationException()
    }

    override fun isInfoEnabled(marker: Marker?): Boolean {
        throw UnsupportedOperationException()
    }

    override fun info(msg: String?) {
        throw UnsupportedOperationException()
    }

    override fun info(format: String?, arg: Any?) {
        throw UnsupportedOperationException()
    }

    override fun info(format: String?, arg1: Any?, arg2: Any?) {
        throw UnsupportedOperationException()
    }

    override fun info(format: String?, vararg arguments: Any?) {
        throw UnsupportedOperationException()
    }

    override fun info(msg: String?, t: Throwable?) {
        throw UnsupportedOperationException()
    }

    override fun info(marker: Marker?, msg: String?) {
        throw UnsupportedOperationException()
    }

    override fun info(marker: Marker?, format: String?, arg: Any?) {
        throw UnsupportedOperationException()
    }

    override fun info(marker: Marker?, format: String?, arg1: Any?, arg2: Any?) {
        throw UnsupportedOperationException()
    }

    override fun info(marker: Marker?, format: String?, vararg arguments: Any?) {
        throw UnsupportedOperationException()
    }

    override fun info(marker: Marker?, msg: String?, t: Throwable?) {
        throw UnsupportedOperationException()
    }

    override fun isWarnEnabled(): Boolean {
        throw UnsupportedOperationException()
    }

    override fun isWarnEnabled(marker: Marker?): Boolean {
        throw UnsupportedOperationException()
    }

    override fun warn(msg: String) {
        println(msg)
    }

    override fun warn(format: String?, arg: Any?) {
        throw UnsupportedOperationException()
    }

    override fun warn(format: String?, vararg arguments: Any?) {
        throw UnsupportedOperationException()
    }

    override fun warn(format: String, arg1: Any?, arg2: Any?) {
        println(format.format(arg1, arg2))
    }

    override fun warn(msg: String?, t: Throwable?) {
        throw UnsupportedOperationException()
    }

    override fun warn(marker: Marker?, msg: String?) {
        throw UnsupportedOperationException()
    }

    override fun warn(marker: Marker?, format: String?, arg: Any?) {
        throw UnsupportedOperationException()
    }

    override fun warn(marker: Marker?, format: String?, arg1: Any?, arg2: Any?) {
        throw UnsupportedOperationException()
    }

    override fun warn(marker: Marker?, format: String?, vararg arguments: Any?) {
        throw UnsupportedOperationException()
    }

    override fun warn(marker: Marker?, msg: String?, t: Throwable?) {
        throw UnsupportedOperationException()
    }

    override fun isErrorEnabled(): Boolean {
        throw UnsupportedOperationException()
    }

    override fun isErrorEnabled(marker: Marker?): Boolean {
        throw UnsupportedOperationException()
    }

    override fun error(msg: String) {
        System.err.println(msg)
    }

    override fun error(format: String?, arg: Any?) {
        throw UnsupportedOperationException()
    }

    override fun error(format: String?, arg1: Any?, arg2: Any?) {
        throw UnsupportedOperationException()
    }

    override fun error(format: String?, vararg arguments: Any?) {
        throw UnsupportedOperationException()
    }

    override fun error(msg: String, t: Throwable) {
        System.err.println(msg)
        t.printStackTrace()
    }

    override fun error(marker: Marker?, msg: String?) {
        throw UnsupportedOperationException()
    }

    override fun error(marker: Marker?, format: String?, arg: Any?) {
        throw UnsupportedOperationException()
    }

    override fun error(marker: Marker?, format: String?, arg1: Any?, arg2: Any?) {
        throw UnsupportedOperationException()
    }

    override fun error(marker: Marker?, format: String?, vararg arguments: Any?) {
        throw UnsupportedOperationException()
    }

    override fun error(marker: Marker?, msg: String?, t: Throwable?) {
        throw UnsupportedOperationException()
    }
}
