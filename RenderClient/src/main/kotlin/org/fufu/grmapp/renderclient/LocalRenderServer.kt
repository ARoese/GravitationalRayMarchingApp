package org.fufu.grmapp.renderclient

import io.ktor.network.sockets.InetSocketAddress
import java.io.File

class LocalRenderServer(val exePath: File, val port: Int): AutoCloseable{
    private fun startProcess(): Process {
        require(exePath.exists())
        // TODO: actually configurable port number
        var process = ProcessBuilder(exePath.path).start()
        process.onExit().thenAccept{
            println("grtServer died.")
        }
        return process
    }
    private var process: Process = startProcess()

    fun getEndpoint(): InetSocketAddress{
        return InetSocketAddress("localhost", port)
    }

    fun makeClient(): RenderClient {
        return RenderClient(getEndpoint())
    }

    override fun close() {
        process.destroy()
    }
}