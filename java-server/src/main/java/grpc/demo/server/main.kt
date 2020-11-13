package grpc.demo.server

fun main() {
    val server = HelloWorldServer()
    server.start()
    server.blockUntilShutdown()
}
