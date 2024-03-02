package com.bawnorton.neruina.util;

public enum ErroredType {
    ITEM_STACK,
    ENTITY,
    BLOCK_ENTITY,
    BLOCK_STATE,
    UNKNOWN;

    public String getName() {
        return this.name().toLowerCase();
    }
}
