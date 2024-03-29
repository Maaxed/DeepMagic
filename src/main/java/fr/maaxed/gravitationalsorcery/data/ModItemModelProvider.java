package fr.maaxed.gravitationalsorcery.data;

import javax.annotation.Nonnull;

import fr.maaxed.gravitationalsorcery.GravitationalSorceryMod;
import fr.maaxed.gravitationalsorcery.init.ModBlocks;
import fr.maaxed.gravitationalsorcery.init.ModItems;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.client.model.generators.ItemModelProvider;

public class ModItemModelProvider extends ItemModelProvider
{
	public ModItemModelProvider(DataGenerator generator, ExistingFileHelper existingFileHelper)
	{
		super(generator, GravitationalSorceryMod.MOD_ID, existingFileHelper);
	}

	@Override
	protected void registerModels()
	{
		simpleItem(name(ModItems.BLACK_HOLE.get()), new ResourceLocation(GravitationalSorceryMod.MOD_ID, "item/event_horizon4"));
		simpleBlock(ModBlocks.BLACK_HOLE_ALTAR_BLOCK.get());
	}

	protected ItemModelBuilder simpleItem(ItemLike entry)
	{
		return simpleItem(name(entry), itemTexture(entry));
	}

	protected ItemModelBuilder simpleItem(String modelName, ResourceLocation texture)
	{
		return singleTexture(modelName, mcLoc("item/generated"), "layer0", texture);
	}

	protected void simpleBlock(Block block)
	{
		withExistingParent(name(block), blockModel(block));
	}

	protected ResourceLocation blockModel(Block block)
	{
		ResourceLocation name = ForgeRegistries.BLOCKS.getKey(block);
		return new ResourceLocation(name.getNamespace(), BLOCK_FOLDER + "/" + name.getPath());
	}

	protected ResourceLocation itemTexture(ItemLike entry)
	{
		ResourceLocation name = ForgeRegistries.ITEMS.getKey(entry.asItem());
		return new ResourceLocation(name.getNamespace(), (entry instanceof Block ? BLOCK_FOLDER : ITEM_FOLDER) + "/" + name.getPath());
	}

	protected String name(ItemLike entry)
	{
		return ForgeRegistries.ITEMS.getKey(entry.asItem()).getPath();
	}

	@Override
	@Nonnull
	public String getName()
	{
		return "GravitationalSorcery Item Models";
	}
}
