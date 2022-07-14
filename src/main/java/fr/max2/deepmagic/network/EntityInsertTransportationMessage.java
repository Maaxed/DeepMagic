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

public class EntityInsertTransportationMessage
{
	private final int entityId;
	private final int index;
	private final ItemStack stack;
	private final Vec3 pos;

	private EntityInsertTransportationMessage(int entityId, int index, ItemStack stack, Vec3 pos)
	{
		this.entityId = entityId;
		this.index = index;
		this.stack = stack;
		this.pos = pos;
	}

	public EntityInsertTransportationMessage(Entity entity, int index, TransportStack stack)
	{
		this(entity.getId(), index, stack.getStack(), stack.getOriginPosition());
	}

	public void encode(FriendlyByteBuf buf)
	{
		buf.writeInt(this.entityId);
		buf.writeInt(this.index);
		buf.writeItem(this.stack);
		buf.writeDouble(this.pos.x);
		buf.writeDouble(this.pos.y);
		buf.writeDouble(this.pos.z);
	}

	public static EntityInsertTransportationMessage decode(FriendlyByteBuf buf)
	{
		return new EntityInsertTransportationMessage(
			buf.readInt(),
			buf.readInt(),
			buf.readItem(),
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
		public static void handle(EntityInsertTransportationMessage msg)
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
					bth.insertItemAt(msg.index, new TransportStack(msg.stack, msg.pos));
				}
			});
		}
	}
}
