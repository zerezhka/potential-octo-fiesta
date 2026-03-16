@file:JvmName("MainKt")
package level7

// Level 7: CoroutineScope + Structured Concurrency
//
// Without a scope, coroutines are fire-and-forget — leaks, orphaned work, silent failures.
// A CoroutineScope ties a Job tree to a lifecycle:
//   - scope.myLaunch { } attaches child Job to scope's Job
//   - scope.cancel() cancels all children
//   - scope waits for all children before completing
//
// TODO:
//   interface CoroutineScope { val job: Job; val dispatcher: Dispatcher }
//
//   fun CoroutineScope.myLaunch(block: ...): Job  — child inherits scope's dispatcher
//
//   suspend fun coroutineScope(block: suspend CoroutineScope.() -> Unit)  — waits for all children
//
//   Bonus: implement SupervisorJob — child failure does NOT cancel siblings

fun main() {
    // Build a tree: parent launches two children, one fails — observe propagation
}
