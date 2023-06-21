package com.bawnorton.neruina_test.fabric;

import com.bawnorton.neruina_test.NeruinaTest;
import com.bawnorton.neruina_test.block.CrashingBlock;
import com.bawnorton.neruina_test.block.CrashingRandomTickBlock;
import com.bawnorton.neruina_test.block.entity.CrashingBlockEntity;
import com.bawnorton.neruina_test.entity.CrashingEntity;
import com.bawnorton.neruina_test.item.CrashingItem;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;

public class NeruinaTestFabric implements ModInitializer {
    public static final EntityType<CrashingEntity> CRASHING_ENTITY = Registry.register(Registries.ENTITY_TYPE, NeruinaTest.id("crashing_entity"), EntityType.Builder.create(CrashingEntity::new, SpawnGroup.MISC).build(NeruinaTest.id("crashing_entity").toString()));

    public static final Block CRASHING_BLOCK = Registry.register(Registries.BLOCK, NeruinaTest.id("crashing_block"), new CrashingBlock(AbstractBlock.Settings.create()));
    public static final Block CRASHING_RANDOM_TICK_BLOCK = Registry.register(Registries.BLOCK, NeruinaTest.id("crashing_random_tick_block"), new CrashingRandomTickBlock(AbstractBlock.Settings.create().ticksRandomly()));

    public static final BlockEntityType<CrashingBlockEntity> CRASHING_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, NeruinaTest.id("crashing_block_entity"), BlockEntityType.Builder.create(CrashingBlockEntity::new, CRASHING_BLOCK).build(null));

    public static final Item CRASHING_BLOCK_ITEM = Registry.register(Registries.ITEM, NeruinaTest.id("crashing_block"), new BlockItem(CRASHING_BLOCK, new Item.Settings()));
    public static final Item CRASHING_RANDOM_TICK_BLOCK_ITEM = Registry.register(Registries.ITEM, NeruinaTest.id("crashing_random_tick_block"), new BlockItem(CRASHING_RANDOM_TICK_BLOCK, new Item.Settings()));
    public static final Item CRASHING_ITEM = Registry.register(Registries.ITEM, NeruinaTest.id("crashing_item"), new CrashingItem(new Item.Settings()));
    public static final Item CRASHING_ENTITY_SPAWN_EGG = Registry.register(Registries.ITEM, NeruinaTest.id("crashing_entity_spawn_egg"), new SpawnEggItem(CRASHING_ENTITY, 0xF15F12, 0x3EDC89, new Item.Settings()));

    private static final RegistryKey<ItemGroup> ITEM_GROUP = RegistryKey.of(RegistryKeys.ITEM_GROUP, NeruinaTest.id("neruina_test_item_group"));

    @Override
    public void onInitialize() {
        NeruinaTest.init();

        Registry.register(Registries.ITEM_GROUP, ITEM_GROUP, FabricItemGroup.builder()
                .icon(() -> new ItemStack(CRASHING_BLOCK_ITEM))
                .displayName(Text.of("Neruina Test Items"))
                .build());

        ItemGroupEvents.modifyEntriesEvent(ITEM_GROUP).register((entries) -> {
            entries.add(CRASHING_BLOCK_ITEM);
            entries.add(CRASHING_RANDOM_TICK_BLOCK_ITEM);
            entries.add(CRASHING_ITEM);
            entries.add(CRASHING_ENTITY_SPAWN_EGG);
        });

        FabricDefaultAttributeRegistry.register(CRASHING_ENTITY, CrashingEntity.createMobAttributes());
    }
}
