@file:JvmName("MainKt")
package level3

import level1.Continuation
import level1.resume
import level1.resumeWithException

// Level 3: State Machine
//
// Та же цепочка что в level2 (fetchUser → fetchProfile → fetchFriends),
// но вместо трёх вложенных объектов — ОДИН класс с полем label.
//
// Идея: каждая точка приостановки = одно состояние (label).
// При resumeWith() смотрим на label, делаем следующий шаг, меняем label.
//
// Локальные переменные (user, profile, friends) — поля класса,
// потому что они должны выжить между вызовами resumeWith().
//
// Диаграмма переходов:
//
//   label=0 → вызвали fetchUser    → label=1
//   label=1 → вызвали fetchProfile → label=2
//   label=2 → вызвали fetchFriends → label=3
//   label=3 → println + cont.resume(Unit)

// ---------------------------------------------------------------------------
// Примитивы (те же что в level2)
// ---------------------------------------------------------------------------

annotation class Suspend

@Suspend fun fetchUser(id: Int, cont: Continuation<String>) = cont.resume("User#$id")

@Suspend fun fetchProfile(user: String, cont: Continuation<String>) {
    if (user.isEmpty()) cont.resumeWithException(IllegalArgumentException("empty user"))
    else cont.resume("Profile of $user")
}

@Suspend fun fetchFriends(user: String, cont: Continuation<List<String>>) =
    cont.resume(listOf("Alice", "Bob"))

// ---------------------------------------------------------------------------
// TODO 1: реализуй ShowProfileSM — state machine для showProfile
//
// Класс реализует Continuation<Any?> — он сам себя передаёт в fetchUser/fetchProfile/fetchFriends
// и сам же получает их результат в resumeWith().
//
// Поля:
//   var label: Int = 0
//   var result: Any? = null        // сюда сохраняем входящий Result между шагами
//   var user: String? = null       // локальные переменные функции → поля класса
//   var profile: String? = null
//   val cont: Continuation<Unit>   // финальный continuation — кому сообщить об окончании
//
// resumeWith() — один большой when(label):
//   0 → fetchUser(id, this)        и label = 1
//   1 → user = result.getOrThrow() и fetchProfile(user, this) и label = 2
//   2 → profile = ...              и fetchFriends(user, this) и label = 3
//   3 → println(...)               и cont.resume(Unit)
//
// Запуск: sm.resumeWith(Result.success(Unit))  — первый пинок
// ---------------------------------------------------------------------------

class ShowProfileSM(val id: Int, val cont: Continuation<Unit>) : Continuation<Any?> {

    var label = 0
    var result: Any? = null
    var user: String? = null
    var profile: String? = null

    override fun resumeWith(result: Result<Any?>) {
        this.result = result
        when(label){
            0 -> {
                label++
                fetchUser(id, this)
            }
            1 -> {
                label++
                user = result.getOrThrow() as String?
                fetchProfile(user!!, this)
            }
            2 -> {
                label++
                profile = result.getOrThrow() as String?
                fetchFriends(user!!, this)
            }
            3 -> {
                var friends: List<String>? = null
                friends = result.getOrThrow() as List<String>
                println("Profile = $profile, friend = $friends")
                cont.resume(Unit)
            }
        }
    }
}

// ---------------------------------------------------------------------------
// TODO 2: запусти state machine отсюда
// ---------------------------------------------------------------------------
fun main() {
    val finalCont = object : Continuation<Unit> {
        override fun resumeWith(result: Result<Unit>) {
            if (result.isFailure) println("Error: ${result.exceptionOrNull()}")
        }
    }
    ShowProfileSM(69, finalCont).resumeWith(Result.success(Unit))
}
