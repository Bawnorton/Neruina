package com.bawnorton.neruina;

import net.fabricmc.api.ModInitializer;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class Neruina implements ModInitializer {
	public static final String MOD_ID = "neruina";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private static final List<BlockEntity> ERRORED_BLOCK_ENTITIES = new ArrayList<>();
	private static final List<Entity> ERRORED_ENTITIES = new ArrayList<>();
	private static final List<ItemStack> ERRORED_ITEM_STACKS = new ArrayList<>();

	@Override
	public void onInitialize() {
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
}