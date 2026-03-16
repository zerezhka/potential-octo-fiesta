@file:JvmName("MainKt")
package level4

import level1.Continuation
import level1.resume
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.ExecutorService

annotation class Suspend

// TODO 1: объяви interface Dispatcher с одним методом dispatch(block: Runnable)
interface Dispatcher {
    fun dispatch(block: Runnable)
}

// TODO 2: реализуй DirectDispatcher — запускает block сразу на текущем потоке (для тестов)
object Unconfined : Dispatcher {
    override fun dispatch(block: Runnable) {
        block.run()
    }
}

// TODO 3: реализуй ThreadPoolDispatcher(threads: Int) — оборачивает Executors.newFixedThreadPool
//         добавь метод shutdown()
class ThreadPoolDispatcher(threads: Int) : Dispatcher {

    private val executor: ExecutorService = Executors.newFixedThreadPool(threads)

    override fun dispatch(block: Runnable) {
        executor.execute(block)
    }
    fun shutdown() {
        executor.shutdown()
    }
}

// TODO 4: реализуй myLaunch(dispatcher: Dispatcher, block: (Continuation<Unit>) -> Unit)
//         каждый resumeWith должен диспатчить следующий шаг через dispatcher
fun myLaunch(dispatcher: Dispatcher, block: (Continuation<Unit>) -> Unit) {
    val cont = object : Continuation<Unit> {
        override fun resumeWith(result: Result<Unit>) {}
    }
    dispatcher.dispatch { block(cont) }
}

// --- тестовый примитив, не трогай ---
@Suspend
fun loggedStep(name: String, cont: Continuation<String>) {
    println("[$name] on thread: ${Thread.currentThread().name}")
    cont.resume(name)
}

// TODO 5: запусти две корутины через ThreadPoolDispatcher(2), каждая делает два loggedStep
//         в конце shutdown + awaitTermination
// State machine которая делает два loggedStep через dispatcher
class TwoStepSM(val dispatcher: Dispatcher, val cont: Continuation<Unit>) : Continuation<Any?> {
    var label = 0

    override fun resumeWith(result: Result<Any?>) {
        if (result.isFailure) { cont.resumeWith(Result.failure(result.exceptionOrNull()!!)); return }
        when (label) {
            0 -> { label = 1; dispatcher.dispatch { loggedStep("first", this) } }
            1 -> { label = 2; dispatcher.dispatch { loggedStep("second", this) } }
            2 -> { cont.resume(Unit) }
        }
    }
}

fun main() {
    val dispatcher = ThreadPoolDispatcher(2)

    myLaunch(dispatcher) { cont -> TwoStepSM(dispatcher, cont).resumeWith(Result.success(Unit)) }
    myLaunch(dispatcher) { cont -> TwoStepSM(dispatcher, cont).resumeWith(Result.success(Unit)) }

    Thread.sleep(500)
    dispatcher.shutdown()
}
