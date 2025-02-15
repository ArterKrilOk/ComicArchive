package space.pixelsg.comicarchive.data.resolver

import android.os.ParcelFileDescriptor
import space.pixelsg.comicarchive.models.UriMeta
import java.io.InputStream

interface UriResolver {
    suspend fun <T> openUri(uri: String, use: suspend (InputStream?) -> T): T
    suspend fun <T> openUriDescriptor(uri: String, use: suspend (ParcelFileDescriptor?) -> T): T
    suspend fun uriMetadata(uri: String): UriMeta
}