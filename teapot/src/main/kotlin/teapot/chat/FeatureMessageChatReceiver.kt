package teapot.chat

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.merge
import teapot.message.Message

inline fun <reified M : Message> featureMessageChatReceiver(
    vararg chat: Chat,
): ChatReceiver<M> = object : ChatReceiver<M> {
    override val messagesFlow: Flow<M> =
        chat.map { it.messageFlow }.merge().filterIsInstance()

    override val chats: Set<Chat> = chat.toSet()
}