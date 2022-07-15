package fr.max2.deepmagic.data;

import fr.max2.deepmagic.DeepMagicMod;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(modid = DeepMagicMod.MOD_ID, bus = Bus.MOD)
public class ModDataProviders
{
	@SubscribeEvent
	public static void gatherData(GatherDataEvent event)
	{
		DataGenerator gen = event.getGenerator();
		ExistingFileHelper files = event.getExistingFileHelper();

		gen.addProvider(event.includeServer(), new ModRecipeProvider(gen));

		gen.addProvider(event.includeClient(), new ModItemModelProvider(gen, files));
		gen.addProvider(event.includeClient(), new ModLanguagesProvider(gen));
	}
}
