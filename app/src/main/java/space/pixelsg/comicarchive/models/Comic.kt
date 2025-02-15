package space.pixelsg.comicarchive.models

data class Comic(
    val id: Long,
    val info: ComicInfo,
    val name: String,
    val uri: String,
)
