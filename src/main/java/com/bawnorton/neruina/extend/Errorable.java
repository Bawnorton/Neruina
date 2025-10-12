package com.bawnorton.neruina.extend;

import java.util.UUID;

public interface Errorable {
	boolean neruina$isErrored();

	void neruina$setErrored();

	void neruina$clearErrored();

	UUID neruina$getTickingEntryId();

	void neruina$setTickingEntryId(UUID uuid);
}
