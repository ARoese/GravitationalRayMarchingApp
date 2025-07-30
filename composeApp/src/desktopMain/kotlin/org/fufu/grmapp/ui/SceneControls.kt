package org.fufu.grmapp.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import protokt.v1.grm.protobuf.Body
import protokt.v1.grm.protobuf.Camera
import protokt.v1.grm.protobuf.Float2
import protokt.v1.grm.protobuf.Float3
import protokt.v1.grm.protobuf.Material
import protokt.v1.grm.protobuf.Scene

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
fun EditableBody(state: Body, onChange: (Body) -> Unit){
    Column {
        Text("Position:")
        EditableFloat3(state.position){
            onChange(state.copy { position = it })
        }
        Text("Rotation:")
        EditableFloat3(state.rotation){
            onChange(state.copy { rotation = it })
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)){
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally){
                Text("Mass")
                FloatField(state.mass){
                    onChange(state.copy { mass = it })
                }
            }
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally){
                Text("Radius")
                FloatField(state.radius){
                    onChange(state.copy { radius = it })
                }
            }
        }
    }
}

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

@Composable
fun EditableBodyList(state: List<Body>, onChange: (List<Body>) -> Unit){
    Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(6.dp)){
        state.forEachIndexed { i, body ->
            EditableBody(body){
                onChange(state.subList(0, i) + it + state.subList(i+1, state.size))
            }
            if(i != state.size-1){
                HorizontalDivider(thickness = 4.dp, color = Color.Blue)
            }
        }
    }
}

@Composable
fun EditTabs(
    scene: Scene,
    onChangeScene: (Scene) -> Unit
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
        }
        when(tabIndex){
            0 -> EditableCamera(scene.cam){
                onChangeScene(scene.copy { cam = it })
            }
            1 -> EditableBodyList(scene.bodies){
                onChangeScene(scene.copy { bodies = it })
            }
        }
    }

}