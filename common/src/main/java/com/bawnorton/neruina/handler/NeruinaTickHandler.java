package com.bawnorton.neruina.handler;

import com.bawnorton.neruina.Neruina;
import com.bawnorton.neruina.exception.DoNotHandleException;
import com.bawnorton.neruina.thread.ConditionalRunnable;
import com.bawnorton.neruina.version.Version;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
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

    private static MinecraftServer server;

    public static void setServer(MinecraftServer server) {
        NeruinaTickHandler.server = server;
    }

    public static void safelyTickItemStack$notTheCauseOfTickLag(ItemStack instance, World world, Entity entity, int slot, boolean selected, Operation<Void> original) {
        try {
            if (isErrored(instance)) {
                return;
            }
            original.call(instance, world, entity, slot, selected);
        } catch (Throwable e) {
            String message = Version.translatableText("neruina.ticking.item_stack", instance.getItem().getName().getString(), slot).getString();
            Neruina.LOGGER.warn((world.isClient? "Client: " : "Server: ") + message, e);
            addErrored(instance);
            if (world.isClient && entity instanceof PlayerEntity player) {
                player.sendMessage(Version.textOf(message), false);
            }
        }
    }

    public static void safelyTickPlayer$notTheCauseOfTickLag(ServerPlayerEntity instance, Operation<Void> original) {
        try {
            original.call(instance);
        } catch (Throwable e) {
            handleTickingPlayer(instance, e);
        }
    }

    public static void safelyTickBlockState$notTheCauseOfTickLag(BlockState instance, ServerWorld world, BlockPos pos, /* java.util.Random or net.minecraft.util.math.random.Random */ Object random, Operation<Void> original) {
        try {
            if (isErrored(pos, instance)) {
                return;
            }
            original.call(instance, world, pos, random);
        } catch (Throwable e) {
            String message = Version.translatableText("neruina.ticking.block_state", instance.getBlock().getName(), pos.getX(), pos.getY(), pos.getZ()).getString();
            Neruina.LOGGER.warn("Server: " + message, e);
            addErrored(pos, instance);
            messagePlayers(message);
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
            String message = Version.translatableText("neruina.ticking.block_entity", state.getBlock().getName(), pos.getX(), pos.getY(), pos.getZ()).getString();
            Neruina.LOGGER.warn((world.isClient? "Client: " : "Server: ") + message, e);
            addErrored(blockEntity);
            if (!world.isClient()) {
                messagePlayers(message);
            }
        }
    }

    public static <T extends Entity> void safelyTickEntities$notTheCauseOfTickLag(Consumer<T> instance, T entity, Operation<Void> original) {
        try {
            if (isErrored(entity)) {
                handleErroredEntity(entity);
                return;
            }
            original.call(instance, entity);
        } catch (DoNotHandleException e) {
            throw e;
        } catch (Throwable e) {
            handleEntityTicking(entity, e);
        }
    }

    public static <T extends Entity> void safelyTickEntities$notTheCauseOfTickLag(Consumer<T> consumer, T entity, World world, /* java Random or mc Random */ Object random, Operation<Void> original) {
        try {
            if (isErrored(entity)) {
                handleErroredEntity(entity);
                return;
            }
            original.call(consumer, entity, world, random);
        } catch (DoNotHandleException e) {
            throw e;
        } catch (Throwable e) {
            handleEntityTicking(entity, e);
        }
    }

    private static void handleErroredEntity(Entity entity) {
        try {
            if(entity instanceof PlayerEntity) return;
            if(entity.getEntityWorld().isClient()) return;

            entity.kill();
            entity.remove(Entity.RemovalReason.KILLED);
            entity.baseTick();
            removeErrored(entity);
        } catch (Throwable e) {
            throw new DoNotHandleException(e);
        }
    }

    private static void handleEntityTicking(Entity entity, Throwable e) {
        if(entity instanceof ServerPlayerEntity player) {
            handleTickingPlayer(player, e);
            return;
        } else if (entity instanceof PlayerEntity) {
            throw new DoNotHandleException(e);
        }

        Vec3d pos = entity.getPos();
        String message = Version.translatableText("neruina.ticking.entity", entity.getName().getString(), Math.floor(pos.getX()), Math.floor(pos.getY()), Math.floor(pos.getZ())).getString();
        Neruina.LOGGER.warn((entity.getEntityWorld().isClient? "Client: " : "Server: ") + message, e);
        addErrored(entity);
        if (!entity.getEntityWorld().isClient()) {
            messagePlayers(message);
        }
    }

    private static void handleTickingPlayer(ServerPlayerEntity player, Throwable e) {
        String message = Version.translatableText("neruina.ticking.player", player.getName().getString()).getString();
        Neruina.LOGGER.warn(message, e);
        if (!player.getEntityWorld().isClient()) {
            if(!server.isDedicated()) throw new DoNotHandleException(e);

            messagePlayers(message);
            player.networkHandler.disconnect(Version.textOf(Version.translatableText("neruina.kick.message").getString()));
        }
    }

    private static void messagePlayers(Text message) {
        PlayerManager playerManager = server.getPlayerManager();
        ConditionalRunnable.create(() -> playerManager.getPlayerList().forEach(player -> player.sendMessage(message, false)), () -> playerManager.getCurrentPlayerCount() >= 1);
    }

    private static void messagePlayers(String message) {
        messagePlayers(Version.textOf(message));
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
