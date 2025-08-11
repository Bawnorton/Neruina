package com.bawnorton.neruina.exception;

import net.minecraft.resources.ResourceLocation;

public class TickingException extends RuntimeException {
    public TickingException(String message, Throwable cause) {
        super(message, cause);
    }

    public static TickingException notHandled(String configOption, Throwable cause) {
        return new TickingException(
                "Ticking exception not handled as \"%s\" is set to \"false\"".formatted(configOption),
                cause
        );
    }

    public static TickingException blacklisted(ResourceLocation owningBlacklist, ResourceLocation tickingId, Throwable cause) {
        return new TickingException(
                "Ticking exception not handled as handling for \"%s\" is blacklisted by \"%s\"".formatted(
                        tickingId,
                        owningBlacklist
                ),
                cause
        );
    }
}
