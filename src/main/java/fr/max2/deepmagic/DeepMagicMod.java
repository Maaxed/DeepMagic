package fr.max2.deepmagic;

import fr.max2.deepmagic.init.ModItems;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(DeepMagicMod.MOD_ID)
public class DeepMagicMod
{
	public static final String MOD_ID = "deepmagic";

	public DeepMagicMod()
	{
		ModItems.REGISTRY.register(FMLJavaModLoadingContext.get().getModEventBus());
	}
}
