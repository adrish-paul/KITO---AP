package com.kito.testing

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler

object TestDispatchers {
    fun standard(scheduler: TestCoroutineScheduler = TestCoroutineScheduler()): CoroutineDispatcher =
        StandardTestDispatcher(scheduler)
}
