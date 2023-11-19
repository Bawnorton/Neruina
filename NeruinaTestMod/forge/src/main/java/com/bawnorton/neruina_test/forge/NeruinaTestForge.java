package com.bawnorton.neruina_test.forge;

import com.bawnorton.neruina_test.NeruinaTest;
import com.bawnorton.neruina_test.block.CrashingBlock;
import com.bawnorton.neruina_test.block.CrashingRandomTickBlock;
import com.bawnorton.neruina_test.block.entity.CrashingBlockEntity;
import com.bawnorton.neruina_test.entity.CrashingEntity;
import com.bawnorton.neruina_test.item.CrashingItem;
import dev.architectury.platform.forge.EventBuses;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod(NeruinaTest.MOD_ID)
public class NeruinaTestForge {
    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, NeruinaTest.MOD_ID);
    public static final RegistryObject<Block> CRASHING_BLOCK = BLOCKS.register("crashing_block", () -> new CrashingBlock(AbstractBlock.Settings.create()));
    public static final RegistryObject<Block> CRASHING_RANDOM_TICK_BLOCK = BLOCKS.register("crashing_random_tick_block", () -> new CrashingRandomTickBlock(AbstractBlock.Settings.create().ticksRandomly()));
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, NeruinaTest.MOD_ID);
    public static final RegistryObject<Item> CRASHING_BLOCK_ITEM = ITEMS.register("crashing_block", () -> new BlockItem(CRASHING_BLOCK.get(), new Item.Settings()));
    public static final RegistryObject<Item> CRASHING_RANDOM_TICK_BLOCK_ITEM = ITEMS.register("crashing_random_tick_block", () -> new BlockItem(CRASHING_RANDOM_TICK_BLOCK.get(), new Item.Settings()));
    public static final RegistryObject<Item> CRASHING_ITEM = ITEMS.register("crashing_item", () -> new CrashingItem(new Item.Settings()));
    private static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, NeruinaTest.MOD_ID);
    public static final RegistryObject<EntityType<CrashingEntity>> CRASHING_ENTITY = ENTITIES.register("crashing_entity", () -> EntityType.Builder.create(CrashingEntity::new, SpawnGroup.MISC).build(NeruinaTest.id("crashing_entity").toString()));
    public static final RegistryObject<Item> CRASHING_ENTITY_SPAWN_EGG = ITEMS.register("crashing_entity_spawn_egg", () -> new ForgeSpawnEggItem(CRASHING_ENTITY, 0xF15F12, 0x3EDC89, new Item.Settings()));
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, NeruinaTest.MOD_ID);
    public static final RegistryObject<BlockEntityType<CrashingBlockEntity>> CRASHING_BLOCK_ENTITY = BLOCK_ENTITIES.register("crashing_block_entity", () -> BlockEntityType.Builder.create(CrashingBlockEntity::new, CRASHING_BLOCK.get()).build(null));

    public NeruinaTestForge() {
        EventBuses.registerModEventBus(NeruinaTest.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
        ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
        ENTITIES.register(FMLJavaModLoadingContext.get().getModEventBus());
        BLOCK_ENTITIES.register(FMLJavaModLoadingContext.get().getModEventBus());

        NeruinaTest.init();
        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> NeruinaTestClientForge::new);
    }

    @SubscribeEvent
    public static void registerEntityAttributes(EntityAttributeCreationEvent event) {
        event.put(CRASHING_ENTITY.get(), CrashingEntity.createMobAttributes().build());
    }
}
