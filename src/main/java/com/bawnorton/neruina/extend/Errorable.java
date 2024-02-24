package com.bawnorton.neruina.extend;

import java.util.UUID;

public interface Errorable {
    boolean neruina$isErrored();

    void neruina$setErrored();

    void neruina$clearErrored();

    UUID neruina$getTickingEntry();

    void neruina$setTickingEntry(UUID uuid);
}
