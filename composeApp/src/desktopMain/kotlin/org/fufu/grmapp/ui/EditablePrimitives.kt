package org.fufu.grmapp.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import protokt.v1.grm.protobuf.Float2
import protokt.v1.grm.protobuf.Float3
import protokt.v1.grm.protobuf.UInt2

@Composable
fun NumberField(contents: Int, onValueChange: (Int) -> Unit){
    var actualStringContent by remember { mutableStateOf(contents.toString()) }
    TextField(
        actualStringContent,
        keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = KeyboardType.Number
        ),
        onValueChange = {
            actualStringContent = it.filter { it.isDigit() }
            actualStringContent.toIntOrNull()?.let(onValueChange)
        }
    )
}

@Composable
fun FloatField(contents: Float, onValueChange: (Float) -> Unit){
    var actualStringContent by remember { mutableStateOf(contents.toString()) }
    TextField(
        actualStringContent,
        keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = KeyboardType.Number
        ),
        onValueChange = {
            actualStringContent = it.filter { it.isDigit() || it == '.' || it == '-' || it == 'e' }
            actualStringContent.toFloatOrNull()?.let(onValueChange)
        }
    )
}

@Composable
fun EditableFloat3(state: Float3?, onChange: (Float3) -> Unit){
    var show = state
    if(show == null){
        show = Float3{x = 0f; y = 0f; z = 0f}
        onChange(show)
    }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)){
        Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally){
            Text("x")
            FloatField(show.x){
                onChange(show.copy { x = it })
            }
        }
        Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("y")
            FloatField(show.y){
                onChange(show.copy { y = it })
            }
        }
        Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally){
            Text("z")
            FloatField(show.z){
                onChange(show.copy { z = it })
            }
        }
    }
}

@Composable
fun EditableFloat2(state: Float2?, onChange: (Float2) -> Unit){
    var show = state
    if(show == null){
        show = Float2{x = 0f; y = 0f}
        onChange(show)
    }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)){
        Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally){
            Text("x")
            FloatField(show.x){
                onChange(show.copy { x = it })
            }
        }
        Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("y")
            FloatField(show.y){
                onChange(show.copy { y = it })
            }
        }
    }
}

@Composable
fun EditableUInt2(state: UInt2?, default: UInt2 = UInt2{x = 0.toUInt(); y = 0.toUInt()}, onChange: (UInt2) -> Unit){
    var show = state
    if(show == null){
        show = default
        onChange(show)
    }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)){
        Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally){
            Text("x")
            NumberField(show.x.toInt()){
                onChange(show.copy { x = it.toUInt() })
            }
        }
        Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("y")
            NumberField(show.y.toInt()){
                onChange(show.copy { y = it.toUInt() })
            }
        }
    }
}