package fr.max2.deepmagic;

import static fr.max2.deepmagic.DeepMagicMod.*;

import fr.max2.deepmagic.init.ModItems;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(MOD_ID)
public class DeepMagicMod
{
	public static final String MOD_ID = "deepmagic";
	
	public DeepMagicMod()
	{
		ModItems.REGISTRY.register(FMLJavaModLoadingContext.get().getModEventBus());
	}
}
