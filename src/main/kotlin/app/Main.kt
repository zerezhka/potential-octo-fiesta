package app

fun main() {
    println("Hello, World!")
    val coroutine = SimpleCoroutine()
    while (coroutine.resume()) {
        println("Waiting for coroutine to complete")
    }
    println("Coroutine completed")
}

interface Coroutine {
    fun resume(): Boolean
}

class SimpleCoroutine : Coroutine {

    private var state: Int = 0 // 0 - initial, 1 - running, 2 - suspended, 3 - completed

    override fun resume(): Boolean {
        when (state) {
            0 -> {
                println("Initial state")
                state = 1
                return true
            }
            1 -> {
                println("Running state")
                state = 2
                return true
            }
            2 -> {
                println("Suspended state")
                state = 3
                return true
            }
            3 -> {
                println("Completed state")
                state = 0
                return false
            }
            else -> {
                throw IllegalStateException("Invalid state: $state")
            }
        }
        println("SimpleCoroutine")
        return false
    }
}