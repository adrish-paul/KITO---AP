package com.kito.core.sync.domain

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/** Concurrent callers share a result; a later request may run again. */
internal class SingleFlight<K, V> {
    private val mutex = Mutex()
    private val running = mutableMapOf<K, CompletableDeferred<V>>()

    suspend fun run(key: K, block: suspend () -> V): V {
        var owner = false
        val result = mutex.withLock {
            running[key] ?: CompletableDeferred<V>().also {
                running[key] = it
                owner = true
            }
        }
        if (!owner) return result.await()
        try {
            return block().also { result.complete(it) }
        } catch (e: Throwable) {
            result.completeExceptionally(e)
            throw e
        } finally {
            withContext(NonCancellable) {
                mutex.withLock { running.remove(key) }
            }
        }
    }
}
