package com.bawnorton.neruina.exception;

import net.minecraft.resources.Identifier;

public class TickingException extends RuntimeException {
	public TickingException(String message, Throwable cause) {
		super(message, cause);
	}

	public TickingException(String message) {
		super(message);
	}

	public static TickingException notHandled(String configOption, Throwable cause) {
		return new TickingException(
				"Ticking exception not handled as \"%s\" is set to \"false\"".formatted(configOption),
				cause
		);
	}

	public static TickingException blacklisted(Identifier owningBlacklist, Identifier tickingId, Throwable cause) {
		return new TickingException(
				"Ticking exception not handled as handling for \"%s\" is blacklisted by \"%s\"".formatted(
						tickingId,
						owningBlacklist
				),
				cause
		);
	}
}
