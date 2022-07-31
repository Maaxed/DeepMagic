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
		ShapedRecipeBuilder.shaped(ModItems.DEEP_DARK_PEARL.get())
			.pattern("SSS")
			.pattern("SES")
			.pattern("SSS")
			.define('S', Items.ECHO_SHARD)
			.define('E', Items.ENDER_PEARL)
			.unlockedBy("has_echo_shard", has(Items.ECHO_SHARD))
			.save(consumer);
		ShapedRecipeBuilder.shaped(ModItems.TRANSPORTATION_WAND.get())
			.pattern(" P")
			.pattern("/ ")
			.define('P', ModItems.DEEP_DARK_PEARL.get())
			.define('/', Items.STICK)
			.unlockedBy("has_deep_dark_pearl", has(ModItems.DEEP_DARK_PEARL.get()))
			.save(consumer);
		ShapedRecipeBuilder.shaped(ModItems.CONFIGURATION_WAND.get())
			.pattern(" S")
			.pattern("/ ")
			.define('S', Items.ECHO_SHARD)
			.define('/', Items.STICK)
			.unlockedBy("has_deep_dark_pearl", has(ModItems.DEEP_DARK_PEARL.get()))
			.save(consumer);
		ShapedRecipeBuilder.shaped(ModBlocks.TRANSPORTATION_BLOCK_ITEM.get())
			.pattern(" P ")
			.pattern("S#S")
			.pattern("###")
			.define('P', ModItems.DEEP_DARK_PEARL.get())
			.define('S', Items.ECHO_SHARD)
			.define('#', Blocks.COBBLESTONE)
			.unlockedBy("has_deep_dark_pearl", has(ModItems.DEEP_DARK_PEARL.get()))
			.save(consumer);
	}

	@Override
	public String getName()
	{
		return "GravitationalSorcery Recipes";
	}

}
