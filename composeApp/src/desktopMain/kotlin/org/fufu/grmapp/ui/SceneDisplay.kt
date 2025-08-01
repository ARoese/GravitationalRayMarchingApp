package org.fufu.grmapp.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import io.ktor.network.sockets.InetSocketAddress
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.withContext
import org.fufu.grmapp.RenderSpec
import org.fufu.grmapp.renderclient.RenderServer
import org.fufu.grmapp.toImageBitmapAsync
import protokt.v1.grm.protobuf.BlobIdentifier
import protokt.v1.grm.protobuf.BlobsHeader
import protokt.v1.grm.protobuf.RenderRequest

@Composable
fun SceneDisplay(renderSpec: RenderSpec){
    var image by remember { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(renderSpec){
        val renderServer = RenderServer(
            InetSocketAddress("localhost", 9000)
        )

        val renderRequest = RenderRequest{
            this.scene = renderSpec.scene
            this.config = renderSpec.renderConfig
            this.device = renderSpec.device
            this.blobsInfo = BlobsHeader{
                blobs = renderSpec.blobs.map { blob ->
                    BlobsHeader.BlobLengthId{
                        blobLength = blob.value.size.toULong()
                        identifier = BlobIdentifier{ id = blob.key }
                    }
                }
            }
        }

        withContext(coroutineContext + CoroutineName("render coroutine")){
            val rawRenderResult = renderServer.render(renderRequest, renderSpec.blobs)
            image = rawRenderResult.toImageBitmapAsync()
        }
    }
    Box(Modifier.fillMaxSize().border(4.dp, Color(255, 0, 0))){
        val currentImage = image
        if(currentImage != null){
            Image(
                currentImage,
                "example image",
                Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }
    }
}