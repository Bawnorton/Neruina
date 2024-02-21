package com.bawnorton.neruina.util;

import com.bawnorton.neruina.extend.CrashReportSectionExtender;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.BlockPos;
import java.util.UUID;
import java.util.function.Function;

public record TickingEntry(Object cause, BlockPos pos, UUID uuid, Throwable e) {
    public void populate(CrashReportSection section) {
        section.add("Message", e.getMessage());
        ((CrashReportSectionExtender) section).neruin$setStacktrace(e);
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
            section.add("Errored", cause);
        }
    }

    public CrashReport createCrashReport() {
        CrashReport report = new CrashReport("Ticking %s".formatted(getCauseType()), e);
        CrashReportSection section = report.addElement("Source: %s".formatted(getCauseName()));
        populate(section);
        return report;
    }

    public String getCauseType() {
        if (cause instanceof Entity) {
            return Type.ENTITY.type;
        } else if (cause instanceof BlockEntity) {
            return Type.BLOCK_ENTITY.type;
        } else if (cause instanceof BlockState) {
            return Type.BLOCK_STATE.type;
        } else if (cause instanceof ItemStack) {
            return Type.ITEM_STACK.type;
        } else {
            return Type.UNKNOWN.type;
        }
    }

    public String getCauseName() {
        if (cause instanceof Entity entity) {
            return Type.ENTITY.nameFunction.apply(entity);
        } else if (cause instanceof BlockEntity blockEntity) {
            return Type.BLOCK_ENTITY.nameFunction.apply(blockEntity);
        } else if (cause instanceof BlockState blockState) {
            return Type.BLOCK_STATE.nameFunction.apply(blockState);
        } else if (cause instanceof ItemStack itemStack) {
            return Type.ITEM_STACK.nameFunction.apply(itemStack);
        } else {
            return Type.UNKNOWN.nameFunction.apply(cause);
        }
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
        static final Type<Object> UNKNOWN = new Type<>("Unknown", Object::toString);
    }
}