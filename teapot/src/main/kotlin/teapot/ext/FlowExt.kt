package teapot.ext

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.launch

internal fun <T> Flow<T>.collectIn(scope: CoroutineScope, collector: FlowCollector<T>) =
    scope.launch { collect(collector) }