package com.bawnorton.neruina.exception;

public class TickingException extends RuntimeException {
    public TickingException(String message, Throwable cause) {
        super(message, cause);
    }

    public static TickingException notHandled(String configOption, Throwable cause) {
        return new TickingException("Ticking exception not handled as \"" + configOption + "\" is set to \"false\"", cause);
    }
}
