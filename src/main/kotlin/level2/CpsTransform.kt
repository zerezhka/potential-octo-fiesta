@file:JvmName("MainKt")
package level2

import level1.Continuation
import level1.resume
import level1.resumeWithException

// Level 2: Manual CPS transformation
//
// Вот "нормальный" код с suspend-функциями (псевдокод, не компилируется):
//
//   suspend fun fetchUser(id: Int): String { return "User#$id" }
//   suspend fun fetchProfile(user: String): String { return "Profile of $user" }
//   suspend fun fetchFriends(user: String): List<String> { return listOf("Alice", "Bob") }
//
//   suspend fun showProfile(id: Int) {
//       val user    = fetchUser(id)       // точка приостановки 1
//       val profile = fetchProfile(user)  // точка приостановки 2
//       val friends = fetchFriends(user)  // точка приостановки 3
//       println("$profile | friends: $friends")
//   }
//
// Задача: переписать showProfile в CPS вручную.
// Каждая точка приостановки → отдельный Continuation.
// Результат каждого шага передаётся в следующий через continuation.resume().
//
// Правила:
//   - никакого return для передачи результата
//   - никаких вложенных suspend-вызовов напрямую
//   - только continuation.resume() / continuation.resumeWithException()

// ---------------------------------------------------------------------------
// Примитивы — уже реализованы, трогать не нужно
// ---------------------------------------------------------------------------

annotation class Suspend

@Suspend
fun fetchUser(id: Int, cont: Continuation<String>) {
    cont.resume("User#$id")
}

@Suspend
fun fetchProfile(user: String, cont: Continuation<String>) {
    if (user.isEmpty()) cont.resumeWithException(IllegalArgumentException("empty user"))
    else cont.resume("Profile of $user")
}

@Suspend
fun fetchFriends(user: String, cont: Continuation<List<String>>) {
    cont.resume(listOf("Alice", "Bob"))
}

// ---------------------------------------------------------------------------
// TODO: реализуй showProfile в CPS стиле
//
// Сигнатура меняется: вместо suspend fun showProfile(id: Int)
// становится      fun showProfile(id: Int, cont: Continuation<Unit>)
//
// Внутри три вложенных continuation:
//   1. получили user    → запускаем fetchProfile
//   2. получили profile → запускаем fetchFriends
//   3. получили friends → println + cont.resume(Unit)
//
// Если на любом шаге ошибка → cont.resumeWithException(e)
// ---------------------------------------------------------------------------

fun showProfile(id: Int, cont: Continuation<Unit>) {
    fetchUser(id, object : Continuation<String> {
        override fun resumeWith(result: Result<String>) {
            if (result.isFailure) {
                cont.resumeWithException(result.exceptionOrNull()!!)
                return
            }
            val user = result.getOrThrow()
            fetchProfile(user, object : Continuation<String> {
                override fun resumeWith(result: Result<String>) {
                    if (result.isFailure) {
                        cont.resumeWithException(result.exceptionOrNull()!!)
                        return
                    }
                    val profile = result.getOrThrow()
                    fetchFriends(user, object : Continuation<List<String>> {
                        override fun resumeWith(result: Result<List<String>>) {
                            if (result.isFailure) {
                                cont.resumeWithException(result.exceptionOrNull()!!)
                                return
                            }
                            val friends = result.getOrThrow()
                            println("$profile | friends: $friends")
                            cont.resume(Unit)
                        }
                    })
                }
            })
        }
    })
}
// ---------------------------------------------------------------------------
// TODO: вызови showProfile отсюда с id=1
// ---------------------------------------------------------------------------
fun main() {
    showProfile(1, object : Continuation<Unit> {
        override fun resumeWith(result: Result<Unit>) {
            if (result.isFailure) println("Error: ${result.exceptionOrNull()}")
        }
    })
}
