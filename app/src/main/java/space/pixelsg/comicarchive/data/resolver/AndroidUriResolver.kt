package space.pixelsg.comicarchive.data.resolver

import android.content.Context
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import space.pixelsg.comicarchive.models.UriMeta
import java.io.InputStream

class AndroidUriResolver(
    context: Context,
) : UriResolver {
    private val resolver = context.contentResolver

    override suspend fun <T> openUri(uri: String, use: suspend (InputStream?) -> T): T =
        resolver.openInputStream(Uri.parse(uri)).use { use(it) }

    override suspend fun <T> openUriDescriptor(
        uri: String,
        use: suspend (ParcelFileDescriptor?) -> T
    ): T =
        resolver.openFileDescriptor(Uri.parse(uri), "r").use { use(it) }

    override suspend fun uriMetadata(uri: String): UriMeta {
        // Get mimetype
        val mime = resolver.getType(Uri.parse(uri))
        var size: Long? = null
        var name: String? = null
        // Get uri cursor
        resolver.query(Uri.parse(uri), null, null, null, null)?.use { cursor ->
            cursor.moveToFirst()
            // Get size column
            val sizeColumn = cursor.getColumnIndex(OpenableColumns.SIZE)
            if (sizeColumn >= 0) size = cursor.getLong(sizeColumn)
            // Get name column
            val nameColumn = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameColumn >= 0) name = cursor.getString(nameColumn)
        }
        return UriMeta(mime, size, name)

    }
}