import io.ktor.network.sockets.InetSocketAddress
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.fufu.grmapp.renderclient.RenderClient
import org.fufu.grmapp.renderclient.simpleTestRender
import org.junit.jupiter.api.Test


class RenderServerTests {
    @Test
    fun testSimpleRender(){
        val result = runBlocking {
            simpleTestRender(RenderClient(InetSocketAddress("localhost", 9000)))
        }
        println(result)
        //println(result.toString())
    }

    @Test
    fun testManyRender(){
        runBlocking {
            (0..5).map { _ ->
                async{
                    simpleTestRender(RenderClient(InetSocketAddress("localhost", 9000)))
                }
            }.awaitAll()
        }
    }
}