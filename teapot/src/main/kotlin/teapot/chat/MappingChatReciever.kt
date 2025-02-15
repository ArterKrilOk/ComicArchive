package teapot.chat

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.merge
import teapot.message.Message

inline fun <reified M : Message> mappingChatReceiver(
    crossinline mapper: (message: Message) -> M?,
    vararg chat: Chat,
) = object : ChatReceiver<M> {
    override val messagesFlow: Flow<M> =
        chat.map { it.messageFlow }.merge().mapNotNull(mapper).filterIsInstance()

    override val chats: Set<Chat> = chat.toSet()
}