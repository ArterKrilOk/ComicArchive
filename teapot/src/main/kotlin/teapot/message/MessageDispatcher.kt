package teapot.message

interface MessageDispatcher<in M : Message> {
    fun dispatch(message: M)

    operator fun invoke(message: M) {
        dispatch(message)
    }

    companion object {
        fun <M : Message> empty() = object : MessageDispatcher<M> {
            override fun dispatch(message: M) = Unit
        }
    }
}