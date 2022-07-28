package fr.max2.deepmagic.data;

import javax.annotation.Nonnull;

import fr.max2.deepmagic.DeepMagicMod;
import fr.max2.deepmagic.init.ModBlocks;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

public class ModBlockStateProvider extends BlockStateProvider
{

	public ModBlockStateProvider(DataGenerator generator, ExistingFileHelper existingFileHelper)
	{
		super(generator, DeepMagicMod.MOD_ID, existingFileHelper);
	}

	@Override
	protected void registerStatesAndModels()
	{
		simpleBlock(ModBlocks.TRANSPORTATION_BLOCK.get(), models().getExistingFile(new ResourceLocation(DeepMagicMod.MOD_ID, "block/black_hole_altar")));
	}

	@Override
	@Nonnull
	public String getName()
	{
		return "DeepMagic Block Models";
	}

}
