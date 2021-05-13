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
Dream: 
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

Experiment so far generates from this java file...
```java
public class TestJavaClass {

    void someMethod(String argOne, String argTwo, Boolean mySweetBool) {
        System.out.println(argOne + argTwo);
    }

    List<String> realKewlMethod(double doubleTrouble, String nameOfPet, List<String> cigarTypes) {
        return Arrays.asList("pizza", "pasta");
    }

    String genericsAreCoolToo(List<String> testFoo) {
        return "top funky";
    }

}
```

...the following kotlin output!
```kotlin
public class TestJavaClassKWrapper(
  private val wrappee: TestJavaClass
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
}
```
Which can now be used as: 
```kotlin
fun main() {
    val x = TestJavaClassKWrapper(TestJavaClass()).genericsAreCoolToo(
        testFoo = listOf("oh yeah!")
    )
    println(x)
}
```
