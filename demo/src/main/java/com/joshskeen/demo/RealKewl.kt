package com.joshskeen.demo

fun main() {
    val kWrapper = TestJavaClass<String>().KWrapper

    kWrapper.genericsAreCoolToo(
        testFoo = listOf("oh yeah!")
    )

    val radCool: String = kWrapper.radCool(someType = "foo")

    kWrapper.realKewlMethod(
        doubleTrouble = 1.0,
        nameOfPet = "foo",
        cigarTypes = listOf("maduro", "churchill")
    )
}