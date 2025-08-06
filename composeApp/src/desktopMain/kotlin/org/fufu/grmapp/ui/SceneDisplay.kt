package org.fufu.grmapp.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ContextMenuArea
import androidx.compose.foundation.ContextMenuItem
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.ImageFormat
import io.github.vinceglb.filekit.dialogs.compose.util.encodeToByteArray
import io.github.vinceglb.filekit.dialogs.openFileSaver
import io.github.vinceglb.filekit.write
import io.ktor.network.sockets.InetSocketAddress
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import org.fufu.grmapp.RenderSpec
import org.fufu.grmapp.renderclient.LocalRenderServer
import org.fufu.grmapp.renderclient.RenderClient
import org.fufu.grmapp.toImageBitmapAsync
import org.jetbrains.compose.resources.painterResource
import protokt.v1.grm.protobuf.BlobIdentifier
import protokt.v1.grm.protobuf.BlobsHeader
import protokt.v1.grm.protobuf.RenderRequest
import res.Res
import res.close
import java.io.File
import java.net.URI

@Composable
fun produceLocalRenderServer(): LocalRenderServer?{
    val renderServer by produceState<LocalRenderServer?>(null){
        value = LocalRenderServer.create(
            File(
                URI(Res.getUri("files/GravitationalRayMarchingServer.exe")).path
            )
        )
    }
    DisposableEffect(Unit){
        onDispose {
            // this local server needs to get cleaned up,
            // and we can't rely on the garbage collector to do that.
            // otherwise, we might leave a child process running when we exit
            renderServer?.close()
        }
    }
    return renderServer
}

@Composable
fun rememberRenderClient(address: InetSocketAddress?): RenderClient? {
    if(address == null){
        return produceLocalRenderServer()?.makeClient()
    }
    return remember(address){RenderClient(address)}
}

@Composable
fun produceImage(renderSpec: RenderSpec, renderClient: RenderClient, onFailure: (Exception) -> Unit): ImageBitmap? {
    val image by produceState<ImageBitmap?>(null, renderSpec, renderClient){
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

        val rawRenderResult = try{
            renderClient.render(renderRequest, renderSpec.blobs)
        }catch (e: Exception){
            if(e is CancellationException){
                throw e
            }
            onFailure(e)
            null
        }

        value = rawRenderResult?.toImageBitmapAsync()
    }
    return image
}

@Composable
fun EndpointPicker(
    currentEndpoint: InetSocketAddress?,
    onHide: () -> Unit,
    onPicked: (InetSocketAddress?) -> Unit
){
    var address by remember { mutableStateOf(currentEndpoint?.hostname ?: "localhost") }
    var port by remember { mutableStateOf(currentEndpoint?.port ?: 9000) }
    Column {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)){
            Column(horizontalAlignment = Alignment.CenterHorizontally){
                Text("Address")
                TextField(
                    address,
                    onValueChange = {
                        address = it
                    }
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally){
                Text("Port")
                NumberField(port){
                    port = it
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)){
            val usingBundled = currentEndpoint == null
            Button(
                enabled = !usingBundled,
                onClick = {
                    onPicked(null)
                }
            ){
                Text(if(usingBundled){"Using Bundled"}else{"Use Bundled"})
            }
            Button(
                onClick = {
                    onPicked(InetSocketAddress(address, port))
                }
            ){
                Text("Submit")
            }
            Spacer(Modifier.weight(1f))
            IconButton(
                onClick = onHide
            ){
                Icon(painterResource(Res.drawable.close), "close")
            }
        }
    }
}

suspend fun trySaveImage(image: ImageBitmap){
    val saveLocation = FileKit.openFileSaver(
        "rendered image",
        "png"
    )
    saveLocation?.write(image.encodeToByteArray(ImageFormat.PNG))
}

@Composable
fun SceneDisplayRoot(renderSpec: RenderSpec){
    val sbh = remember { SnackbarHostState() }
    Scaffold(
        snackbarHost = {
            SnackbarHost(sbh)
        }
    ){
        SceneDisplay(renderSpec, sbh)
    }
}

@Composable
fun SceneDisplay(renderSpec: RenderSpec, sbh: SnackbarHostState){
    val coroutineScope = rememberCoroutineScope()

    var renderEndpoint by remember{mutableStateOf<InetSocketAddress?>(null)}
    val renderClient = rememberRenderClient(renderEndpoint)
    val image = renderClient?.let{
        produceImage(renderSpec, renderClient){
            coroutineScope.launch {
                sbh.showSnackbar(
                    "Render failed: ${it.message}",
                    withDismissAction = true,
                    duration = SnackbarDuration.Indefinite
                )
            }
        }
    }

    var showEndpointPicker by remember { mutableStateOf(false) }
    Column{
        AnimatedVisibility(showEndpointPicker){
            EndpointPicker(
                renderEndpoint,
                onHide = {
                    showEndpointPicker = false
                },
                onPicked = {
                    renderEndpoint = it
                }
            )
        }

        ContextMenuArea(items = {
            listOf(
                ContextMenuItem("Download"){
                    image?.let{
                        coroutineScope.launch {
                            trySaveImage(image)
                        }
                    }
                },
                ContextMenuItem("Set Render Server"){
                    showEndpointPicker = true
                },
            )
        }) {
            Box(Modifier.fillMaxSize()){
                if(image != null){
                    Image(
                        image,
                        "example image",
                        Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }
    }
}