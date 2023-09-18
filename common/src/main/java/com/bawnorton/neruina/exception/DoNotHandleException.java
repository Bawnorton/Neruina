package com.bawnorton.neruina.exception;

// holder class for exceptions that should bypass the mod
public class DoNotHandleException extends RuntimeException {
    public DoNotHandleException(Throwable e) {
        super(e);
    }

    @Override
    public String getMessage() {
        return "Neruina cannot handle this exception. Please report this to the culprit mod author.\n" + super.getMessage();
    }
}
