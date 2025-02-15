package teapot.chat

import kotlinx.coroutines.flow.Flow
import teapot.message.Message

/**
 * Describes how [Feature] should react to specific [Chat] message and how to convert
 * unknown to feature [Message] to its message type [M].
 *
 * See [featureMessageChatReceiver] and [mappingChatReceiver] for specific realisation.
 *
 * Note: One feature can have multiple chatReceivers and one chat receiver can have multiple chats.
 */
interface ChatReceiver<out M : Message> {
    val messagesFlow: Flow<M>
    val chats: Set<Chat>
}