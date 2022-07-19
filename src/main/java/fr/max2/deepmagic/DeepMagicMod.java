package fr.max2.deepmagic;

import fr.max2.deepmagic.init.ModBlocks;
import fr.max2.deepmagic.init.ModItems;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(DeepMagicMod.MOD_ID)
public class DeepMagicMod
{
	public static final String MOD_ID = "deepmagic";

	public DeepMagicMod()
	{
		IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
		ModItems.REGISTRY.register(modBus);
		ModBlocks.BLOCK_REGISTRY.register(modBus);
		ModBlocks.BLOCKENTITY_REGISTRY.register(modBus);
	}
}
