package fr.maaxed.gravitationalsorcery;

import fr.maaxed.gravitationalsorcery.init.ModBlocks;
import fr.maaxed.gravitationalsorcery.init.ModItems;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(GravitationalSorceryMod.MOD_ID)
public class GravitationalSorceryMod
{
	public static final String MOD_ID = "gravitationalsorcery";

	public GravitationalSorceryMod()
	{
		IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
		ModItems.REGISTRY.register(modBus);
		ModBlocks.BLOCK_REGISTRY.register(modBus);
		ModBlocks.BLOCKENTITY_REGISTRY.register(modBus);
	}
}
