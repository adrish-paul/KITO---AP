package com.kito.core.sync

import com.kito.core.sync.domain.SingleFlight
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class SingleFlightTest {
    @Test
    fun overlappingRequestsShareFailureAndLaterCallsRetry() = runTest {
        val flight = SingleFlight<String, Result<Unit>>()
        val gate = CompletableDeferred<Unit>()
        var calls = 0
        val first = async { flight.run("same") { calls++; gate.await(); Result.failure<Unit>(IllegalStateException("offline")) } }
        runCurrent()
        val second = async { flight.run("same") { calls++; Result.success(Unit) } }
        runCurrent()
        gate.complete(Unit)
        assertTrue(first.await().isFailure)
        assertTrue(second.await().isFailure)
        assertEquals(1, calls)
        assertTrue(flight.run("same") { calls++; Result.success(Unit) }.isSuccess)
        assertEquals(2, calls)
    }

    @Test
    fun cancellingWaiterDoesNotCancelOwnerAndDifferentKeysStaySeparate() = runTest {
        val flight = SingleFlight<String, Int>()
        val gate = CompletableDeferred<Unit>()
        val owner = async { flight.run("a") { gate.await(); 1 } }
        runCurrent()
        val waiter = async { flight.run("a") { error("must join owner") } }
        runCurrent()
        waiter.cancelAndJoin()
        assertEquals(2, flight.run("b") { 2 })
        gate.complete(Unit)
        assertEquals(1, owner.await())
    }
}
