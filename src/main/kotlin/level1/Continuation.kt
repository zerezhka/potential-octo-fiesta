@file:JvmName("MainKt")
package level1

// Level 1: Continuation — the fundamental building block
//
// A Continuation<T> is a callback that represents "the rest of the computation".
// When a coroutine suspends, it hands a Continuation to whoever will resume it.
// That entity calls resumeWith(result) when it's ready — with either a value or an exception.

annotation class Suspend

// TODO 1: интерфейс уже дан — прочитай и пойми каждую строчку.
interface Continuation<in T> {
    fun resumeWith(result: Result<T>)
}
fun <T> Continuation<T>.resume(value: T) = resumeWith(Result.success(value))
// TODO 2: исправь баг в этом extension — он рекурсивно зовёт сам себя
fun <T> Continuation<T>.resumeWithException(e: Throwable) = resumeWith(Result.failure(e))

// TODO 3: реализуй эту функцию.
// Она симулирует "асинхронную" работу: получает id, через какое-то время
// должна вернуть строку вызывающему — но не через return, а через continuation.
@Suspend
fun fetchUser(id: Int, continuation: Continuation<String>) {
    continuation.resumeWith(Result.success("User#$id"))
}

// TODO 4: реализуй эту функцию.
// Она симулирует падение: если id < 0 — ошибка, иначе возвращает профиль.
@Suspend
fun fetchProfile(userId: String, continuation: Continuation<String>) {
    if (userId.isEmpty()) {
        continuation.resumeWithException(RuntimeException("Failed to fetch empty userId")) 
    } else { 
        continuation.resumeWith(Result.success("Profile of $userId"))
    }
}

// TODO 5: в main() создай анонимный объект Continuation<String> и передай его в fetchUser.
// В resumeWith распечатай результат (успех или ошибку).
// Потом сцепи: результат fetchUser передай в fetchProfile.
fun main() {
    val profileCont = object : Continuation<String> {
        override fun resumeWith(result: Result<String>) {
            println(result)
        }
    }

    val userCont = object : Continuation<String> {
        override fun resumeWith(result: Result<String>) {
            fetchProfile(result.getOrThrow(), profileCont)
        }
    }

    fetchUser(42, userCont)
}
