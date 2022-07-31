package fr.maaxed.gravitationalsorcery.init;

import java.util.function.Supplier;

import fr.maaxed.gravitationalsorcery.GravitationalSorceryMod;
import fr.maaxed.gravitationalsorcery.block.BlackHoleAltarBlock;
import fr.maaxed.gravitationalsorcery.block.BlackHoleAltarBlockEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@EventBusSubscriber(bus = Bus.MOD, modid = GravitationalSorceryMod.MOD_ID)
public class ModBlocks
{
	public static final DeferredRegister<Block> BLOCK_REGISTRY = DeferredRegister.create(ForgeRegistries.BLOCKS, GravitationalSorceryMod.MOD_ID);
	public static final RegistryObject<Block> BLACK_HOLE_ALTAR_BLOCK = register("black_hole_altar", () -> new BlackHoleAltarBlock(Properties.of(Material.STONE).strength(3.0F, 9.0F)));

	public static final RegistryObject<Item> BLACK_HOLE_ALTAR_ITEM = item(BLACK_HOLE_ALTAR_BLOCK);

	public static final DeferredRegister<BlockEntityType<?>> BLOCKENTITY_REGISTRY = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, GravitationalSorceryMod.MOD_ID);
	public static final RegistryObject<BlockEntityType<BlackHoleAltarBlockEntity>> BLACK_HOLE_ALTAR_BLOCKENTITY = blockEntity(BLACK_HOLE_ALTAR_BLOCK, BlackHoleAltarBlockEntity::new);

	private static <B extends Block> RegistryObject<B> register(String name, Supplier<B> item)
	{
		return BLOCK_REGISTRY.register(name, item);
	}

	private static RegistryObject<Item> item(RegistryObject<? extends Block> block)
	{
		return ModItems.register(block.getId().getPath(), props -> new BlockItem(block.get(), props));
	}

	private static <BE extends BlockEntity> RegistryObject<BlockEntityType<BE>> blockEntity(RegistryObject<? extends Block> blocks, BlockEntityType.BlockEntitySupplier<? extends BE> blockEntity)
	{
		return BLOCKENTITY_REGISTRY.register(blocks.getId().getPath(), () -> BlockEntityType.Builder.<BE>of(blockEntity, blocks.get()).build(null));
	}
}
