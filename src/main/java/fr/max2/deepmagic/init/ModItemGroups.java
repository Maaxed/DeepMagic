package fr.max2.deepmagic.init;

import fr.max2.deepmagic.DeepMagicMod;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class ModItemGroups
{
	public static final CreativeModeTab MAIN_TAB = new CreativeModeTab(DeepMagicMod.MOD_ID + ".main_tab")
	{
		@Override
		public ItemStack makeIcon()
		{
			return new ItemStack(ModItems.DEEP_DARK_PEARL.get());
		}
	};
}
