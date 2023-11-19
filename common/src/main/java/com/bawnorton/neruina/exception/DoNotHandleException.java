package com.bawnorton.neruina.exception;

public class DoNotHandleException extends RuntimeException {
    public DoNotHandleException(Throwable e, Reason reason) {
        super(reason.getMessage(), e);
    }

    public DoNotHandleException(Throwable e) {
        super(e);
    }

    public enum Reason {
        ENTITY_DISABLED("Neruina skipped handling an entity because it was disabled in the config"),
        BLOCK_ENTITY_DISABLED("Neruina skipped handling a block entity because it was disabled in the config"),
        BLOCK_STATE_DISABLED("Neruina skipped handling a block state because it was disabled in the config"),
        ITEM_STACK_DISABLED("Neruina skipped handling an item stack because it was disabled in the config"),
        PLAYER_DISABLED("Neruina skipped handling a player because it was disabled in the config"),
        PLAYER_IN_SINGLEPLAYER("Neruina cannot prevent a ticking player exception in singleplayer");

        private final String message;

        Reason(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }
}
