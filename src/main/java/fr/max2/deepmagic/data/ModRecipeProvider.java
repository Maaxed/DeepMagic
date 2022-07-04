package fr.max2.deepmagic.data;

import java.util.function.Consumer;

import fr.max2.deepmagic.init.ModItems;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.Items;

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
			.define('E', Items.ENDER_EYE)
			.unlockedBy("has_echo_shard", has(Items.ECHO_SHARD))
			.save(consumer);
	}
	
	@Override
	public String getName()
	{
		return "DeepMagic Recipes";
	}
	
}
