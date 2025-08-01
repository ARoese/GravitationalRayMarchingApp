package org.fufu.grmapp.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import protokt.v1.grm.protobuf.MarchConfig
import protokt.v1.grm.protobuf.RenderConfig
import protokt.v1.grm.protobuf.RenderDevice
import protokt.v1.grm.protobuf.UInt2

@Composable
fun EditableMarchConfig(state: MarchConfig?, onChange: (MarchConfig) -> Unit){
    var show = state
    if(show == null){
        show = MarchConfig{
            marchSteps = 10000.toUInt()
            marchStepDeltaTime = 0.05f
        }
        onChange(show)
    }
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)){
        Column (Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally){
            Text("steps")
            NumberField(show.marchSteps.toInt()){
                onChange(show.copy { marchSteps = it.toUInt() })
            }
        }
        Column (Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally){
            Text("step dt")
            FloatField(show.marchStepDeltaTime){
                onChange(show.copy { marchStepDeltaTime = it })
            }
        }
    }
}

@Composable
fun RenderDevicePicker(state: RenderDevice, onChange: (RenderDevice) -> Unit){
    SingleChoiceSegmentedButtonRow {
        SegmentedButton(
            shape = SegmentedButtonDefaults.itemShape(
                index = 0,
                count = 2
            ),
            onClick = {
                if(state != RenderDevice.CPU){
                    onChange(RenderDevice.CPU)
                }
            },
            selected = state == RenderDevice.CPU,
            label = { Text("CPU") }
        )
        SegmentedButton(
            shape = SegmentedButtonDefaults.itemShape(
                index = 1,
                count = 2
            ),
            onClick = {
                if(state != RenderDevice.GPU){
                    onChange(RenderDevice.GPU)
                }
            },
            selected = state == RenderDevice.GPU,
            label = { Text("GPU") }
        )
    }
}

@Composable
fun EditableRenderConfig(state: RenderConfig, onChange: (RenderConfig) -> Unit){
    Column {
        Text("Resolution:")
        EditableUInt2(state.resolution, default = UInt2{ x = 64.toUInt(); y = 64.toUInt() }){
            onChange(state.copy { resolution = it })
        }
        Text("March Config:")
        EditableMarchConfig(state.marchConfig){
            onChange(state.copy { marchConfig = it })
        }
    }
}