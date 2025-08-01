package org.fufu.grmapp.renderclient

import io.ktor.network.selector.SelectorManager
import io.ktor.network.sockets.Socket
import io.ktor.network.sockets.SocketAddress
import io.ktor.network.sockets.aSocket
import io.ktor.network.sockets.openReadChannel
import io.ktor.network.sockets.openWriteChannel
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readByteArray
import io.ktor.utils.io.readInt
import io.ktor.utils.io.writeByteArray
import io.ktor.utils.io.writeInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.io.bytestring.ByteString
import protokt.v1.grm.protobuf.BlobsHeader
import protokt.v1.grm.protobuf.RenderRequest
import protokt.v1.grm.protobuf.RenderResponse
import protokt.v1.grm.protobuf.RenderResult
import protokt.v1.grm.protobuf.Texture

typealias BlobMap = Map<UInt, ByteString>

data class ResponseTexture(
    val width: UInt,
    val height: UInt,
    val encoding: Texture.ImageEncodingType,
    val blob: ByteString
)

class RenderServer(val address: SocketAddress) {
    private val socketBuilder = aSocket(SelectorManager(Dispatchers.IO)).tcp()

    private suspend fun readBlobs(readChannel: ByteReadChannel, blobInfo: BlobsHeader): BlobMap {
        return blobInfo.blobs.associate {
            if(it.identifier == null){
                throw ResponseException("unknown blob identifier type in BlobsHeader")
            }
            it.identifier.id to ByteString(readChannel.readByteArray(it.blobLength.toInt()))
        }
    }

    private fun toResponseTexture(result: RenderResult, responseBlobs: BlobMap): ResponseTexture {
        val resultTexture = when(result.result){
            is RenderResult.Result.Error -> throw RenderException(result.result.error.reason)
            null -> throw ResponseException("Unknown render result type")
            is RenderResult.Result.Success -> result.result.success
        }
        val textureBlobIdent = resultTexture.blobIdent?.id ?: throw ResponseException("unknown blob identifier type in response texture")
        val textureBlob = responseBlobs[textureBlobIdent] ?: throw ResponseException("no blob sent for texture data")

        return ResponseTexture(
            resultTexture.width,
            resultTexture.height,
                resultTexture.encoding,
            textureBlob
            )
    }

    private suspend fun do_render(socket: Socket, renderRequest: RenderRequest, blobMap: BlobMap): ResponseTexture {
        val writeChannel = socket.openWriteChannel()
        val readChannel = socket.openReadChannel()

        val request = renderRequest.serialize()
        writeChannel.writeInt(request.size)
        writeChannel.writeByteArray(request)
        blobMap.forEach {
            writeChannel.writeByteArray(it.value.toByteArray())
        }
        writeChannel.flush()

        val responseLength = readChannel.readInt()
        val responseBytes = readChannel.readByteArray(responseLength)

        val response = RenderResponse.deserialize(responseBytes)
        if(response.blobsInfo == null){
            throw ResponseException("Response was missing blobs header")
        }
        val responseBlobs = readBlobs(readChannel, response.blobsInfo)
        return when(response.union){
            is RenderResponse.Union.InvalidRequest -> throw RequestException(response.union.invalidRequest.reason)
            null -> throw ResponseException("Unknown response type.")
            is RenderResponse.Union.Result -> toResponseTexture(response.union.result, responseBlobs)
        }
    }

    /**
     * @throws RenderException if the render failed server-side
     * @throws ResponseException if the server sent an invalid response
     * @throws RequestException if the server reported that the client request was invalid
     */
    suspend fun render(renderRequest: RenderRequest, blobMap: BlobMap): ResponseTexture {
        return withContext(Dispatchers.IO){
            val socket = socketBuilder.connect(address)
            try{
                do_render(socket, renderRequest, blobMap)
            }finally {
                socket.close()
            }
        }
    }
}