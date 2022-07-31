package fr.maaxed.gravitationalsorcery.data;

import javax.annotation.Nonnull;

import fr.maaxed.gravitationalsorcery.GravitationalSorceryMod;
import fr.maaxed.gravitationalsorcery.init.ModBlocks;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

public class ModBlockStateProvider extends BlockStateProvider
{
	public ModBlockStateProvider(DataGenerator generator, ExistingFileHelper existingFileHelper)
	{
		super(generator, GravitationalSorceryMod.MOD_ID, existingFileHelper);
	}

	@Override
	protected void registerStatesAndModels()
	{
		simpleBlock(ModBlocks.BLACK_HOLE_ALTAR_BLOCK.get(), models().getExistingFile(new ResourceLocation(GravitationalSorceryMod.MOD_ID, "block/black_hole_altar")));
	}

	@Override
	@Nonnull
	public String getName()
	{
		return "GravitationalSorcery Block States";
	}
}
