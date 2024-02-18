package com.bawnorton.neruina.mixin;

import com.bawnorton.neruina.extend.CrashReportSectionExtender;
import net.minecraft.util.crash.CrashReportSection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(CrashReportSection.class)
public abstract class CrashReportSectionMixin implements CrashReportSectionExtender {
    @Shadow
    private StackTraceElement[] stackTrace;

    @Override
    public void neruin$setStacktrace(Throwable throwable) {
        stackTrace = throwable.getStackTrace();
    }
}
