package space.pixelsg.comicarchive.ui.root

import android.content.Intent
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch
import teapot.chat.SharedFlowChat
import teapot.message.Message

class ContentMessenger : SharedFlowChat() {
    sealed interface Msg : Message {
        data class GetContent(val mime: List<String>) : Msg
        data class GetContentResultUri(val uri: String?) : Msg

        data class GetContents(val mime: List<String>) : Msg
        data class GetContentsResults(val uris: List<String>) : Msg
    }


    fun ComponentActivity.bindToActivity() {
        fun requestPersistentPermission(uri: Uri) {
            val contentResolver = applicationContext.contentResolver
            val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION
            contentResolver.takePersistableUriPermission(uri, takeFlags)
        }

        val openDocumentLauncher =
            registerForActivityResult(ActivityResultContracts.OpenDocument()) {
                if (it != null) requestPersistentPermission(it)

                lifecycleScope.launch {
                    send(Msg.GetContentResultUri(it?.toString()))
                }
            }

        val openMultipleDocumentsLauncher =
            registerForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) {
                val uris = it.map { uri ->
                    requestPersistentPermission(uri)
                    uri.toString()
                }
                lifecycleScope.launch {
                    send(Msg.GetContentsResults(uris))
                }
            }

        lifecycleScope.launch {
            messageFlow.filterIsInstance<Msg>().collect {
                when (it) {
                    is Msg.GetContent ->
                        openDocumentLauncher.launch(it.mime.toTypedArray())

                    is Msg.GetContents ->
                        openMultipleDocumentsLauncher.launch(it.mime.toTypedArray())

                    else -> Unit
                }
            }
        }
    }
}