package org.fufu.grmapp.renderclient

import io.ktor.network.sockets.InetSocketAddress
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

class LocalRenderServer private constructor(val exePath: File, var port: Int?): AutoCloseable{
    private fun startProcess(): Process {
        require(exePath.exists())
        val pb = port?.let {
            ProcessBuilder(exePath.path, "--port $it")
        } ?: ProcessBuilder(exePath.path)

        var process = pb
            .directory(exePath.parentFile)
            .start()
        process.onExit().thenAccept{
            println("grtServer died.")
        }
        return process
    }
    private var process: Process = startProcess()

    /*
    unsafe to call except from objects returned by create()
     */
    fun getEndpoint(): InetSocketAddress{
        return port.let {
            require(it != null)
            InetSocketAddress("localhost", it)
        }
    }

    /*
    unsafe to call except from objects returned by create()
     */
    fun makeClient(): RenderClient {
        return RenderClient(getEndpoint())
    }

    override fun close() {
        process.destroy()
    }

    companion object{
        suspend fun create(exePath: File, port: Int? = null): LocalRenderServer {
            val server = LocalRenderServer(exePath, port)
            val firstLine = try{
                withContext(Dispatchers.IO){
                    try{
                        server.process.inputStream.bufferedReader().readLine().let {
                            if(it == null){
                                null
                            }else{
                                it
                            }
                        }
                    }catch(e: IOException){
                        if(!isActive){
                            throw CancellationException()
                        }
                        throw e
                    }
                }
            }catch(e: Exception){
                server.close()
                throw e
            }

            server.port = firstLine?.split(" ")?.lastOrNull()?.trim()?.toInt()
                ?: throw IllegalStateException("render server did not advertise port. First line was:\n$firstLine")
            return server
        }
    }
}