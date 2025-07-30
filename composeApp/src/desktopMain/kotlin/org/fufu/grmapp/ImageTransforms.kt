package org.fufu.grmapp

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageBitmapConfig
import androidx.compose.ui.graphics.colorspace.ColorSpaces
import androidx.compose.ui.graphics.toPixelMap
import org.fufu.grmapp.renderclient.ResponseTexture
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.ColorInfo
import org.jetbrains.skia.ColorSpace
import org.jetbrains.skia.ColorType
import org.jetbrains.skia.ImageInfo
import org.jetbrains.skiko.toBufferedImage
import androidx.compose.ui.graphics.toComposeImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

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