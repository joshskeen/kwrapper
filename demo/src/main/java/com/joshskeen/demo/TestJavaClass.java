package com.joshskeen.demo;

import com.joshskeen.annotation.KWrapper;

import java.util.Arrays;
import java.util.List;

@KWrapper
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
