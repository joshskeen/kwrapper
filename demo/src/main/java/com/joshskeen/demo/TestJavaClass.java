package com.joshskeen.demo;

import com.joshskeen.kwrapper.annotation.KWrapper;

import java.util.Arrays;
import java.util.List;

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
