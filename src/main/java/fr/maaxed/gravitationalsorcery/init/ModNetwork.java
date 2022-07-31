package fr.maaxed.gravitationalsorcery.init;

import fr.maaxed.gravitationalsorcery.GravitationalSorceryMod;
import fr.maaxed.gravitationalsorcery.network.BlockReplaceActionMessage;
import fr.maaxed.gravitationalsorcery.network.EntityReplaceTransportationMessage;
import fr.maaxed.gravitationalsorcery.network.ExtractTransportationMessage;
import fr.maaxed.gravitationalsorcery.network.InsertTransportationMessage;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

@EventBusSubscriber(modid = GravitationalSorceryMod.MOD_ID, bus = Bus.MOD)
public class ModNetwork
{
	private static final String PROTOCOL_VERSION = "1";

	public static final SimpleChannel CHANNEL =
		NetworkRegistry.ChannelBuilder
			.named(new ResourceLocation(GravitationalSorceryMod.MOD_ID, "main"))
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
