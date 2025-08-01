package org.fufu.grmapp.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import protokt.v1.grm.protobuf.Camera

@Composable
fun EditableCamera(state: Camera?, onChange: (Camera) -> Unit){
    var realState = state
    if(realState == null){
        realState = Camera {}
        onChange(realState)
    }
    Column {
        Text("Position:")
        EditableFloat3(realState.camPos){
            onChange(realState.copy { camPos = it })
        }
        Text("Rotation:")
        EditableFloat3(realState.camRot){
            onChange(realState.copy { camRot = it })
        }
        Text("FOV:")
        EditableFloat2(realState.fov){
            onChange(realState.copy { fov = it })
        }
    }
}