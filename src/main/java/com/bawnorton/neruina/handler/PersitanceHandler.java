package com.bawnorton.neruina.handler;

import com.bawnorton.neruina.Neruina;
import com.bawnorton.neruina.thread.ConditionalRunnable;
import com.bawnorton.neruina.util.TickingEntry;
import com.bawnorton.neruina.version.VersionedText;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;
import java.util.ArrayList;
import java.util.List;

public final class PersitanceHandler extends PersistentState {
    private static ServerWorld world;
    private static final TickHandler tickHandler = Neruina.getInstance().getTickHandler();

    /*? if >=1.20.2 {*/
    private static final Type<PersitanceHandler> type = new Type<>(
            PersitanceHandler::new,
            PersitanceHandler::fromNbt,
            null
    );
    /*? } */

    public static PersitanceHandler getServerState(MinecraftServer server) {
        world = server.getWorld(World.OVERWORLD);
        assert world != null;
        PersistentStateManager manager = world.getPersistentStateManager();
        /*? if >=1.20.2 {*/
        PersitanceHandler handler = manager.getOrCreate(type, Neruina.MOD_ID);
        /*? } else {*//*
        PersitanceHandler handler = manager.getOrCreate(PersitanceHandler::fromNbtInternal, PersitanceHandler::new, Neruina.MOD_ID);
        *//*? }*/
        handler.markDirty();
        return handler;
    }

    /*? if >=1.20.2 {*/
    private static PersitanceHandler fromNbt(NbtCompound nbt, net.minecraft.registry.RegistryWrapper.WrapperLookup registryLookup) {
        return fromNbtInternal(nbt);
    }
    /*? } */

    private static PersitanceHandler fromNbt(NbtCompound nbt) {
        return fromNbtInternal(nbt);
    }

    private static PersitanceHandler fromNbtInternal(NbtCompound nbt) {
        PersitanceHandler handler = new PersitanceHandler();
        NbtList tickingEntries = nbt.getList("tickingEntries", NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < tickingEntries.size(); i++) {
            tickHandler.addTickingEntry(TickingEntry.fromNbt(world, tickingEntries.getCompound(i)));
        }
        List<Text> tickingEntryMessages = new ArrayList<>();
        int count = tickHandler.getTickingEntries().size();
        if(count == 0) return handler;

        if(count == 1) {
            tickingEntryMessages.add(VersionedText.format(VersionedText.translatable("neruina.ticking_entries.count.single")));
        } else {
            tickingEntryMessages.add(VersionedText.format(VersionedText.translatable("neruina.ticking_entries.count", count)));
        }
        MinecraftServer server = world.getServer();
        MessageHandler messageHandler = Neruina.getInstance().getMessageHandler();
        ConditionalRunnable.create(() -> server.execute(() -> {
            tickHandler.getTickingEntries().forEach(entry -> tickingEntryMessages.add(VersionedText.withStyle(
                    VersionedText.translatable("neruina.ticking_entries.entry", entry.getCauseName(), messageHandler.posAsNums(entry.pos())),
                    style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/neruina info " + entry.uuid()))
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, VersionedText.translatable("neruina.ticking_entries.entry.tooltip")))
                            .withColor(Formatting.RED)
            )));
            tickingEntryMessages.add(messageHandler.generateInfoAction());
            messageHandler.broadcastToPlayers(server, VersionedText.concatDelimited(VersionedText.LINE_BREAK, tickingEntryMessages.toArray(new Text[0])));
        }), () -> world.getChunkManager().getLoadedChunkCount() >= 9);
        return handler;
    }

    /*? if >=1.20.2 {*/
    public NbtCompound writeNbt(NbtCompound nbt, net.minecraft.registry.RegistryWrapper.WrapperLookup registryLookup) {
        return writeNbtInternal(nbt);
    }
    /*? } */

    public NbtCompound writeNbt(NbtCompound nbt) {
        return writeNbtInternal(nbt);
    }

    private NbtCompound writeNbtInternal(NbtCompound nbt) {
        NbtList tickingEntries = new NbtList();
        tickHandler.getTickingEntries().forEach(entry -> tickingEntries.add(entry.writeNbt()));
        nbt.put("tickingEntries", tickingEntries);
        return nbt;
    }
}
