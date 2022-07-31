package fr.maaxed.gravitationalsorcery.capability;

import fr.maaxed.gravitationalsorcery.GravitationalSorceryMod;
import fr.maaxed.gravitationalsorcery.init.ModNetwork;
import fr.maaxed.gravitationalsorcery.network.EntityReplaceGravitationMessage;
import fr.maaxed.gravitationalsorcery.util.CapabilityProviderHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.network.PacketDistributor;

@EventBusSubscriber(modid = GravitationalSorceryMod.MOD_ID, bus = Bus.MOD)
public class CapabilityGravitationHandler
{
	public static final Capability<IGravitationHandler> GRAVITATION_HANDLER_CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});

	@SubscribeEvent
	public static void registerCapability(RegisterCapabilitiesEvent event)
	{
		event.register(IGravitationHandler.class);
	}

	@EventBusSubscriber(modid = GravitationalSorceryMod.MOD_ID, bus = Bus.FORGE)
	public static class Events
	{
		@SubscribeEvent
		public static void attachCapability(AttachCapabilitiesEvent<Entity> event)
		{
			if (!(event.getObject() instanceof Player player))
				return;

			int size = 16;
			BaseGravitationHandler handler = player.level.isClientSide ? new ClientTransportationHandler(size) : new SyncGravitationHandler(size, CapabilityProviderHolder.entity(player));
			GravitationCapabilityProvider<?, ?> capability = new GravitationCapabilityProvider<>(handler);

			event.addListener(capability::invalidate);
			event.addCapability(new ResourceLocation(GravitationalSorceryMod.MOD_ID, "main_transportation"), capability);
		}

		@SubscribeEvent
		public static void syncCapability(PlayerLoggedInEvent event)
		{
			// Sync capability data
			if (!(event.getEntity() instanceof ServerPlayer player))
				return;

			player.getCapability(GRAVITATION_HANDLER_CAPABILITY).ifPresent(transportation ->
			{
				if (!(transportation instanceof BaseGravitationHandler bth))
					return;

				ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new EntityReplaceGravitationMessage(player, bth));
			});
		}

		@SubscribeEvent
		public static void trackCapability(PlayerEvent.StartTracking event)
		{
			// Sync capability data
			if (!(event.getEntity() instanceof ServerPlayer player))
				return;

			Entity target = event.getTarget();
			target.getCapability(GRAVITATION_HANDLER_CAPABILITY).ifPresent(transportation ->
			{
				if (!(transportation instanceof BaseGravitationHandler bth))
					return;

				ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new EntityReplaceGravitationMessage(target, bth));
			});
		}

		@SubscribeEvent
		public static void tickCapability(PlayerTickEvent event)
		{
			if (event.phase != Phase.END || event.side != LogicalSide.CLIENT)
				return;

			event.player.getCapability(GRAVITATION_HANDLER_CAPABILITY).ifPresent(transportation ->
			{
				if (!(transportation instanceof BaseGravitationHandler bth))
					return;

				bth.update();
			});
		}

		@SubscribeEvent
		public static void dropCapability(LivingDropsEvent event)
		{
			// Drop the content of the transportation capability
			LivingEntity entity = event.getEntity();
			entity.getCapability(GRAVITATION_HANDLER_CAPABILITY).ifPresent(transportation ->
			{
				if (!(transportation instanceof BaseGravitationHandler bth))
					return;

				for (ItemStack stack : bth.getDrops())
				{
					if (stack.isEmpty())
						continue;

					event.getDrops().add(entity.spawnAtLocation(stack));
				}
			});
		}
	}
}
