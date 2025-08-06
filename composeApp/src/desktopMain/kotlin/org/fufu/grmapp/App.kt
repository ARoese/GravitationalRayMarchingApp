package org.fufu.grmapp

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import res.Res
import org.fufu.grmapp.renderclient.BlobMap
import org.fufu.grmapp.renderclient.LocalRenderServer
import org.fufu.grmapp.renderclient.make_test_scene
import org.fufu.grmapp.ui.EditTabs
import org.fufu.grmapp.ui.SceneDisplay
import org.fufu.grmapp.ui.SceneDisplayRoot
import org.jetbrains.compose.ui.tooling.preview.Preview
import protokt.v1.grm.protobuf.Material
import protokt.v1.grm.protobuf.RenderConfig
import protokt.v1.grm.protobuf.RenderDevice
import protokt.v1.grm.protobuf.Scene
import protokt.v1.grm.protobuf.UInt2
import java.io.File
import java.net.URI

data class RenderSpec(
    val scene: Scene,
    val renderConfig: RenderConfig,
    val device: RenderDevice = RenderDevice.GPU,
    val blobs: BlobMap = emptyMap()
){
    // remove any blobs from blobmap that aren't referenced in scene.
    // this returns a new copy of RenderSpec, and helps keep bandwidth and memory usage down
    fun trimmedBlobs(): RenderSpec {
        fun Material.Shader?.blobRefs(): Set<UInt>{
            if(this == null){
                return emptySet()
            }
            return when(this){
                is Material.Shader.Color -> null
                is Material.Shader.Texture -> this.texture.blobIdent?.id?.let { setOf(it) }
            } ?: emptySet()
        }
        val referencedBlobs = scene.nohit?.shader.blobRefs() +
                scene.bodies.mapNotNull { it.material?.shader.blobRefs() }.flatten()
        if((blobs - referencedBlobs).isEmpty()){
            return this
        }
        return this.copy(blobs = blobs.filterKeys { it in referencedBlobs })
    }
}

@Composable
@Preview
fun App() {
    MaterialTheme {
        var renderSpec by remember { mutableStateOf(
            RenderSpec(
                make_test_scene(),
                RenderConfig{
                    resolution = UInt2{x = 512.toUInt(); y = 512.toUInt()}
                }
            )
        ) }

        Box(Modifier
            .safeContentPadding()
            .fillMaxSize()
        ){
            Row {
                Box(Modifier.weight(5f)){
                    SceneDisplayRoot(renderSpec)
                }
                Box(Modifier.weight(4f)){
                    EditTabs(renderSpec){
                        renderSpec = it.trimmedBlobs()
                    }
                }
            }
        }
    }
}