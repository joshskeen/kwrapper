Problem:
```java
class SomeJavaClass {

    void someReallyBigSignature(
        boolean someBool, String someString, boolean anotherArg, List<String> yetAnother, 
        int yetAnotherArg
    )
}
```
```kotlin
class SomeKotlinClass {
    fun doStuff() {
        SomeJavaClass().someReallyBigSignature(
            // --> NO NAMED PARAMS! 
        )
    }
}
```
Idea: 
```kotlin
class SomeKotlinClass {
    fun doStuff() {
        SomeJavaClass().KWrapper.someReallyBigSignature(
            someBool = true,
            someString = "pizza", 
            anotherArg = true,
            yetAnother = listOf("pizza"),
            yetAnotherArg = 42
        )
    }
}
```

Experiment so far generates from this java file (annotated with `@KWrapper`)...
```java
@KWrapper
public class TestJavaClass<X> {

    void someMethod(String argOne, String argTwo, Boolean mySweetBool) {
        System.out.println(argOne + argTwo);
    }

    List<String> realKewlMethod(double doubleTrouble, String nameOfPet, List<String> cigarTypes) {
        return Arrays.asList("pizza", "pasta");
    }

    String genericsAreCoolToo(List<String> testFoo) {
        return "top funky";
    }

    <T> T radCool(T someType) {
        return someType;
    }

}
```

...the following kotlin source:
```kotlin
@JvmInline
public value class TestJavaClassKWrapper<X : Any>(
    private val wrappee: TestJavaClass<X>
) {
    public fun someMethod(
        argOne: String,
        argTwo: String,
        mySweetBool: Boolean
    ): Unit = wrappee.someMethod(argOne, argTwo, mySweetBool)

    public fun realKewlMethod(
        doubleTrouble: Double,
        nameOfPet: String,
        cigarTypes: List<String>
    ): List<String> = wrappee.realKewlMethod(doubleTrouble, nameOfPet, cigarTypes)

    public fun genericsAreCoolToo(testFoo: List<String>): String = wrappee.genericsAreCoolToo(testFoo)

    public fun <T : Any> radCool(someType: T): T = wrappee.radCool(someType)
}

public val <X : Any> TestJavaClass<X>.KWrapper: TestJavaClassKWrapper<X>
    get() = TestJavaClassKWrapper(this)
```
Which can now be used as: 
```kotlin
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
```