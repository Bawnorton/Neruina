package com.bawnorton.neruina.util;

import com.bawnorton.neruina.extend.CrashReportSectionExtender;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

public final class TickingEntry {
    private final Supplier<@Nullable Object> causeSupplier;
    private final BlockPos pos;
    private final UUID uuid;
    private final Throwable error;
    private String cachedCauseType;
    private String cachedCauseName;

    public TickingEntry(Supplier<@Nullable Object> causeSupplier, BlockPos pos, UUID uuid, Throwable error) {
        this.causeSupplier = causeSupplier;
        this.pos = pos;
        this.uuid = uuid;
        this.error = error;
    }

    public TickingEntry(Object cause, BlockPos pos, Throwable error) {
        this(() -> cause, pos, UUID.randomUUID(), error);
        this.update();
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

    public CrashReport createCrashReport() {
        CrashReport report = new CrashReport("Ticking %s".formatted(getCauseType()), error);
        CrashReportSection section = report.addElement("Source: %s".formatted(getCauseName()));
        populate(section);
        return report;
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

    public NbtCompound writeNbt() {
        NbtCompound nbt = new NbtCompound();
        Object cause = getCause();
        nbt.putString("causeType", getCauseType());
        nbt.putString("causeName", getCauseName());
        nbt.putUuid("uuid", uuid);
        nbt.putLong("pos", pos.asLong());
        writeStackTraceNbt(nbt);
        if (cause instanceof Entity entity) {
            nbt.putUuid("entityUuid", entity.getUuid());
        }
        return nbt;
    }

    private void writeStackTraceNbt(NbtCompound nbt) {
        nbt.putString("message", error.getMessage());
        nbt.putString("exception", error.getClass().getName());
        NbtList stacktrace = new NbtList();
        for (StackTraceElement element : error.getStackTrace()) {
            NbtCompound elementNbt = new NbtCompound();
            if (element.getClassLoaderName() != null) {
                elementNbt.putString("classLoaderName", element.getClassLoaderName());
            }
            if (element.getModuleName() != null) {
                elementNbt.putString("moduleName", element.getModuleName());
            }
            if (element.getModuleVersion() != null) {
                elementNbt.putString("moduleVersion", element.getModuleVersion());
            }
            elementNbt.putString("declaringClass", element.getClassName());
            elementNbt.putString("methodName", element.getMethodName());
            if (element.getFileName() != null) {
                elementNbt.putString("fileName", element.getFileName());
            }
            elementNbt.putInt("lineNumber", element.getLineNumber());
            stacktrace.add(elementNbt);
        }
        nbt.put("stacktrace", stacktrace);
    }

    public static TickingEntry fromNbt(ServerWorld world, NbtCompound nbtCompound) {
        String causeType = nbtCompound.getString("causeType");
        String causeName = nbtCompound.getString("causeName");
        UUID uuid = nbtCompound.getUuid("uuid");
        BlockPos pos = BlockPos.fromLong(nbtCompound.getLong("pos"));
        Throwable error = readStackTraceNbt(nbtCompound);
        Supplier<Object> cause = () -> null;
        if (causeType.equals(Type.ENTITY.type)) {
            if (nbtCompound.contains("entityUuid")) {
                UUID entityUuid = nbtCompound.getUuid("entityUuid");
                cause = () -> world.getEntity(entityUuid);
            }
        } else if (causeType.equals(Type.BLOCK_ENTITY.type)) {
            cause = () -> world.getBlockEntity(pos);
        } else if (causeType.equals(Type.BLOCK_STATE.type)) {
            cause = () -> world.getBlockState(pos);
        }
        TickingEntry entry = new TickingEntry(cause, pos, uuid, error);
        entry.cachedCauseType = causeType;
        entry.cachedCauseName = causeName;
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
            if (classLoaderName.isEmpty()) {
                classLoaderName = null;
            }
            String moduleName = compound.getString("moduleName");
            if (moduleName.isEmpty()) {
                moduleName = null;
            }
            String moduleVersion = compound.getString("moduleVersion");
            if (moduleVersion.isEmpty()) {
                moduleVersion = null;
            }
            String declaringClass = compound.getString("declaringClass");
            String methodName = compound.getString("methodName");
            String fileName = compound.getString("fileName");
            if (fileName.isEmpty()) {
                fileName = null;
            }
            int lineNumber = compound.getInt("lineNumber");
            elements[i] = new StackTraceElement(classLoaderName, moduleName, moduleVersion, declaringClass, methodName, fileName, lineNumber);
        }

        return createThrowable(message, exceptionClass, elements);
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

    public BlockPos pos() {
        return pos;
    }

    public UUID uuid() {
        return uuid;
    }

    public Throwable error() {
        return error;
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
                Objects.equals(this.pos, that.pos) &&
                Objects.equals(this.uuid, that.uuid) &&
                Objects.equals(this.error, that.error);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cachedCauseType, cachedCauseName, pos, uuid, error);
    }

    @Override
    public String toString() {
        return "TickingEntry[causeType=%s, causeName=%s, pos=%s, uuid=%s, error=%s]".formatted(cachedCauseType, cachedCauseName, pos, uuid, error);
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
}