package space.pixelsg.comicarchive.ext

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

fun <T> MutableStateFlow<List<T>>.updateItemByPredicate(
    predicate: (T) -> Boolean,
    update: (T) -> T,
) {
    update { items -> items.updateByPredicate(predicate, update) }
}