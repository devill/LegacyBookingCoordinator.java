package com.legacybooking;

public class CanNotUseInTestsException extends RuntimeException {
    public CanNotUseInTestsException(String className) {
        super("Cannot use " + className + " in tests - this class has external dependencies!");
    }
}