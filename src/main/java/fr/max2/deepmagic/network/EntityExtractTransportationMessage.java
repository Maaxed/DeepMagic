package fr.max2.deepmagic.network;

import java.util.function.Supplier;

import fr.max2.deepmagic.capability.BaseTransportationHandler;
import fr.max2.deepmagic.capability.CapabilityTransportationHandler;
import fr.max2.deepmagic.capability.ITransportationHandler.TransportStack;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

public class EntityExtractTransportationMessage
{
	private final int entityId;
	private final int index;

	private EntityExtractTransportationMessage(int entityId, int index)
	{
		this.entityId = entityId;
		this.index = index;
	}

	public EntityExtractTransportationMessage(Entity entity, int index)
	{
		this(entity.getId(), index);
	}

	public void encode(FriendlyByteBuf buf)
	{
		buf.writeInt(this.entityId);
		buf.writeInt(this.index);
	}

	public static EntityExtractTransportationMessage decode(FriendlyByteBuf buf)
	{
		return new EntityExtractTransportationMessage(
			buf.readInt(),
			buf.readInt()
		);
	}

	public void handleMainThread(Supplier<NetworkEvent.Context> ctx)
	{
    	DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientHandler.handle(this));
	}

	// Client only code
	private static class ClientHandler
	{
		public static void handle(EntityExtractTransportationMessage msg)
		{
			Level lvl = Minecraft.getInstance().level;
			if (lvl == null)
				return;

			Entity entity = lvl.getEntity(msg.entityId);
			if (entity == null)
				return;

			entity.getCapability(CapabilityTransportationHandler.TRANSPORTATION_HANDLER_CAPABILITY).ifPresent(transportation ->
			{
				if (transportation instanceof BaseTransportationHandler bth)
				{
					bth.extractItemAt(msg.index);
				}
			});
		}
	}
}
