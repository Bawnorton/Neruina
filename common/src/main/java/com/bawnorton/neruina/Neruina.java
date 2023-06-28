package com.bawnorton.neruina;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Neruina {
    public static final String MOD_ID = "neruina";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static final Set<BlockEntity> ERRORED_BLOCK_ENTITIES = new HashSet<>();
    private static final Set<Entity> ERRORED_ENTITIES = new HashSet<>();
    private static final Set<ItemStack> ERRORED_ITEM_STACKS = new HashSet<>();
    private static final Set<ImmutablePair<BlockPos, BlockState>> ERRORED_BLOCK_STATES = new HashSet<>();

    public static void init() {
        LOGGER.info("Initializing Neruina");
    }

    public static boolean isErrored(BlockEntity blockEntity) {
        return ERRORED_BLOCK_ENTITIES.contains(blockEntity);
    }

    public static void addErrored(BlockEntity blockEntity) {
        ERRORED_BLOCK_ENTITIES.add(blockEntity);
    }

    public static void removeErrored(BlockEntity blockEntity) {
        ERRORED_BLOCK_ENTITIES.remove(blockEntity);
    }

    public static boolean isErrored(Entity entity) {
        return ERRORED_ENTITIES.contains(entity);
    }

    public static void addErrored(Entity entity) {
        ERRORED_ENTITIES.add(entity);
    }

    public static void removeErrored(Entity entity) {
        ERRORED_ENTITIES.remove(entity);
    }

    public static boolean isErrored(ItemStack item) {
        return ERRORED_ITEM_STACKS.contains(item);
    }

    public static void addErrored(ItemStack item) {
        ERRORED_ITEM_STACKS.add(item);
    }

    public static boolean isErrored(BlockPos pos, BlockState state) {
        return ERRORED_BLOCK_STATES.contains(new ImmutablePair<>(pos, state));
    }

    public static void addErrored(BlockPos pos, BlockState state) {
        ERRORED_BLOCK_STATES.add(new ImmutablePair<>(pos, state));
    }

    public static void removeErrored(BlockPos pos, BlockState state) {
        ERRORED_BLOCK_STATES.remove(new ImmutablePair<>(pos, state));
    }
}
