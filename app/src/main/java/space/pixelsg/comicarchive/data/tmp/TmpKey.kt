package space.pixelsg.comicarchive.data.tmp

object TmpKey {
    fun createFromUriAndPage(uri: String, page: String) = "$uri/$page"
    fun createFromUriAndSource(uri: String, source: String) = "$uri/$source"
}