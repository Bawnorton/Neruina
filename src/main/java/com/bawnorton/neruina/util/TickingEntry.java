package com.bawnorton.neruina.util;

import com.bawnorton.neruina.Neruina;
import com.bawnorton.neruina.extend.CrashReportSectionExtender;
import com.bawnorton.neruina.handler.PersitanceHandler;
import com.bawnorton.neruina.platform.Platform;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
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

//? if >1.21.4 {
/*import net.minecraft.util.Uuids;
import net.minecraft.registry.RegistryKey;
*///?} elif >1.19.2 {
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
//?} else {
/*import net.minecraft.util.registry.RegistryKey;
import net.minecraft.util.registry.Registry;
*///?}

public final class TickingEntry {
    //? if >1.21.4 {
    /*public static final Codec<TickingEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("causeType").forGetter(TickingEntry::getCauseType),
            Codec.STRING.fieldOf("causeName").forGetter(TickingEntry::getCauseName),
            Uuids.CODEC.fieldOf("uuid").forGetter(TickingEntry::uuid),
            World.CODEC.fieldOf("dimension").forGetter(TickingEntry::dimension),
            BlockPos.CODEC.fieldOf("pos").forGetter(TickingEntry::pos),
            ThrowableData.CODEC.fieldOf("error").forGetter(tickingEntry -> ThrowableData.fromThrowable(tickingEntry.error())),
            Uuids.CODEC.optionalFieldOf("entityUuid").forGetter(tickingEntry -> {
                try {
                    if (tickingEntry.getCauseType().equals(Type.ENTITY.type)) {
                        if (tickingEntry.cachedEntityUuid != null) {
                            return Optional.of(tickingEntry.cachedEntityUuid);
                        } else if (tickingEntry.getCause() instanceof Entity entity) {
                            tickingEntry.cachedEntityUuid = entity.getUuid();
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
                cause = () -> PersitanceHandler.getWorld().getEntity(entityUuidValue);
            }
        } else if (causeType.equals(Type.BLOCK_ENTITY.type)) {
            cause = () -> PersitanceHandler.getWorld().getBlockEntity(pos);
        } else if (causeType.equals(Type.BLOCK_STATE.type)) {
            cause = () -> PersitanceHandler.getWorld().getBlockState(pos);
        }
        TickingEntry entry = new TickingEntry(cause, true, dimension, pos, uuid, error.toThrowable());
        entry.cachedCauseType = causeType;
        entry.cachedCauseName = causeName;
        return entry;
    }));
    *///?}

    private final Supplier<@Nullable Object> causeSupplier;
    private final boolean persitent;
    private final RegistryKey<World> dimension;
    private final BlockPos pos;
    private final Throwable error;
    private final UUID uuid;
    private String cachedCauseType;
    private String cachedCauseName;
    private UUID cachedEntityUuid;

    private final List<String> blacklistedModids = List.of(
            Neruina.MOD_ID, "minecraft", "forge", "neoforge"
    );

    public TickingEntry(Object cause, boolean persitent, RegistryKey<World> dimension, BlockPos pos, Throwable error) {
        this.causeSupplier = () -> cause;
        this.persitent = persitent;
        this.dimension = dimension;
        this.pos = pos;
        this.error = error;
        this.uuid = UUID.randomUUID();

        this.update();
    }

    private TickingEntry(Supplier<@Nullable Object> causeSupplier, boolean persitent, RegistryKey<World> dimension, BlockPos pos, UUID uuid, Throwable error) {
        this.causeSupplier = causeSupplier;
        this.persitent = persitent;
        this.dimension = dimension;
        this.pos = pos;
        this.uuid = uuid;
        this.error = error;
    }

    public void populate(CrashReportSection section) {
        section.add("Message", error.getMessage());
        ((CrashReportSectionExtender) section).neruin$setStacktrace(error);
        Object cause = getCause();
        if (cause instanceof Entity entity) {
            entity.populateCrashReport(section);
        } else if (cause instanceof BlockEntity blockEntity) {
            blockEntity.populateCrashReport(section);
        } else if (cause instanceof BlockState state) {
            section.add("Position", pos);
            section.add("BlockState", state);
        } else if (cause instanceof ItemStack stack) {
            section.add("ItemStack", stack);
        } else {
            section.add("Errored", "Unknown");
        }
    }

    public String createCrashReport() {
        CrashReport report = new CrashReport("Ticking %s".formatted(getCauseType()), error);
        CrashReportSection section = report.addElement("Source: %s".formatted(getCauseName()));
        populate(section);
        return report.asString(
            //? if >=1.20.7
            net.minecraft.util.crash.ReportType.MINECRAFT_CRASH_REPORT
        );
    }

    public Object getCause() {
        return causeSupplier.get();
    }

    public void update() {
        Object cause = causeSupplier.get();
        if (cause instanceof Entity entity) {
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

    //? if <1.21.4 {
    public NbtCompound writeNbt() {
        NbtCompound nbt = new NbtCompound();
        nbt.putString("causeType", getCauseType());
        nbt.putString("causeName", getCauseName());
        nbt.putString("uuid", uuid.toString());
        nbt.putString("dimension", dimension.getValue().toString());
        nbt.putLong("pos", pos.asLong());
        writeStackTraceNbt(nbt);
        try {
            if (getCauseType().equals(Type.ENTITY.type)) {
                if (cachedEntityUuid != null) {
                    nbt.putUuid("entityUuid", cachedEntityUuid);
                } else if (getCause() instanceof Entity entity) {
                    cachedEntityUuid = entity.getUuid();
                    nbt.putUuid("entityUuid", cachedEntityUuid);
                }
            }
        } catch (RuntimeException e) {
            Neruina.LOGGER.warn("Failed to find entity UUID when serializing TickingEntry", e);
        }
        return nbt;
    }

    private void writeStackTraceNbt(NbtCompound nbt) {
        nbt.putString("message", nonNullString(error.getMessage()));
        nbt.putString("exception", error.getClass().getName());
        NbtList stacktrace = new NbtList();
        for (StackTraceElement element : error.getStackTrace()) {
            NbtCompound elementNbt = new NbtCompound();
            writeElementNbt(elementNbt, element);
            stacktrace.add(elementNbt);
        }
        nbt.put("stacktrace", stacktrace);
    }

    private void writeElementNbt(NbtCompound elementNbt, StackTraceElement element) {
        elementNbt.putString("classLoaderName", nonNullString(element.getClassLoaderName()));
        elementNbt.putString("moduleName", nonNullString(element.getModuleName()));
        elementNbt.putString("moduleVersion", nonNullString(element.getModuleVersion()));
        elementNbt.putString("declaringClass", nonNullString(element.getClassName()));
        elementNbt.putString("methodName", nonNullString(element.getMethodName()));
        elementNbt.putString("fileName", nonNullString(element.getFileName()));
        elementNbt.putInt("lineNumber", element.getLineNumber());
    }

    private String nonNullString(String str) {
        return str == null ? "" : str;
    }

    public static TickingEntry fromNbt(ServerWorld world, NbtCompound nbtCompound) {
        String causeType = nbtCompound.getString("causeType");
        String causeName = nbtCompound.getString("causeName");
        UUID uuid = nbtCompound.getUuid("uuid");
        String dimensionStr = nbtCompound.getString("dimension");
        RegistryKey<World> dimension;
        if(dimensionStr != null) {
            //? if >1.19.2 {
            dimension = RegistryKey.of(RegistryKeys.WORLD, Identifier.tryParse(dimensionStr));
            //?} else {
            /*dimension = RegistryKey.of(Registry.WORLD_KEY, Identifier.tryParse(dimensionStr));
            *///?}
        } else {
            dimension = World.OVERWORLD;
        }
        BlockPos pos = BlockPos.fromLong(nbtCompound.getLong("pos"));
        Throwable error = readStackTraceNbt(nbtCompound);
        Supplier<Object> cause = () -> null;
        UUID entityUuid = null;
        if (causeType.equals(Type.ENTITY.type)) {
            if (nbtCompound.contains("entityUuid")) {
                entityUuid = nbtCompound.getUuid("entityUuid");
                UUID finalEntityUuid = entityUuid;
                cause = () -> world.getEntity(finalEntityUuid);
            }
        } else if (causeType.equals(Type.BLOCK_ENTITY.type)) {
            cause = () -> world.getBlockEntity(pos);
        } else if (causeType.equals(Type.BLOCK_STATE.type)) {
            cause = () -> world.getBlockState(pos);
        }
        TickingEntry entry = new TickingEntry(cause, true, dimension, pos, uuid, error);
        entry.cachedCauseType = causeType;
        entry.cachedCauseName = causeName;
        entry.cachedEntityUuid = entityUuid;
        return entry;
    }

    private static Throwable readStackTraceNbt(NbtCompound nbtCompound) {
        String message = nbtCompound.getString("message");
        String exceptionClass = nbtCompound.getString("exception");
        NbtList stacktrace = nbtCompound.getList("stacktrace", NbtElement.COMPOUND_TYPE);
        StackTraceElement[] elements = new StackTraceElement[stacktrace.size()];
        for (int i = 0; i < stacktrace.size(); i++) {
            NbtElement nbtElement = stacktrace.get(i);
            NbtCompound compound = (NbtCompound) nbtElement;
            String classLoaderName = compound.getString("classLoaderName");
            if (classLoaderName.isEmpty()) classLoaderName = null;
            String moduleName = compound.getString("moduleName");
            if (moduleName.isEmpty()) moduleName = null;
            String moduleVersion = compound.getString("moduleVersion");
            if (moduleVersion.isEmpty()) moduleVersion = null;
            String declaringClass = compound.getString("declaringClass");
            String methodName = compound.getString("methodName");
            String fileName = compound.getString("fileName");
            if (fileName.isEmpty()) fileName = null;
            int lineNumber = compound.getInt("lineNumber");
            elements[i] = new StackTraceElement(classLoaderName, moduleName, moduleVersion, declaringClass, methodName, fileName, lineNumber);
        }

        return createThrowable(message, exceptionClass, elements);
    }
    //?}

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

    public RegistryKey<World> dimension() {
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
                blockEntity -> blockEntity.getCachedState().getBlock().getName().getString()
        );
        static final Type<BlockState> BLOCK_STATE = new Type<>(
                "BlockState",
                blockState -> blockState.getBlock().getName().getString()
        );
        static final Type<ItemStack> ITEM_STACK = new Type<>(
                "ItemStack",
                itemStack -> itemStack.getItem().getName().getString()
        );
        static final Type<Object> UNKNOWN = new Type<>("Unknown", object -> "Unknown");
    }

    //? if >1.21.4 {
    /*private record ThrowableData(String message, String exceptionClass, StackTraceElement[] elements) {
        public static final Codec<ThrowableData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("message").forGetter(ThrowableData::message),
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

        private record StackTraceElementData(String classLoaderName, String moduleName, String moduleVersion, String declaringClass, String methodName, String fileName, int lineNumber) {
            public static final Codec<StackTraceElementData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    Codec.STRING.fieldOf("classLoaderName").forGetter(StackTraceElementData::classLoaderName),
                    Codec.STRING.fieldOf("moduleName").forGetter(StackTraceElementData::moduleName),
                    Codec.STRING.fieldOf("moduleVersion").forGetter(StackTraceElementData::moduleVersion),
                    Codec.STRING.fieldOf("declaringClass").forGetter(StackTraceElementData::declaringClass),
                    Codec.STRING.fieldOf("methodName").forGetter(StackTraceElementData::methodName),
                    Codec.STRING.fieldOf("fileName").forGetter(StackTraceElementData::fileName),
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
                        classLoaderName,
                        moduleName,
                        moduleVersion,
                        declaringClass,
                        methodName,
                        fileName,
                        lineNumber
                );
            }
        }
    }
    *///?}
}