package teapot.chat

import kotlinx.coroutines.flow.Flow
import teapot.message.Message

/**
 * Main feature to feature communication method.
 * Features can read chat messages and react to them.
 *
 * To use Chat inside feature see [ChatReceiver]
 */
interface Chat {
    val messageFlow: Flow<Message>
    suspend fun send(message: Message)
}