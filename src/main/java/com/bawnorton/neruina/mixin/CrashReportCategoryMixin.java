package com.bawnorton.neruina.mixin;

import com.bawnorton.neruina.extend.CrashReportCategoryExtender;
import net.minecraft.CrashReportCategory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(CrashReportCategory.class)
public abstract class CrashReportCategoryMixin implements CrashReportCategoryExtender {
	@Shadow
	private StackTraceElement[] stackTrace;

	@Override
	public void neruin$setStacktrace(Throwable throwable) {
		stackTrace = throwable.getStackTrace();
	}
}
