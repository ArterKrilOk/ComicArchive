package space.pixelsg.comicarchive.ext

fun <T> List<T>.updateByPredicate(
    predicate: (T) -> Boolean,
    update: (T) -> T,
): List<T> = map { item ->
    if (predicate(item)) update(item)
    else item
}