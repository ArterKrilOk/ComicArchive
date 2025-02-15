package space.pixelsg.comicarchive.utils

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock


class ArgumentLock<T> {
    private val locks = mutableMapOf<T, Mutex>()

    suspend fun <R> withLock(arg: T, block: suspend () -> R): R {
        val mutex = synchronized(locks) {
            locks.getOrPut(arg) { Mutex() }
        }

        return mutex.withLock {
            try {
                block()
            } finally {
                synchronized(locks) {
                    if (!mutex.isLocked) locks.remove(arg)
                }
            }
        }
    }
}