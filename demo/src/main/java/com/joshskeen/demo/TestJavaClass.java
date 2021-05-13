package com.joshskeen.demo;

import com.joshskeen.annotation.KWrapper;

@KWrapper
public class TestJavaClass {
    void someMethod(String argOne, String argTwo) {
        System.out.println(argOne + argTwo);
    }
}
