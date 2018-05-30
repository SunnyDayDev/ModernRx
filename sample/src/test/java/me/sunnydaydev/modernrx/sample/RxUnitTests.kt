package me.sunnydaydev.modernrx.sample

import io.reactivex.Completable
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class RxUnitTests {

    @Test
    fun `Exception inside Rx-create not throw global exception, just handle it`() {

        var errorHappen = false

        try {

            var errorHandled = false

            Completable.create {
                throw RuntimeException("Ha")
            }.subscribe({}, {
                errorHandled = true
            })

            assertTrue("Oh no! onError not called!", errorHandled)

        } catch (e: Throwable) {

            errorHappen = true

        }

        assertFalse("Oh no! Error happen.", errorHappen)

    }

    @Test
    fun `Exception inside Rx-unsafeCreate throw global exception without handling`() {

        var errorHappen = false
        var errorHandled = false

        try {

            Completable.unsafeCreate {
                throw RuntimeException("Ha")
            }.subscribe({}, {
                errorHandled = true
            })


        } catch (e: Throwable) {

            errorHappen = true

        }

        assertFalse("Oh no! onError called!", errorHandled)
        assertTrue("Oh no! Error not happen.", errorHappen)

    }

}
