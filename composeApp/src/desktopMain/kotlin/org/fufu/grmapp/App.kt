package org.fufu.grmapp

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview

import io.ktor.network.sockets.InetSocketAddress
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.withContext
import org.fufu.grmapp.renderclient.RenderServer
import org.fufu.grmapp.renderclient.make_test_scene
import org.fufu.grmapp.ui.EditTabs
import org.fufu.grmapp.ui.EditableBodyList
import org.fufu.grmapp.ui.SceneDisplay
import protokt.v1.grm.protobuf.Body
import protokt.v1.grm.protobuf.Float3
import protokt.v1.grm.protobuf.RenderConfig
import protokt.v1.grm.protobuf.RenderDevice
import protokt.v1.grm.protobuf.RenderRequest
import protokt.v1.grm.protobuf.UInt2

@Composable
@Preview
fun App() {
    MaterialTheme {
        var scene by remember { mutableStateOf(make_test_scene()) }

        Box(Modifier
            .safeContentPadding()
            .fillMaxSize()
        ){
            Row {
                Box(Modifier.weight(2f)){
                    SceneDisplay(scene)
                }
                Box(Modifier.weight(1f)){
                    EditTabs(
                        scene,
                        onChangeScene = {
                            scene = it
                        }
                    )
                }
            }
        }
    }
}