package com.bawnorton.neruina.exception;

import java.util.concurrent.CancellationException;

public class InProgressException extends CancellationException {
    public InProgressException(String message) {
        super(message);
    }
}
