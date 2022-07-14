package fr.max2.deepmagic.network;

import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import fr.max2.deepmagic.capability.BaseTransportationHandler;
import fr.max2.deepmagic.capability.CapabilityTransportationHandler;
import fr.max2.deepmagic.capability.BaseTransportationHandler.TransportStack;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

public class EntityExtractTransportationMessage
{
	private final int entityId;
	private final int index;
	private final Vec3 targetPos;

	private EntityExtractTransportationMessage(int entityId, int index, Vec3 targetPos)
	{
		this.entityId = entityId;
		this.index = index;
		this.targetPos = targetPos;
	}

	public EntityExtractTransportationMessage(Entity entity, int index, TransportStack stack)
	{
		this(entity.getId(), index, stack.getTargetPosition());
	}

	public void encode(FriendlyByteBuf buf)
	{
		buf.writeInt(this.entityId);
		buf.writeInt(this.index);
		buf.writeDouble(this.targetPos.x);
		buf.writeDouble(this.targetPos.y);
		buf.writeDouble(this.targetPos.z);
	}

	public static EntityExtractTransportationMessage decode(FriendlyByteBuf buf)
	{
		return new EntityExtractTransportationMessage(
			buf.readInt(),
			buf.readInt(),
			new Vec3(
				buf.readDouble(),
				buf.readDouble(),
				buf.readDouble()
			)
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
					bth.extractItemAt(msg.index, msg.targetPos);
				}
			});
		}
	}
}
