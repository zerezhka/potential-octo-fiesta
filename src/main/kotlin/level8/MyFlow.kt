@file:JvmName("MainKt")
package level8

// Level 8 (Bonus): Cold Stream — MyFlow<T>
//
// A Flow is a cold, sequential stream: nothing runs until collect() is called.
// emit() suspends until the collector is ready; collect() suspends waiting for next value.
//
// TODO:
//   fun interface MyFlow<T> {
//       suspend fun collect(collector: FlowCollector<T>)
//   }
//
//   fun interface FlowCollector<T> {
//       suspend fun emit(value: T)
//   }
//
//   fun <T> myFlow(block: suspend FlowCollector<T>.() -> Unit): MyFlow<T>
//
//   Operators: map, filter, take

fun main() {
    // Build a flow that emits 1..5, map * 2, filter even, collect + print
}
