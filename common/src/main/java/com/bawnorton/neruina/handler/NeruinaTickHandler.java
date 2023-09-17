package com.bawnorton.neruina.handler;

import com.bawnorton.neruina.Neruina;
import com.bawnorton.neruina.thread.ConditionalRunnable;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public abstract class NeruinaTickHandler {
    private static final Set<BlockEntity> ERRORED_BLOCK_ENTITIES = new HashSet<>();
    private static final Set<Entity> ERRORED_ENTITIES = new HashSet<>();
    private static final Set<ItemStack> ERRORED_ITEM_STACKS = new HashSet<>();
    private static final Set<ImmutablePair<BlockPos, BlockState>> ERRORED_BLOCK_STATES = new HashSet<>();
    
    public static void safelyTickItemStack$notTheCauseOfTickLag(ItemStack instance, World world, Entity entity, int slot, boolean selected, Operation<Void> original) {
        try {
            if (isErrored(instance)) {
                return;
            }
            original.call(instance, world, entity, slot, selected);
        } catch (Throwable e) {
            String message = Text.translatable("neruina.ticking.item_stack", instance.getItem().getName().getString(), slot).getString();
            Neruina.LOGGER.warn((world.isClient? "Client: " : "Server: ") + message, e);
            addErrored(instance);
            if (world.isClient && entity instanceof PlayerEntity player) {
                player.sendMessage(Text.of(message), false);
            }
        }
    }

    public static void safelyTickPlayer$notTheCauseOfTickLag(ServerPlayerEntity instance, Operation<Void> original) {
        try {
            original.call(instance);
        } catch (Throwable e) {
            String message = Text.translatable("neruina.ticking.player", instance.getName().getString()).getString();
            Neruina.LOGGER.warn(message, e);
            if (instance.getWorld() instanceof ServerWorld serverWorld) {
                if(serverWorld.getServer().isDedicated()) {
                    messagePlayers(serverWorld, message);
                    instance.networkHandler.disconnect(Text.of(Text.translatable("neruina.kick.message").getString()));
                } else {
                    Neruina.LOGGER.error(Text.translatable("neruina.cannot.handle").getString());
                    throw e;
                }
            }
        }
    }

    public static void safelyTickBlockState$notTheCauseOfTickLag(BlockState instance, ServerWorld world, BlockPos pos, Random random, Operation<Void> original) {
        try {
            if (isErrored(pos, instance)) {
                return;
            }
            original.call(instance, world, pos, random);
        } catch (Throwable e) {
            String message = Text.translatable("neruina.ticking.block_state", instance.getBlock().getName(), pos.getX(), pos.getY(), pos.getZ()).getString();
            Neruina.LOGGER.warn("Server: " + message, e);
            addErrored(pos, instance);
            messagePlayers(world, message);
        }
    }

    public static void safelyTickBlockEntity$notTheCauseOfTickLag(BlockEntityTicker<? extends BlockEntity> instance, World world, BlockPos pos, BlockState state, BlockEntity blockEntity, Operation<Void> original) {
        try {
            if (isErrored(blockEntity)) {
                if(world.isClient) return;

                WorldChunk chunk = world.getWorldChunk(pos);
                chunk.removeBlockEntityTicker(pos);
                return;
            }
            original.call(instance, world, pos, state, blockEntity);
        } catch (Throwable e) {
            String message = Text.translatable("neruina.ticking.block_entity", state.getBlock().getName(), pos.getX(), pos.getY(), pos.getZ()).getString();
            Neruina.LOGGER.warn((world.isClient? "Client: " : "Server: ") + message, e);
            addErrored(blockEntity);
            if (world instanceof ServerWorld serverWorld) {
                messagePlayers(serverWorld, message);
            }
        }
    }

    public static <T extends Entity> void safelyTickEntities$notTheCauseOfTickLag(Consumer<T> instance, Object param, Operation<Void> original) {
        Entity entity = (Entity) param;
        try {
            if(isErrored(entity)) {
                if(entity instanceof PlayerEntity) return;
                if(entity.getWorld().isClient) return;

                entity.kill();
                entity.remove(Entity.RemovalReason.KILLED);
                entity.baseTick();
                removeErrored(entity);
                return;
            }
            original.call(instance, param);
        } catch (Throwable e) {
            BlockPos pos = entity.getBlockPos();
            String message = Text.translatable("neruina.ticking.entity", entity.getName().getString(), pos.getX(), pos.getY(), pos.getZ()).getString();
            Neruina.LOGGER.warn((entity.getWorld().isClient? "Client: " : "Server: ") + message, e);
            addErrored(entity);
            if (entity.getWorld() instanceof ServerWorld serverWorld) {
                messagePlayers(serverWorld, message);
            }
        }
    }

    private static void messagePlayers(ServerWorld world, String message) {
        PlayerManager playerManager = world.getServer().getPlayerManager();
        ConditionalRunnable.create(() -> playerManager.getPlayerList().forEach(player -> player.sendMessage(Text.of(message), false)), () -> playerManager.getCurrentPlayerCount() >= 1);
    }

    public static boolean isErrored(BlockEntity blockEntity) {
        if(ERRORED_BLOCK_ENTITIES.isEmpty()) return false;
        return ERRORED_BLOCK_ENTITIES.contains(blockEntity);
    }

    public static void addErrored(BlockEntity blockEntity) {
        ERRORED_BLOCK_ENTITIES.add(blockEntity);
    }

    public static void removeErrored(BlockEntity blockEntity) {
        ERRORED_BLOCK_ENTITIES.remove(blockEntity);
    }

    public static boolean isErrored(Entity entity) {
        if(ERRORED_ENTITIES.isEmpty()) return false;
        return ERRORED_ENTITIES.contains(entity);
    }

    public static void addErrored(Entity entity) {
        ERRORED_ENTITIES.add(entity);
    }

    public static void removeErrored(Entity entity) {
        ERRORED_ENTITIES.remove(entity);
    }

    public static boolean isErrored(ItemStack item) {
        if(ERRORED_ITEM_STACKS.isEmpty()) return false;
        return ERRORED_ITEM_STACKS.contains(item);
    }

    public static void addErrored(ItemStack item) {
        ERRORED_ITEM_STACKS.add(item);
    }

    public static boolean isErrored(BlockPos pos, BlockState state) {
        if(ERRORED_BLOCK_STATES.isEmpty()) return false;
        return ERRORED_BLOCK_STATES.contains(new ImmutablePair<>(pos, state));
    }

    public static void addErrored(BlockPos pos, BlockState state) {
        ERRORED_BLOCK_STATES.add(new ImmutablePair<>(pos, state));
    }

    public static void removeErrored(BlockPos pos, BlockState state) {
        ERRORED_BLOCK_STATES.remove(new ImmutablePair<>(pos, state));
    }
}
