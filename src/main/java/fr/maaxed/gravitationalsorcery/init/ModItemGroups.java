package fr.maaxed.gravitationalsorcery.init;

import fr.maaxed.gravitationalsorcery.GravitationalSorceryMod;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class ModItemGroups
{
	public static final CreativeModeTab MAIN_TAB = new CreativeModeTab(GravitationalSorceryMod.MOD_ID + ".main_tab")
	{
		@Override
		public ItemStack makeIcon()
		{
			return new ItemStack(ModItems.DEEP_DARK_PEARL.get());
		}
	};
}
