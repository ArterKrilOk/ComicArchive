package teapot.chat

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import teapot.message.Message

inline fun <reified M : Message> sendOnlyChatReceiver(
    vararg chat: Chat,
) = object : ChatReceiver<M> {
    override val messagesFlow: Flow<M> = emptyFlow()

    override val chats: Set<Chat> = chat.toSet()
}