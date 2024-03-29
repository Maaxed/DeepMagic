package fr.maaxed.gravitationalsorcery.data;

import java.util.function.Consumer;

import fr.maaxed.gravitationalsorcery.init.ModBlocks;
import fr.maaxed.gravitationalsorcery.init.ModItems;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

public class ModRecipeProvider extends RecipeProvider
{
	public ModRecipeProvider(DataGenerator generatorIn)
	{
		super(generatorIn);
	}

	@Override
	protected void buildCraftingRecipes(Consumer<FinishedRecipe> consumer)
	{
		ShapedRecipeBuilder.shaped(ModItems.BLACK_HOLE.get())
			.pattern(" S ")
			.pattern("SES")
			.pattern(" S ")
			.define('S', Items.ECHO_SHARD)
			.define('E', Items.ENDER_PEARL)
			.unlockedBy("has_echo_shard", has(Items.ECHO_SHARD))
			.save(consumer);
		ShapedRecipeBuilder.shaped(ModItems.BLACK_HOLE_WAND.get())
			.pattern(" H")
			.pattern("/ ")
			.define('H', ModItems.BLACK_HOLE.get())
			.define('/', Items.STICK)
			.unlockedBy("has_deep_dark_pearl", has(ModItems.BLACK_HOLE.get()))
			.save(consumer);
		ShapedRecipeBuilder.shaped(ModItems.CONFIGURATION_WAND.get())
			.pattern(" S")
			.pattern("/ ")
			.define('S', Items.ECHO_SHARD)
			.define('/', Items.STICK)
			.unlockedBy("has_deep_dark_pearl", has(ModItems.BLACK_HOLE.get()))
			.save(consumer);
		ShapedRecipeBuilder.shaped(ModBlocks.BLACK_HOLE_ALTAR_ITEM.get())
			.pattern(" H ")
			.pattern("S#S")
			.pattern("###")
			.define('H', ModItems.BLACK_HOLE.get())
			.define('S', Items.ECHO_SHARD)
			.define('#', Blocks.COBBLESTONE)
			.unlockedBy("has_deep_dark_pearl", has(ModItems.BLACK_HOLE.get()))
			.save(consumer);
	}

	@Override
	public String getName()
	{
		return "GravitationalSorcery Recipes";
	}
}
