package com.bawnorton.neruina.data;

//? if fabric {

import dev.kikugie.fletching_table.annotation.fabric.Entrypoint;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.DetectedVersion;
import net.minecraft.data.metadata.PackMetadataGenerator;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.util.InclusiveRange;

import java.util.Optional;

@Entrypoint("fabric-datagen")
public final class NeruinaDataGen implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
		FabricDataGenerator.Pack mainPack = fabricDataGenerator.createPack();
		mainPack.addProvider((FabricDataGenerator.Pack.Factory<PackMetadataGenerator>) output -> new PackMetadataGenerator(output)
				.add(
						//? if >=1.21.10 {
						PackMetadataSection.SERVER_TYPE,
						new PackMetadataSection(
								Component.literal("${mod_description}"),
								InclusiveRange.create(
										DetectedVersion.BUILT_IN.packVersion(PackType.SERVER_DATA),
										DetectedVersion.BUILT_IN.packVersion(PackType.SERVER_DATA)
								).getOrThrow()
						)
						//?} else {
						/*PackMetadataSection.TYPE,
						new PackMetadataSection(
								//? if >1.20.1 {
								Component.literal("${mod_description}"),
								//? if >=1.21.6 {
								DetectedVersion.BUILT_IN.packVersion(PackType.SERVER_DATA),
								//?} else {
								/^DetectedVersion.BUILT_IN.getPackVersion(PackType.SERVER_DATA),
								^///?}
								Optional.empty()
								//?} else {
								/^Component.literal("${mod_description}"),
								DetectedVersion.BUILT_IN.getPackVersion(PackType.SERVER_DATA)
								^///?}
						)
						*///?}
				)
		);
	}
}
//?} else {
/*import com.bawnorton.neruina.Neruina;
import net.minecraft.DetectedVersion;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.metadata.PackMetadataGenerator;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.util.InclusiveRange;

import java.util.Optional;

//? if neoforge {
/^import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;
^///?} else {
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
//?}

@EventBusSubscriber(modid = Neruina.MOD_ID)
public final class NeruinaDataGen {
	@SubscribeEvent
	//? if >=1.21.5 {
	public static void gatherServerData(GatherDataEvent.Server event) {
		DataGenerator gen = event.getGenerator();
	//?} else {
	/^public static void gatherData(GatherDataEvent event) {
		DataGenerator gen = event.getGenerator();
	^///?}
		PackOutput mainPack = gen.getPackOutput();
		gen.addProvider(true, new PackMetadataGenerator(mainPack)
				.add(
						//? if >=1.21.10 {
						PackMetadataSection.SERVER_TYPE,
						new PackMetadataSection(
								Component.literal("${mod_description}"),
								InclusiveRange.create(
										DetectedVersion.BUILT_IN.packVersion(PackType.SERVER_DATA),
										DetectedVersion.BUILT_IN.packVersion(PackType.SERVER_DATA)
								).getOrThrow()
						)
						//?} else {
						/^PackMetadataSection.TYPE,
						new PackMetadataSection(
								//? if >1.20.1 {
								Component.literal("${mod_description}"),
								//? if >=1.21.6 {
								DetectedVersion.BUILT_IN.packVersion(PackType.SERVER_DATA),
								 //?} else {
								/^¹DetectedVersion.BUILT_IN.getPackVersion(PackType.SERVER_DATA),
								¹^///?}
								Optional.empty()
								//?} else {
								/^¹Component.literal("${mod_description}"),
								DetectedVersion.BUILT_IN.getPackVersion(PackType.SERVER_DATA)
								¹^///?}
						)
						^///?}
				)
		);
	}
}
*///?}