package org.fufu.grmapp.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import coil3.Bitmap
import coil3.BitmapImage
import coil3.Image
import coil3.request.ImageRequest
import com.github.skydoves.colorpicker.compose.BrightnessSlider
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.openFilePicker
import io.github.vinceglb.filekit.readBytes
import io.ktor.utils.io.bits.lowByte
import io.ktor.utils.io.bits.lowShort
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.fufu.grmapp.renderclient.BlobMap
import org.fufu.grmapp.renderclient.ResponseTexture
import org.fufu.grmapp.toImageBitmapAsync
import org.fufu.grmapp.toResponseTexture
import org.jetbrains.skia.Data
import protokt.v1.grm.protobuf.BlobIdentifier
import protokt.v1.grm.protobuf.Body
import protokt.v1.grm.protobuf.Material
import protokt.v1.grm.protobuf.Texture
import protokt.v1.grm.protobuf.UInt3
import kotlin.collections.plus

data class BodiesBlobs(val bodies: List<Body>, val blobs: BlobMap)

fun Color.toUInt3(): UInt3 {
    return UInt3{
        z = toArgb().lowShort.lowByte.toUByte().toUInt()
        y = (toArgb() shr 8).lowShort.lowByte.toUByte().toUInt()
        x = (toArgb() shr 16).lowShort.lowByte.toUByte().toUInt()
    }
}

fun UInt3.toColor(): Color{
    // 0xAARRGGBB
    return Color(
        0xFF shl 24
                or (x.toInt() shl 16)
                or (y.toInt() shl 8)
                or z.toInt()
    )
}

fun BlobMap.nextAvailableKey(): UInt {
    return (keys.maxOrNull() ?: 0.toUInt()) + 1.toUInt()
}

fun ResponseTexture.toTextureMaterial(blobId: UInt): Material{
    return Material{
        shader = Material.Shader.Texture(
            Texture{
                width = this@toTextureMaterial.width
                height = this@toTextureMaterial.height
                encoding = this@toTextureMaterial.encoding
                blobIdent = BlobIdentifier{id = blobId}
            }
        )
    }
}

@Composable
fun EditableTextureMaterial(
    textureMaterial: Material.Shader.Texture,
    blobs: BlobMap,
    onChange: (Material, BlobMap) -> Unit
){
    var showPicker by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var image by remember { mutableStateOf<ImageBitmap?>(null) }
    Box(
        Modifier
            .fillMaxSize()
            .border(2.dp, Color.Black)
            .clickable(){showPicker = true}
    ){

        val currentImage = image
        if(currentImage == null){
            scope.launch {
                val texture = textureMaterial.texture
                val blob = texture.blobIdent?.id?.let { blobs[it] }
                if(blob == null){
                    onChange(
                        Material{
                            shader = Material.Shader.Color(
                                UInt3{
                                    x = 0.toUInt()
                                    y = 0.toUInt()
                                    z = 0.toUInt()
                                }
                            )
                        },
                        blobs
                    )
                    return@launch
                }
                val rt = ResponseTexture(
                    texture.width,
                    texture.height,
                    texture.encoding,
                    blob
                    )
                image = rt.toImageBitmapAsync()
            }
        }else{
            Image(currentImage, "material preview", contentScale = ContentScale.Fit)
        }
    }

    if(showPicker){
        Popup(
            onDismissRequest = { showPicker = false }
        ){
            Surface(
                Modifier
                    .width(200.dp)
                    .height(300.dp)
                    .border(2.dp, Color.Black)
            ){
                val controller = rememberColorPickerController()
                controller.debounceDuration = 100
                Column {
                    Button(
                        onClick = {
                            onChange(
                                Material{
                                    shader = Material.Shader.Color(UInt3{
                                        x = 0.toUInt()
                                        y = 0.toUInt()
                                        z = 0.toUInt()
                                    })
                                }, blobs
                            )
                        }
                    ){
                        Text("Use Color")
                    }
                    ImageSelectorButton(scope){
                        val nextKey = blobs.nextAvailableKey()
                        onChange(
                            it.toTextureMaterial(nextKey),
                            blobs + (nextKey to it.blob)
                        )
                        image = null
                    }
                }
            }
        }
    }
}

@Composable
fun ImageSelectorButton(
    scope: CoroutineScope,
    onSelectTexture: (ResponseTexture) -> Unit
){
    Button(
        onClick = {
            scope.launch {
                val imageFile = FileKit.openFilePicker(FileKitType.Image)
                if(imageFile == null){
                    return@launch
                }
                val rt = withContext(Dispatchers.Default){
                    val image = org.jetbrains.skia.Image.makeFromEncoded(imageFile.readBytes())
                    org.jetbrains.skia.Bitmap.makeFromImage(image).toResponseTexture()
                }

                onSelectTexture(rt)
            }
        }
    ){
        Text("Use Texture")
    }
}

@Composable
fun EditableColorMaterial(
    colorMaterial: Material.Shader.Color,
    blobs: BlobMap,
    onChange: (Material, BlobMap) -> Unit
){
    var showPicker by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    Box(
        Modifier
            .aspectRatio(1f)
            .fillMaxSize()
            .border(2.dp, Color.Black)
            .background(colorMaterial.color.toColor())
            .clickable(){showPicker = true}
    ){

    }
    if(showPicker){
        Popup(
            onDismissRequest = { showPicker = false }
        ){
            Surface(
                Modifier
                    .width(200.dp)
                    .height(300.dp)
                    .border(2.dp, Color.Black)
            ){
                val controller = rememberColorPickerController()
                controller.debounceDuration = 100
                Column {
                    HsvColorPicker(
                        Modifier.weight(6f),
                        initialColor = colorMaterial.color.toColor(),
                        controller = controller,
                        onColorChanged = {
                            onChange(Material{
                                shader = Material.Shader.Color(it.color.toUInt3())
                            }, blobs)
                        }
                    )
                    BrightnessSlider(
                        Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(10.dp)
                            .height(35.dp),
                        controller = controller
                    )
                    ImageSelectorButton(scope){
                        val nextKey = blobs.nextAvailableKey()
                        onChange(
                            it.toTextureMaterial(nextKey),
                            blobs + (nextKey to it.blob)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EditableMaterial(material: Material?, blobs: BlobMap, onChange: (Material, BlobMap) -> Unit){
    var show = material
    val defaultMaterial = Material{
        shader = Material.Shader.Color(UInt3{
            x = 0.toUInt()
            y = 0.toUInt()
            z = 0.toUInt()
        })
    }
    if(show == null){
        show = defaultMaterial
        onChange(show, blobs)
    }

    val currentShader = show.shader
    when(currentShader){
        is Material.Shader.Color -> EditableColorMaterial(currentShader, blobs, onChange)
        is Material.Shader.Texture -> EditableTextureMaterial(currentShader, blobs, onChange)
        null -> onChange(defaultMaterial, blobs)
    }
}

@Composable
fun EditableBody(body: Body, blobs: BlobMap, onDelete: () -> Unit, onChange: (Body, BlobMap) -> Unit){
    Column {
        Row{
            Spacer(Modifier.weight(1f))
            Button(
                onClick = onDelete
            ){
                Text("Delete")
            }
        }
        Text("Position:")
        EditableFloat3(body.position){
            onChange(body.copy { position = it }, blobs)
        }
        Text("Rotation:")
        EditableFloat3(body.rotation){
            onChange(body.copy { rotation = it }, blobs)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)){
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally){
                Text("Mass")
                FloatField(body.mass){
                    onChange(body.copy { mass = it }, blobs)
                }
            }
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally){
                Text("Radius")
                FloatField(body.radius){
                    onChange(body.copy { radius = it }, blobs)
                }
            }
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally){
                Text("Material")
                EditableMaterial(body.material, blobs){ newMat, blobs ->
                    onChange(body.copy { material = newMat }, blobs)
                }
            }
        }
    }
}

@Composable
fun EditableBodyList(state: BodiesBlobs, onChange: (BodiesBlobs) -> Unit){
    Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(6.dp)){
        state.bodies.forEachIndexed { i, body ->
            EditableBody(
                body,
                state.blobs,
                onDelete = {
                    val bodies = state.bodies.subList(0, i) + state.bodies.subList(i+1, state.bodies.size)
                    onChange(BodiesBlobs(bodies, state.blobs))
                }
            ){ body, blobs ->
                val bodies = state.bodies.subList(0, i) + body + state.bodies.subList(i+1, state.bodies.size)
                onChange(BodiesBlobs(bodies, blobs))
            }
            if(i != state.bodies.size-1){
                HorizontalDivider(thickness = 4.dp, color = Color.Blue)
            }
        }
        Button(
            onClick = {
                val bodies = state.bodies + Body{
                    material = Material{
                        shader = Material.Shader.Color(UInt3{})
                    }
                }
                onChange(BodiesBlobs(bodies, state.blobs))
            }
        ){
            Text("Add new body")
        }
    }
}