package fr.max2.deepmagic.data;

import javax.annotation.Nonnull;

import fr.max2.deepmagic.DeepMagicMod;
import fr.max2.deepmagic.init.ModItems;
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
		super(generator, DeepMagicMod.MOD_ID, existingFileHelper);
	}

	@Override
	protected void registerModels()
	{
		simpleItem(ModItems.DEEP_DARK_DUST.get());
	}
	
	protected ItemModelBuilder simpleItem(ItemLike entry)
	{
		return singleTexture(name(entry), mcLoc("item/generated"), "layer0", itemTexture(entry));
	}
	
	protected ItemModelBuilder simpleItem(String modelName, ResourceLocation texture)
	{
		return singleTexture(modelName, mcLoc("item/generated"), "layer0", texture);
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
		return "DeepMagic Item Models";
	}
	
}
