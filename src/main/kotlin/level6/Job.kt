@file:JvmName("MainKt")
package level6

import level1.Continuation
import level1.resume
import level4.Dispatcher
import level4.ThreadPoolDispatcher
import level5.myDelay
import level5.scheduler

// Level 6: Job + Cancellation
//
// Сейчас корутины запускаются и работают до конца — остановить нельзя.
// Job добавляет lifecycle: Active → Cancelling → Cancelled / Completed
//
// Cancellation кооперативная — корутина сама проверяет isActive
// и бросает CancellationException если её отменили.

// ---------------------------------------------------------------------------
// TODO 1: CancellationException
class CancellationException(message: String) : Exception(message)
// ---------------------------------------------------------------------------

// ---------------------------------------------------------------------------
// TODO 2: интерфейс Job
//
interface Job {
    val isActive: Boolean
    fun cancel()
    fun invokeOnCompletion(handler: (Throwable?) -> Unit)
}
// ---------------------------------------------------------------------------

// ---------------------------------------------------------------------------
// TODO 3: реализуй SimpleJob
//
// Хранит:
//   - state: enum/sealed: Active, Cancelled, Completed
//   - список completion handlers
//
// cancel() → меняет state на Cancelled, зовёт все handlers с CancellationException
// complete() → меняет state на Completed, зовёт все handlers с null
// invokeOnCompletion() → если уже завершён — зови сразу, иначе добавь в список
// ---------------------------------------------------------------------------
class SimpleJob : Job {
    var state: JobState = JobState.Active
    val handlers: MutableList<Handler> = mutableListOf()

    override val isActive: Boolean get() = state is JobState.Active

    override fun cancel() {
        val c = CancellationException("Canceled")
        state = JobState.Cancelled(c)
        handlers.forEach { it(c) }
    }

    fun complete() {
        state = JobState.Completed
        handlers.forEach { it(null) }
    }

    override fun invokeOnCompletion(handler: Handler) {
        when (val s = state) {
            is JobState.Completed  -> handler(null)
            is JobState.Cancelled  -> handler(s.e)
            is JobState.Active     -> handlers.add(handler)
        }
    }
}

sealed class JobState {
    object Active : JobState()
    class Cancelled(val e: Exception) : JobState()
    object Completed : JobState()
}

// ---------------------------------------------------------------------------
// TODO 4: добавь проверку isActive в state machine
//
// В каждой ветке when(label) перед работой:
//   if (!job.isActive) throw CancellationException("cancelled")
//
// Оберни весь resumeWith в try/catch — при CancellationException
// зови job.cancel() и cont.resumeWith(Result.failure(e))
// ---------------------------------------------------------------------------
  class CancellableSM(val dispatcher: Dispatcher, val job: SimpleJob, val cont: Continuation<Unit>) : Continuation<Any?> {
      var label = 0
      override fun resumeWith(result: Result<Any?>) {
        try {

        
        when(label) {
            0 ->  {
                if (job.isActive.not()) throw CancellationException("Was canceled")
                label = 1
                println("step1")
                myDelay(300, dispatcher, this)
            }
            1 ->  {
                if (job.isActive.not()) throw CancellationException("Was canceled")
                label = 2
                println("step2")
                myDelay(300, dispatcher, this)
            }
            2 ->  {
                if (job.isActive.not()) throw CancellationException("Was canceled")
                label = 3
                println("step3")
                job.complete()
                cont.resume(Unit)
            }
        }
        } catch (e:CancellationException) {
            job.cancel()
            cont.resumeWith(Result.failure(e))
        }
      }
  }
// ---------------------------------------------------------------------------
// TODO 5: напиши CancellableSM — корутина с тремя myDelay(300ms)
//         принимает job: SimpleJob
//         проверяет job.isActive перед каждым шагом
// ---------------------------------------------------------------------------

// ---------------------------------------------------------------------------
// TODO 6: в main() запусти корутину, через 400ms отмени job
//         убедись что CancellationException поймана и корутина остановилась
//         после первого delay (label=1), а не дошла до конца
// ---------------------------------------------------------------------------

fun main() {
    val dispatcher = ThreadPoolDispatcher(2)
    val job = SimpleJob()
    val cont = object : Continuation<Unit> {
        override fun resumeWith(result: Result<Unit>) {
            if (result.isFailure) println("Coroutine cancelled: ${result.exceptionOrNull()}")
            else println("Coroutine completed")
        }
    }

    job.invokeOnCompletion { e ->
        if (e != null) println("Job cancelled: $e")
        else println("Job completed")
    }

    dispatcher.dispatch { CancellableSM(dispatcher, job, cont).resumeWith(Result.success(Unit)) }

    // отменяем через 400ms — после step1 (300ms) но до step2
    Thread.sleep(400)
    println("cancelling job...")
    job.cancel()

    Thread.sleep(500)
    dispatcher.shutdown()
    scheduler.shutdown()
}


typealias Handler = (Throwable?) -> Unit