package org.fufu.grmapp

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.util.lerp
import io.ktor.utils.io.bits.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.io.bytestring.ByteString
import org.fufu.grmapp.renderclient.ResponseTexture
import org.jetbrains.skia.*
import org.jetbrains.skiko.toBufferedImage
import protokt.v1.grm.protobuf.Texture

fun ResponseTexture.toBitmap(): Bitmap {
    val bm = Bitmap()
    val info = ImageInfo(
        ColorInfo(
            ColorType.RGB_888X,
            ColorAlphaType.OPAQUE,
            ColorSpace.sRGB
        ),
        width.toInt(),
        height.toInt()
    )
    val transformed = ByteArray((width*height).toInt()*4)
    (0..(width*height).toInt()-1).forEach{
        transformed[it*4+0] = blob[it*3+0]
        transformed[it*4+1] = blob[it*3+1]
        transformed[it*4+2] = blob[it*3+2]
        transformed[it*4+3] = 0xff.toByte()
    }
    bm.installPixels(
        info, transformed, width.toInt()*4
    )
    return bm
}

fun ResponseTexture.toImageBitmap(): ImageBitmap {
    return runBlocking {
        toImageBitmapAsync()
    }
}

suspend fun ResponseTexture.toImageBitmapAsync(): ImageBitmap {
    return withContext(Dispatchers.Default){
        ensureActive()
        val bitmap = toBitmap()
        ensureActive()
        val bufferedImage = bitmap.toBufferedImage()
        ensureActive()
        val imageBitmap = bufferedImage.toComposeImageBitmap()
        ensureActive()
        imageBitmap
    }
}

private fun Bitmap.toResponseTextureRAW_RGB(): ResponseTexture{
    val blob = ByteArray(width*height*3)
    (0..height-1).forEach { y ->
        (0..width-1).forEach { x ->
            // ARGB
            // TODO: the colorType is a LIE. The loaded image type is ARGB_8888, even if it claims to be
            // BGRA_8888. I don't know why. This might be an issue to report with the skia library.
            // for now, I'm hard-coding an expectation of ARGB_8888
            var color = getColor(x,y)
            blob[(y*width + x)*3 + 0] = ((color shr 16) and 0xFF).toByte()
            blob[(y*width + x)*3 + 1] = ((color shr 8) and 0xFF).toByte()
            blob[(y*width + x)*3 + 2] = (color and 0xFF).toByte()
        }
    }
    return ResponseTexture(
        width.toUInt(),
        height.toUInt(),
        Texture.ImageEncodingType.RAW_RGB,
        ByteString(blob)
    )
}

suspend fun Bitmap.toResponseTexture(encoding: Texture.ImageEncodingType = Texture.ImageEncodingType.RAW_RGB): ResponseTexture {
    return withContext(Dispatchers.Default) {
        when (encoding) {
            Texture.ImageEncodingType.RAW_RGB -> toResponseTextureRAW_RGB()
            else -> throw IllegalArgumentException("Unknown encoding type requested")
        }
    }
}