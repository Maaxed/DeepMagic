package fr.max2.deepmagic.init;

import fr.max2.deepmagic.DeepMagicMod;
import fr.max2.deepmagic.network.EntityExtractTransportationMessage;
import fr.max2.deepmagic.network.EntityInsertTransportationMessage;
import fr.max2.deepmagic.network.EntityReplaceTransportationMessage;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

@EventBusSubscriber(modid = DeepMagicMod.MOD_ID, bus = Bus.MOD)
public class ModNetwork
{
	private static final String PROTOCOL_VERSION = "1";

	public static final SimpleChannel CHANNEL =
		NetworkRegistry.ChannelBuilder
			.named(new ResourceLocation(DeepMagicMod.MOD_ID, "main"))
			.networkProtocolVersion(() -> PROTOCOL_VERSION)
			.serverAcceptedVersions(PROTOCOL_VERSION::equals)
			.clientAcceptedVersions(PROTOCOL_VERSION::equals)
			.simpleChannel();

	@SubscribeEvent
	public static void registerPackets(FMLCommonSetupEvent event)
	{
		int id = 0;
		CHANNEL
			.messageBuilder(EntityInsertTransportationMessage.class, id++, NetworkDirection.PLAY_TO_CLIENT)
			.encoder(EntityInsertTransportationMessage::encode)
			.decoder(EntityInsertTransportationMessage::decode)
			.consumerMainThread(EntityInsertTransportationMessage::handleMainThread)
			.add();
		CHANNEL
			.messageBuilder(EntityExtractTransportationMessage.class, id++, NetworkDirection.PLAY_TO_CLIENT)
			.encoder(EntityExtractTransportationMessage::encode)
			.decoder(EntityExtractTransportationMessage::decode)
			.consumerMainThread(EntityExtractTransportationMessage::handleMainThread)
			.add();
		CHANNEL
			.messageBuilder(EntityReplaceTransportationMessage.class, id++, NetworkDirection.PLAY_TO_CLIENT)
			.encoder(EntityReplaceTransportationMessage::encode)
			.decoder(EntityReplaceTransportationMessage::decode)
			.consumerMainThread(EntityReplaceTransportationMessage::handleMainThread)
			.add();
	}
}
