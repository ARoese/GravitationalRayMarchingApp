package org.fufu.grmapp.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import org.fufu.grmapp.RenderSpec
import org.fufu.grmapp.renderclient.BlobMap
import protokt.v1.grm.protobuf.Material
import protokt.v1.grm.protobuf.RenderDevice
import protokt.v1.grm.protobuf.UInt2

@Composable
fun SkyboxTab(
    material: Material?,
    blobs: BlobMap,
    onChange: (Material, BlobMap) -> Unit
){
    Column{
        Text("Skybox Material:")
        Row(horizontalArrangement = Arrangement.Center){
            EditableMaterial(material, blobs, onChange)
        }
    }
}

@Composable
fun EditTabs(
    renderSpec: RenderSpec,
    onChange: (RenderSpec) -> Unit
){
    Column {
        var tabIndex by remember { mutableStateOf(0) }
        TabRow(selectedTabIndex = tabIndex){
            Tab(
                selected = tabIndex == 0,
                onClick = {tabIndex = 0},
                text={Text("Camera")}
            )
            Tab(
                selected = tabIndex == 1,
                onClick = {tabIndex = 1},
                text={Text("Bodies")}
            )
            Tab(
                selected = tabIndex == 2,
                onClick = {tabIndex = 2},
                text={Text("Render")}
            )
            Tab(
                selected = tabIndex == 3,
                onClick = {tabIndex = 3},
                text={Text("Skybox")}
            )
        }
        when(tabIndex){
            0 -> EditableCamera(renderSpec.scene.cam){
                onChange(renderSpec.copy(
                    scene = renderSpec.scene.copy { cam = it }
                ))
            }
            1 -> EditableBodyList(BodiesBlobs(renderSpec.scene.bodies, renderSpec.blobs)){
                onChange(renderSpec.copy(
                    scene = renderSpec.scene.copy { bodies = it.bodies },
                    blobs = it.blobs
                ))
            }
            2 -> {
                EditableRenderConfig(renderSpec.renderConfig){
                    onChange(renderSpec.copy(renderConfig = it))
                }
                Text("Render Device:")
                RenderDevicePicker(renderSpec.device){
                    if(it == RenderDevice.CPU){
                        onChange(renderSpec.copy(
                            device = it,
                            renderConfig = renderSpec.renderConfig.copy { resolution = UInt2{ x = 64.toUInt(); y = 64.toUInt() } })
                        )
                    }else{
                        onChange(renderSpec.copy(device = it))
                    }
                }
            }
            3 -> SkyboxTab(renderSpec.scene.nohit, renderSpec.blobs){ material, blobs ->
                onChange(
                    renderSpec.copy(
                        scene = renderSpec.scene.copy { nohit = material },
                        blobs = blobs
                    )
                )
            }
        }
    }
}