package grpc.demo.server

import io.grpc.Server
import io.grpc.ServerBuilder
import java.util.concurrent.TimeUnit
import java.util.logging.Logger

class HelloWorldServer {

    companion object {
        private val logger = Logger.getLogger(HelloWorldServer::class.java.name)
        private const val PORT = 50051
    }

    private var server: Server? = null
    private val shutdownHook = object : Thread() {
        override fun run() {
            System.err.println("*** shutting down gRPC server since JVM is shutting down")
            try {
                this@HelloWorldServer.stop()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
            System.err.println("*** server shut down")
        }
    }

    fun start() {
        server = ServerBuilder.forPort(PORT)
                .addService(GreeterImpl(logger))
                .build()
                .start()
        logger.info("Server started, listening on $PORT")
        Runtime.getRuntime().addShutdownHook(shutdownHook)
    }

    fun blockUntilShutdown() = server?.run { awaitTermination() }

    private fun stop() = server?.run { shutdown().awaitTermination(30, TimeUnit.SECONDS) }
}
