package fr.max2.deepmagic.capability;

import fr.max2.deepmagic.DeepMagicMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(modid = DeepMagicMod.MOD_ID, bus = Bus.MOD)
public class CapabilityTransportationHandler
{
	public static final Capability<ITransportationHandler> TRANSPORTATION_HANDLER_CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});
	
	@SubscribeEvent
	public static void registerCapability(RegisterCapabilitiesEvent event)
	{
		event.register(ITransportationHandler.class);
	}
	
	@EventBusSubscriber(modid = DeepMagicMod.MOD_ID, bus = Bus.FORGE)
	public static class Events
	{
		@SubscribeEvent
		public static void attachCapability(AttachCapabilitiesEvent<Entity> event)
		{
			if (!(event.getObject() instanceof Player))
				return;
			
			TransportationCapabilityProvider<?, ?> capability = new TransportationCapabilityProvider<>(new BaseTransportationHandler(16));

			event.addListener(capability::invalidate);
			event.addCapability(new ResourceLocation(DeepMagicMod.MOD_ID, "main_transportation"), capability);
		}
	}
}
