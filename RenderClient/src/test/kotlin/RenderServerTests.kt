import io.ktor.network.sockets.InetSocketAddress
import kotlinx.coroutines.runBlocking
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
}