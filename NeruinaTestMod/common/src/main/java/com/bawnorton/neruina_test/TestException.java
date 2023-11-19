package com.bawnorton.neruina_test;

public class TestException extends RuntimeException {
    private TestException(String message) {
        super(message);
    }

    public static TestException create() {
        return new TestException("Neruina Test Exception");
    }
}
