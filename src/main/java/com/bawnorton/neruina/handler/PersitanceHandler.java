package com.bawnorton.neruina.handler;

import com.bawnorton.neruina.Neruina;
import com.bawnorton.neruina.util.TickingEntry;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

//~ if >=26.1 'DimensionDataStorage' -> 'SavedDataStorage'
import net.minecraft.world.level.storage.SavedDataStorage;

import java.util.List;

import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

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

	//? if >1.21.1 {
	private static final SavedDataType<PersitanceHandler> type = new SavedDataType<>(
			//$ if >=26.1 'ResourceLocation.fromNamespaceAndPath(Neruina.MOD_ID, "persistance"),' else 'Neruina.MOD_ID,'
			Identifier.fromNamespaceAndPath(Neruina.MOD_ID, "persistance"),
			PersitanceHandler::new,
			CODEC,
			null
	);
	//?} elif >1.20.1 {
  /*private static final SavedData.Factory<PersitanceHandler> type = new SavedData.Factory<>(
		  PersitanceHandler::new,
		  (compoundTag, provider) -> load(compoundTag),
		  null
  );
  *///?}

	private static ServerLevel level;

	public static void updateServerState(MinecraftServer server) {
		level = server.getLevel(Level.OVERWORLD);
		if (level == null) {
			Neruina.LOGGER.error("Level is null, unable to save persistent state.");
			return;
		}
		//~ if >=26.1 'DimensionDataStorage' -> 'SavedDataStorage'
		SavedDataStorage dataStorage = level.getDataStorage();
		//? if <=1.20.1 {
		/*PersitanceHandler handler = dataStorage.computeIfAbsent(
				PersitanceHandler::load,
				PersitanceHandler::new,
				Neruina.MOD_ID
		);
		*///?} elif <=1.21.1 {
		/*PersitanceHandler handler = dataStorage.computeIfAbsent(type, Neruina.MOD_ID);
		 *///?} else {
		PersitanceHandler handler = dataStorage.computeIfAbsent(type);
		 //?}
		handler.setDirty();
	}

	public static ServerLevel getLevel() {
		return level;
	}

	private static PersitanceHandler load(CompoundTag tag) {
		DataResult<PersitanceHandler> dataResult = CODEC.parse(NbtOps.INSTANCE, tag);
		//? if >1.20.1 {
		if (dataResult.isSuccess()) {
			return dataResult.getOrThrow();
		} else {
			Neruina.LOGGER.warn("Failed to load persitance handler. {}", dataResult.error().orElseThrow());
			return new PersitanceHandler();
		}
		//?} else {
		/*Either<PersitanceHandler, DataResult.PartialResult<PersitanceHandler>> either = dataResult.get();
		if (either.left().isPresent()) {
			return either.left().get();
		} else {
			Neruina.LOGGER.warn("Failed to load persitance handler. {}", either.right().orElseThrow());
			return new PersitanceHandler();
		}
		*///?}
	}

	private List<TickingEntry> getTickingEntries() {
		return Neruina.getInstance()
				.getTickHandler()
				.getTickingEntries()
				.stream()
				.filter(TickingEntry::isPersitent)
				.toList();
	}

	//? if >1.20.1 {
	public @NotNull CompoundTag save(@NotNull CompoundTag tag, HolderLookup.Provider registries) {
		DataResult<Tag> dataResult = CODEC.encode(this, NbtOps.INSTANCE, tag);
		return (CompoundTag) dataResult.getOrThrow();
	}
	//?} else {
	/*public CompoundTag save(CompoundTag compoundTag) {
		DataResult<Tag> dataResult = CODEC.encode(this, NbtOps.INSTANCE, compoundTag);
		return (CompoundTag) dataResult.get().left().orElseThrow();
	}
	*///?}
}
