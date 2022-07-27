package fr.max2.deepmagic.init;

import fr.max2.deepmagic.DeepMagicMod;
import fr.max2.deepmagic.network.ExtractTransportationMessage;
import fr.max2.deepmagic.network.InsertTransportationMessage;
import fr.max2.deepmagic.network.BlockReplaceActionMessage;
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
			.messageBuilder(InsertTransportationMessage.class, id++, NetworkDirection.PLAY_TO_CLIENT)
			.encoder(InsertTransportationMessage::encode)
			.decoder(InsertTransportationMessage::decode)
			.consumerMainThread(InsertTransportationMessage::handleMainThread)
			.add();
		CHANNEL
			.messageBuilder(ExtractTransportationMessage.class, id++, NetworkDirection.PLAY_TO_CLIENT)
			.encoder(ExtractTransportationMessage::encode)
			.decoder(ExtractTransportationMessage::decode)
			.consumerMainThread(ExtractTransportationMessage::handleMainThread)
			.add();
		CHANNEL
			.messageBuilder(EntityReplaceTransportationMessage.class, id++, NetworkDirection.PLAY_TO_CLIENT)
			.encoder(EntityReplaceTransportationMessage::encode)
			.decoder(EntityReplaceTransportationMessage::decode)
			.consumerMainThread(EntityReplaceTransportationMessage::handleMainThread)
			.add();
		CHANNEL
			.messageBuilder(BlockReplaceActionMessage.class, id++, NetworkDirection.PLAY_TO_CLIENT)
			.encoder(BlockReplaceActionMessage::encode)
			.decoder(BlockReplaceActionMessage::decode)
			.consumerMainThread(BlockReplaceActionMessage::handleMainThread)
			.add();
	}
}
