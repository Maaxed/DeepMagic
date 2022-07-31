package fr.maaxed.gravitationalsorcery.init;

import fr.maaxed.gravitationalsorcery.GravitationalSorceryMod;
import fr.maaxed.gravitationalsorcery.network.BlockReplaceActionMessage;
import fr.maaxed.gravitationalsorcery.network.EntityReplaceGravitationMessage;
import fr.maaxed.gravitationalsorcery.network.ExtractGravitationMessage;
import fr.maaxed.gravitationalsorcery.network.InsertGravitationMessage;
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
			.messageBuilder(InsertGravitationMessage.class, id++, NetworkDirection.PLAY_TO_CLIENT)
			.encoder(InsertGravitationMessage::encode)
			.decoder(InsertGravitationMessage::decode)
			.consumerMainThread(InsertGravitationMessage::handleMainThread)
			.add();
		CHANNEL
			.messageBuilder(ExtractGravitationMessage.class, id++, NetworkDirection.PLAY_TO_CLIENT)
			.encoder(ExtractGravitationMessage::encode)
			.decoder(ExtractGravitationMessage::decode)
			.consumerMainThread(ExtractGravitationMessage::handleMainThread)
			.add();
		CHANNEL
			.messageBuilder(EntityReplaceGravitationMessage.class, id++, NetworkDirection.PLAY_TO_CLIENT)
			.encoder(EntityReplaceGravitationMessage::encode)
			.decoder(EntityReplaceGravitationMessage::decode)
			.consumerMainThread(EntityReplaceGravitationMessage::handleMainThread)
			.add();
		CHANNEL
			.messageBuilder(BlockReplaceActionMessage.class, id++, NetworkDirection.PLAY_TO_CLIENT)
			.encoder(BlockReplaceActionMessage::encode)
			.decoder(BlockReplaceActionMessage::decode)
			.consumerMainThread(BlockReplaceActionMessage::handleMainThread)
			.add();
	}
}
