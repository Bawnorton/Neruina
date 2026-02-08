package com.bawnorton.neruina.util;

import com.bawnorton.neruina.Neruina;
import com.bawnorton.neruina.extend.CrashReportCategoryExtender;
import com.bawnorton.neruina.handler.PersitanceHandler;
import com.bawnorton.neruina.platform.Platform;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.transformer.meta.MixinMerged;

import java.lang.reflect.Method;
import java.net.URL;
import java.security.CodeSource;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

//? if >1.20.1 {
import net.minecraft.ReportType;
//?}

public final class TickingEntry {
	private static final Codec<UUID> UUID_CODEC = Codec.either(UUIDUtil.CODEC, UUIDUtil.STRING_CODEC)
			.xmap(
					either -> either.map(Function.identity(), Function.identity()),
					Either::left
			);

	public static final Codec<TickingEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.fieldOf("causeType").forGetter(TickingEntry::getCauseType),
			Codec.STRING.fieldOf("causeName").forGetter(TickingEntry::getCauseName),
			UUID_CODEC.fieldOf("uuid").forGetter(TickingEntry::uuid),
			Level.RESOURCE_KEY_CODEC.fieldOf("dimension").forGetter(TickingEntry::dimension),
			BlockPos.CODEC.fieldOf("pos").forGetter(TickingEntry::pos),
			ThrowableData.CODEC.fieldOf("error").forGetter(tickingEntry -> ThrowableData.fromThrowable(tickingEntry.error())),
			UUID_CODEC.optionalFieldOf("entityUuid").forGetter(tickingEntry -> {
				try {
					if (tickingEntry.getCauseType().equals(Type.ENTITY.type)) {
						if (tickingEntry.cachedEntityUuid != null) {
							return Optional.of(tickingEntry.cachedEntityUuid);
						} else if (tickingEntry.getCause() instanceof Entity entity) {
							tickingEntry.cachedEntityUuid = entity.getUUID();
							return Optional.of(tickingEntry.cachedEntityUuid);
						}
					}
				} catch (RuntimeException e) {
					Neruina.LOGGER.warn("Failed to find entity UUID when serializing TickingEntry", e);
				}
				return Optional.empty();
			})
	).apply(instance, (causeType, causeName, uuid, dimension, pos, error, entityUuid) -> {
		Supplier<Object> cause = () -> null;
		if (causeType.equals(Type.ENTITY.type)) {
			if (entityUuid.isPresent()) {
				UUID entityUuidValue = entityUuid.get();
				cause = () -> PersitanceHandler.getLevel().getEntity(entityUuidValue);
			}
		} else if (causeType.equals(Type.BLOCK_ENTITY.type)) {
			cause = () -> PersitanceHandler.getLevel().getBlockEntity(pos);
		} else if (causeType.equals(Type.BLOCK_STATE.type)) {
			cause = () -> PersitanceHandler.getLevel().getBlockState(pos);
		}
		TickingEntry entry = new TickingEntry(cause, true, dimension, pos, uuid, error.toThrowable());
		entry.cachedCauseType = causeType;
		entry.cachedCauseName = causeName;
		return entry;
	}));

	private final Supplier<Object> causeSupplier;
	private final boolean persitent;
	private final ResourceKey<Level> dimension;
	private final BlockPos pos;
	private final Throwable error;
	private final UUID uuid;
	private String cachedCauseType;
	private String cachedCauseName;
	private UUID cachedEntityUuid;

	private final List<String> blacklistedModids = List.of(
			Neruina.MOD_ID, "minecraft", "forge", "neoforge"
	);

	public TickingEntry(Object cause, boolean persitent, ResourceKey<Level> dimension, BlockPos pos, Throwable error) {
		this.causeSupplier = () -> cause;
		this.persitent = persitent;
		this.dimension = dimension;
		this.pos = pos;
		this.error = error;
		this.uuid = UUID.randomUUID();

		this.update();
	}

	private TickingEntry(Supplier<Object> causeSupplier, boolean persitent, ResourceKey<Level> dimension, BlockPos pos, UUID uuid, Throwable error) {
		this.causeSupplier = causeSupplier;
		this.persitent = persitent;
		this.dimension = dimension;
		this.pos = pos;
		this.uuid = uuid;
		this.error = error;
	}

	public void populate(CrashReportCategory category) {
		category.setDetail("Message", error.toString());
		((CrashReportCategoryExtender) category).neruin$setStacktrace(error);
		Object cause = getCause();
		//? if >1.20.1 {
		switch (cause) {
			case Entity entity -> entity.fillCrashReportCategory(category);
			case BlockEntity blockEntity -> blockEntity.fillCrashReportCategory(category);
			case BlockState state -> {
				category.setDetail("Position", pos);
				category.setDetail("BlockState", state);
			}
			case ItemStack stack -> category.setDetail("ItemStack", stack);
			case null, default -> category.setDetail("Errored", "Unknown");
		}
		//?} else {
		/*if (cause instanceof Entity entity) {
			entity.fillCrashReportCategory(category);
		} else if (cause instanceof BlockEntity blockEntity) {
			blockEntity.fillCrashReportCategory(category);
		} else if (cause instanceof BlockState state) {
			category.setDetail("Position", pos);
			category.setDetail("BlockState", state);
		} else if (cause instanceof ItemStack stack) {
			category.setDetail("ItemStack", stack);
		} else {
			category.setDetail("Errored", "Unknown");
		}
		*///?}
	}

	public String createCrashReport() {
		CrashReport report = new CrashReport("Ticking %s".formatted(getCauseType()), error);
		CrashReportCategory category = report.addCategory("Source: %s".formatted(getCauseName()));
		populate(category);
		//? if >1.20.1 {
		return report.getFriendlyReport(ReportType.CRASH);
		//?} else {
		/*return report.getFriendlyReport();
		*///?}
	}

	public Object getCause() {
		try {
			return causeSupplier.get();
		} catch (RuntimeException e) {
			Neruina.LOGGER.warn("Failed to get cause", e);
			return null;
		}
	}

	public void update() {
		Object cause = causeSupplier.get();
		//? if >1.20.1 {
		switch (cause) {
			case Entity entity -> {
				cachedCauseType = Type.ENTITY.type;
				cachedCauseName = Type.ENTITY.nameFunction.apply(entity);
			}
			case BlockEntity blockEntity -> {
				cachedCauseType = Type.BLOCK_ENTITY.type;
				cachedCauseName = Type.BLOCK_ENTITY.nameFunction.apply(blockEntity);
			}
			case BlockState state -> {
				cachedCauseType = Type.BLOCK_STATE.type;
				cachedCauseName = Type.BLOCK_STATE.nameFunction.apply(state);
			}
			case ItemStack stack -> {
				cachedCauseType = Type.ITEM_STACK.type;
				cachedCauseName = Type.ITEM_STACK.nameFunction.apply(stack);
			}
			case null, default -> {
				cachedCauseType = Type.UNKNOWN.type;
				cachedCauseName = Type.UNKNOWN.nameFunction.apply(cause);
			}
		}
		//?} else {
		/*if (cause instanceof Entity entity) {
			cachedCauseType = Type.ENTITY.type;
			cachedCauseName = Type.ENTITY.nameFunction.apply(entity);
		} else if (cause instanceof BlockEntity blockEntity) {
			cachedCauseType = Type.BLOCK_ENTITY.type;
			cachedCauseName = Type.BLOCK_ENTITY.nameFunction.apply(blockEntity);
		} else if (cause instanceof BlockState state) {
			cachedCauseType = Type.BLOCK_STATE.type;
			cachedCauseName = Type.BLOCK_STATE.nameFunction.apply(state);
		} else if (cause instanceof ItemStack stack) {
			cachedCauseType = Type.ITEM_STACK.type;
			cachedCauseName = Type.ITEM_STACK.nameFunction.apply(stack);
		} else {
			cachedCauseType = Type.UNKNOWN.type;
			cachedCauseName = Type.UNKNOWN.nameFunction.apply(cause);
		}
		*///?}
	}

	public String getCauseType() {
		return cachedCauseType;
	}

	public String getCauseName() {
		return cachedCauseName;
	}

	public Set<String> findPotentialSources() {
		StackTraceElement[] stackTrace = error.getStackTrace();
		Set<String> modids = new HashSet<>();
		for (StackTraceElement element : stackTrace) {
			Class<?> clazz;
			try {
				clazz = Class.forName(element.getClassName());
			} catch (ClassNotFoundException ignored) {
				continue;
			}

			String methodName = element.getMethodName();
			String modid = checkForMixin(clazz, methodName);
			if (modid != null) {
				modids.add(modid);
				continue;
			}

			CodeSource codeSource = clazz.getProtectionDomain().getCodeSource();
			if (codeSource == null) continue;

			URL resource = codeSource.getLocation();
			String modidFromResource = modidFromResource(resource);
			if (modidFromResource != null) {
				modids.add(modidFromResource);
			}
		}
		blacklistedModids.forEach(modids::remove);
		return modids;
	}

	private @Nullable String checkForMixin(Class<?> clazz, String methodName) {
		MixinMerged annotation;
		Method method = Reflection.findMethod(clazz, methodName);
		if (method == null) return null;
		if (!method.isAnnotationPresent(MixinMerged.class)) return null;

		annotation = method.getAnnotation(MixinMerged.class);
		String mixinClassName = annotation.mixin();
		ClassLoader classLoader = clazz.getClassLoader();
		URL resource = classLoader.getResource(mixinClassName.replace('.', '/') + ".class");
		if (resource == null) return null;

		return modidFromResource(resource);
	}

	@Nullable
	private static String modidFromResource(URL resource) {
		String location = resource.getPath();
		int index = location.indexOf("jar");
		if (index != -1) {
			location = location.substring(0, index + "jar".length());
			String[] parts = location.split("/");
			String jarName = parts[parts.length - 1];
			return Platform.modidFromJar(jarName);
		}
		return null;
	}

	private static Throwable createThrowable(String message, String exceptionClass, StackTraceElement[] elements) {
		try {
			Class<?> clazz = Class.forName(exceptionClass);
			Throwable throwable = (Throwable) clazz.getConstructor(String.class).newInstance(message);
			throwable.setStackTrace(elements);
			return throwable;
		} catch (Exception e) {
			Throwable throwable = new Throwable(message);
			throwable.setStackTrace(elements);
			return throwable;
		}
	}

	public ResourceKey<Level> dimension() {
		return dimension;
	}

	public BlockPos pos() {
		return pos;
	}

	public UUID uuid() {
		return uuid;
	}

	public Throwable error() {
		return error;
	}

	public boolean isPersitent() {
		return persitent;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj == null || obj.getClass() != this.getClass()) {
			return false;
		}
		var that = (TickingEntry) obj;
		return Objects.equals(this.cachedCauseName, that.cachedCauseName) &&
				Objects.equals(this.cachedCauseType, that.cachedCauseType) &&
				Objects.equals(this.dimension, that.dimension) &&
				Objects.equals(this.pos, that.pos) &&
				Objects.equals(this.uuid, that.uuid) &&
				Objects.equals(this.error, that.error);
	}

	@Override
	public int hashCode() {
		return Objects.hash(cachedCauseType, cachedCauseName, dimension, pos, uuid, error);
	}

	@Override
	public String toString() {
		return "TickingEntry[causeType=%s, causeName=%s, dimension=%s pos=%s, uuid=%s, error=%s]".formatted(cachedCauseType, cachedCauseName, dimension, pos, uuid, error);
	}

	private record Type<T>(String type, Function<T, String> nameFunction) {
		static final Type<Entity> ENTITY = new Type<>("Entity", entity -> entity.getName().getString());
		static final Type<BlockEntity> BLOCK_ENTITY = new Type<>(
				"BlockEntity",
				blockEntity -> blockEntity.getBlockState().getBlock().getName().getString()
		);
		static final Type<BlockState> BLOCK_STATE = new Type<>(
				"BlockState",
				blockState -> blockState.getBlock().getName().getString()
		);
		static final Type<ItemStack> ITEM_STACK = new Type<>(
				"ItemStack",
				itemStack -> itemStack.getItem().getName(itemStack).getString()
		);
		static final Type<Object> UNKNOWN = new Type<>("Unknown", object -> "Unknown");
	}

	private record ThrowableData(String message, String exceptionClass, StackTraceElement[] elements) {
		public static final Codec<ThrowableData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.STRING.fieldOf("message").forGetter(throwableData -> Objects.requireNonNullElse(throwableData.message(), "")),
				Codec.STRING.fieldOf("exceptionClass").forGetter(ThrowableData::exceptionClass),
				StackTraceElementData.CODEC.listOf().fieldOf("elements").forGetter(ThrowableData::elementDatas)
		).apply(instance, ThrowableData::new));

		private ThrowableData(String message, String exceptionClass, List<StackTraceElementData> elements) {
			this(message, exceptionClass, elements.stream().map(StackTraceElementData::toStackTraceElement).toArray(StackTraceElement[]::new));
		}

		public static ThrowableData fromThrowable(Throwable throwable) {
			return new ThrowableData(
					throwable.getMessage(),
					throwable.getClass().getName(),
					throwable.getStackTrace()
			);
		}

		public Throwable toThrowable() {
			return createThrowable(message, exceptionClass, elements);
		}

		public List<StackTraceElementData> elementDatas() {
			return Stream.of(elements).map(StackTraceElementData::fromStackTraceElement).toList();
		}

		private record StackTraceElementData(String classLoaderName, String moduleName, String moduleVersion,
		                                     String declaringClass, String methodName, String fileName, int lineNumber) {
			public static final Codec<StackTraceElementData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
					Codec.STRING.optionalFieldOf("classLoaderName", "").forGetter(data -> Objects.requireNonNullElse(data.classLoaderName(), "")),
					Codec.STRING.optionalFieldOf("moduleName", "").forGetter(data -> Objects.requireNonNullElse(data.moduleName(), "")),
					Codec.STRING.optionalFieldOf("moduleVersion", "").forGetter(data -> Objects.requireNonNullElse(data.moduleVersion(), "")),
					Codec.STRING.fieldOf("declaringClass").forGetter(StackTraceElementData::declaringClass),
					Codec.STRING.fieldOf("methodName").forGetter(StackTraceElementData::methodName),
					Codec.STRING.optionalFieldOf("fileName", "").forGetter(data -> Objects.requireNonNullElse(data.fileName(), "")),
					Codec.INT.fieldOf("lineNumber").forGetter(StackTraceElementData::lineNumber)
			).apply(instance, StackTraceElementData::new));

			public static StackTraceElementData fromStackTraceElement(StackTraceElement element) {
				return new StackTraceElementData(
						element.getClassLoaderName(),
						element.getModuleName(),
						element.getModuleVersion(),
						element.getClassName(),
						element.getMethodName(),
						element.getFileName(),
						element.getLineNumber()
				);
			}

			public StackTraceElement toStackTraceElement() {
				return new StackTraceElement(
						classLoaderName.isEmpty() ? null : classLoaderName,
						moduleName.isEmpty() ? null : moduleName,
						moduleVersion.isEmpty() ? null : moduleVersion,
						declaringClass,
						methodName,
						fileName.isEmpty() ? null : fileName,
						lineNumber
				);
			}
		}
	}
}