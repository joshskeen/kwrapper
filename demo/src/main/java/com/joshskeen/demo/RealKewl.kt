package com.joshskeen.demo

fun main() {
    val x = TestJavaClassKWrapper(TestJavaClass()).genericsAreCoolToo(
        testFoo = listOf("oh yeah!")
    )
    println(x)
}