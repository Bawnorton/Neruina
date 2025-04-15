package com.bawnorton.neruina.handler;

import com.bawnorton.neruina.Neruina;
import com.bawnorton.neruina.util.TickingEntry;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;
import java.util.List;

//? if >1.21.4 {
/*import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.PersistentStateType;
*///?}

public final class PersitanceHandler extends PersistentState {
    private static ServerWorld world;

    //? if >1.21.4 {
    /*private static final Codec<PersitanceHandler> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            TickingEntry.CODEC.listOf().fieldOf("tickingEntries").forGetter(PersitanceHandler::getTickingEntries)
    ).apply(instance, (tickingEntries -> {
        PersitanceHandler handler = new PersitanceHandler();
        TickHandler tickHandler = Neruina.getInstance().getTickHandler();
        tickingEntries.forEach(tickHandler::addTickingEntryUnsafe);
        return handler;
    })));

    private static final PersistentStateType<PersitanceHandler> type = new PersistentStateType<>(
            Neruina.MOD_ID,
            PersitanceHandler::new,
            CODEC,
            null
    );
    *///?} elif >=1.20.2 {
    private static final Type<PersitanceHandler> type = new Type<>(
            PersitanceHandler::new,
            PersitanceHandler::fromNbt,
            null
    );
    //?}

    public static void updateServerState(MinecraftServer server) {
        world = server.getWorld(World.OVERWORLD);
        if(world == null) {
            Neruina.LOGGER.error("World is null, unable to save persistent state.");
            return;
        }
        PersistentStateManager manager = world.getPersistentStateManager();
        //? if >1.21.4 {
        /*PersitanceHandler handler = manager.getOrCreate(type);
        *///?} elif >=1.20.2 {
        PersitanceHandler handler = manager.getOrCreate(type, Neruina.MOD_ID);
        //?} else {
        /*PersitanceHandler handler = manager.getOrCreate(PersitanceHandler::fromNbtInternal, PersitanceHandler::new, Neruina.MOD_ID);
        *///?}
        handler.markDirty();
    }

    //? if <1.21.4 {
    //? if >=1.20.2 {
    private static PersitanceHandler fromNbt(NbtCompound nbt, net.minecraft.registry.RegistryWrapper.WrapperLookup registryLookup) {
        return fromNbtInternal(nbt);
    }
    //?}

    private static PersitanceHandler fromNbt(NbtCompound nbt) {
        return fromNbtInternal(nbt);
    }

    private static PersitanceHandler fromNbtInternal(NbtCompound nbt) {
        PersitanceHandler handler = new PersitanceHandler();
        TickHandler tickHandler = Neruina.getInstance().getTickHandler();
        NbtList tickingEntries = nbt.getList("tickingEntries", NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < tickingEntries.size(); i++) {
            tickHandler.addTickingEntryUnsafe(TickingEntry.fromNbt(world, tickingEntries.getCompound(i)));
        }
        return handler;
    }

    //? if >=1.20.2 {
    public NbtCompound writeNbt(NbtCompound nbt, net.minecraft.registry.RegistryWrapper.WrapperLookup registryLookup) {
        return writeNbtInternal(nbt);
    }
    //?}

    public NbtCompound writeNbt(NbtCompound nbt) {
        return writeNbtInternal(nbt);
    }

    private NbtCompound writeNbtInternal(NbtCompound nbt) {
        NbtList tickingEntries = new NbtList();
        Neruina.getInstance()
                .getTickHandler()
                .getTickingEntries()
                .stream()
                .filter(TickingEntry::isPersitent)
                .forEach(entry -> tickingEntries.add(entry.writeNbt()));
        nbt.put("tickingEntries", tickingEntries);
        return nbt;
    }
    //?} else {

    /*private List<TickingEntry> getTickingEntries() {
        return Neruina.getInstance()
                      .getTickHandler()
                      .getTickingEntries()
                      .stream()
                      .filter(TickingEntry::isPersitent)
                      .toList();
    }

    public static ServerWorld getWorld() {
        return world;
    }
    *///?}
}
