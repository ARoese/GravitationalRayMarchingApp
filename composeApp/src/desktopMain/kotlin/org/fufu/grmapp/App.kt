package org.fufu.grmapp

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import org.fufu.grmapp.renderclient.BlobMap
import org.fufu.grmapp.renderclient.make_test_scene
import org.fufu.grmapp.ui.EditTabs
import org.fufu.grmapp.ui.SceneDisplay
import org.jetbrains.compose.ui.tooling.preview.Preview
import protokt.v1.grm.protobuf.RenderConfig
import protokt.v1.grm.protobuf.RenderDevice
import protokt.v1.grm.protobuf.Scene
import protokt.v1.grm.protobuf.UInt2

data class RenderSpec(
    val scene: Scene,
    val renderConfig: RenderConfig,
    val device: RenderDevice = RenderDevice.GPU,
    val blobs: BlobMap = emptyMap()
)

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
                Box(Modifier.weight(2f)){
                    SceneDisplay(renderSpec)
                }
                Box(Modifier.weight(1f)){
                    EditTabs(renderSpec){
                        renderSpec = it
                    }
                }
            }
        }
    }
}