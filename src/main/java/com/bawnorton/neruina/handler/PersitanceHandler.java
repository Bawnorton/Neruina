package com.bawnorton.neruina.handler;

import com.bawnorton.neruina.Neruina;
import com.bawnorton.neruina.util.TickingEntry;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.DimensionDataStorage;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import net.minecraft.world.level.saveddata.SavedData;

//? if >=1.21.5
import net.minecraft.world.level.saveddata.SavedDataType;

public final class PersitanceHandler extends SavedData {
	private static final Codec<PersitanceHandler> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			TickingEntry.CODEC.listOf().fieldOf("tickingEntries").forGetter(PersitanceHandler::getTickingEntries)
	).apply(
			instance, (tickingEntries -> {
				PersitanceHandler handler = new PersitanceHandler();
				TickHandler tickHandler = Neruina.getInstance().getTickHandler();
				tickingEntries.forEach(tickHandler::addTickingEntryUnsafe);
				return handler;
			})
	));
	//? if 1.21.1 {
    /*private static final SavedData.Factory<PersitanceHandler> type = new SavedData.Factory<>(
            PersitanceHandler::new,
            (compoundTag, provider) -> load(compoundTag),
            null
    );
    *///?} else {
	private static final SavedDataType<PersitanceHandler> type = new SavedDataType<>(
			Neruina.MOD_ID,
			PersitanceHandler::new,
			CODEC,
			null
	);
	//?}

	private static ServerLevel level;

	public static void updateServerState(MinecraftServer server) {
		level = server.getLevel(Level.OVERWORLD);
		if (level == null) {
			Neruina.LOGGER.error("Level is null, unable to save persistent state.");
			return;
		}
		DimensionDataStorage dataStorage = level.getDataStorage();
		//? if 1.21.1 {
		/*PersitanceHandler handler = dataStorage.computeIfAbsent(type, Neruina.MOD_ID);
		 *///?} else {
		PersitanceHandler handler = dataStorage.computeIfAbsent(type);
		//?}
		handler.setDirty();
	}

	public static ServerLevel getLevel() {
		return level;
	}

	private List<TickingEntry> getTickingEntries() {
		return Neruina.getInstance()
				.getTickHandler()
				.getTickingEntries()
				.stream()
				.filter(TickingEntry::isPersitent)
				.toList();
	}

	public @NotNull CompoundTag save(@NotNull CompoundTag tag, HolderLookup.Provider registries) {
		DataResult<Tag> dataResult = CODEC.encode(this, NbtOps.INSTANCE, tag);
		return (CompoundTag) dataResult.getOrThrow();
	}

	private static PersitanceHandler load(CompoundTag tag) {
		DataResult<PersitanceHandler> dataResult = CODEC.parse(NbtOps.INSTANCE, tag);
		if (dataResult.isSuccess()) {
			return dataResult.getOrThrow();
		} else {
			Neruina.LOGGER.warn("Failed to load persitance handler. {}", dataResult.error().orElseThrow());
			return new PersitanceHandler();
		}
	}
}
