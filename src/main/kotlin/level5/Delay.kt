@file:JvmName("MainKt")
package level5

import level1.Continuation
import level1.resume
import level4.Dispatcher
import level4.ThreadPoolDispatcher
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

// Level 5: myDelay() без Thread.sleep
//
// Thread.sleep(ms) блокирует поток — поток ничего не делает но занят.
// Если 1000 корутин делают sleep(1000ms) — нужно 1000 потоков.
//
// myDelay должен:
//   1. Приостановить корутину (не вызывать continuation прямо сейчас)
//   2. Через ms миллисекунд — возобновить через dispatcher
//   3. Поток пула в это время свободен и делает другую работу
//
// Для этого нужен ScheduledExecutorService — он умеет откладывать задачи.

// ---------------------------------------------------------------------------
// TODO 1: реализуй myDelay
//
val scheduler = Executors.newSingleThreadScheduledExecutor()

fun myDelay(ms: Long, dispatcher: Dispatcher, cont: Continuation<Unit>) {
    scheduler.schedule(
        { dispatcher.dispatch { cont.resume(Unit) } },
        ms,
        TimeUnit.MILLISECONDS
    )
}

class TwoDelaysSM(val dispatcher: Dispatcher, val cont: Continuation<Unit>) : Continuation<Any?> {
    var label = 0

    override fun resumeWith(result: Result<Any?>) {
    when (label) {
            0 -> { label = 1; println("start");             myDelay(500, dispatcher, this) }
            1 -> { label = 2; println("after first delay"); myDelay(500, dispatcher, this) }
            2 -> { println("done"); cont.resume(Unit) }
        }
    }
}

// TODO 3: в main запусти две такие корутины параллельно через ThreadPoolDispatcher(2)
//         замерь время — должно быть ~500ms, а не ~1000ms
//         (две корутины не блокируют друг друга)

fun main() {
    val dispatcher = ThreadPoolDispatcher(2)
    val done = object : Continuation<Unit> { override fun resumeWith(result: Result<Unit>) {} }

    val start = System.currentTimeMillis()
    dispatcher.dispatch { TwoDelaysSM(dispatcher, done).resumeWith(Result.success(Unit)) }
    dispatcher.dispatch { TwoDelaysSM(dispatcher, done).resumeWith(Result.success(Unit)) }

    Thread.sleep(2000)
    println("total: ${System.currentTimeMillis() - start}ms")
    dispatcher.shutdown()
    scheduler.shutdown()
}
