import io.ktor.network.sockets.InetSocketAddress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.fufu.grmapp.renderclient.RenderServer
import org.fufu.grmapp.renderclient.simpleTestRender
import org.junit.jupiter.api.Test


class RenderServerTests {
    @Test
    fun testSimpleRender(){
        val result = runBlocking {
            simpleTestRender(RenderServer(InetSocketAddress("localhost", 9000)))
        }
        println(result)
        //println(result.toString())
    }

    @Test
    fun testManyRender(){
        runBlocking {
            (0..40).map { _ ->
                async{
                    simpleTestRender(RenderServer(InetSocketAddress("localhost", 9000)))
                }
            }.awaitAll()
        }
    }
}