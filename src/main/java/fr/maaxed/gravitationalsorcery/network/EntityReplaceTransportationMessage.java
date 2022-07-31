package fr.maaxed.gravitationalsorcery.network;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import fr.maaxed.gravitationalsorcery.capability.BaseTransportationHandler;
import fr.maaxed.gravitationalsorcery.capability.CapabilityTransportationHandler;
import fr.maaxed.gravitationalsorcery.capability.BaseTransportationHandler.TransportStack;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

public class EntityReplaceTransportationMessage
{
	private final int entityId;
	private final List<TransportStack> stacks;

	private EntityReplaceTransportationMessage(int entityId, List<TransportStack> stacks)
	{
		this.entityId = entityId;
		this.stacks = stacks;
	}

	public EntityReplaceTransportationMessage(Entity entity, BaseTransportationHandler handler)
	{
		this(entity.getId(), handler.getContent());
	}

	public void encode(FriendlyByteBuf buf)
	{
		buf.writeInt(this.entityId);
		buf.writeInt(this.stacks.size());
		for (var stack : this.stacks)
		{
			if (stack == null)
			{
				buf.writeBoolean(false);
			}
			else
			{
				buf.writeBoolean(true);
				buf.writeItem(stack.getStack());
				buf.writeDouble(stack.getOriginPosition().x);
				buf.writeDouble(stack.getOriginPosition().y);
				buf.writeDouble(stack.getOriginPosition().z);
				buf.writeInt(stack.getTicksAlive());
			}
		}
	}

	public static EntityReplaceTransportationMessage decode(FriendlyByteBuf buf)
	{
		int entityId = buf.readInt();
		int count = buf.readInt();
		List<TransportStack> stacks = new ArrayList<>();
		for (int i = 0; i < count; i++)
		{
			if (!buf.readBoolean())
			{
				stacks.add(null);
			}
			else
			{
				stacks.add(new TransportStack(
					buf.readItem(),
					new Vec3(
						buf.readDouble(),
						buf.readDouble(),
						buf.readDouble()
					),
					buf.readInt()
				));
			}
		}
		return new EntityReplaceTransportationMessage(entityId, stacks);
	}

	public void handleMainThread(Supplier<NetworkEvent.Context> ctx)
	{
		DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientHandler.handle(this));
	}

	// Client only code
	private static class ClientHandler
	{
		public static void handle(EntityReplaceTransportationMessage msg)
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
					bth.replaceContent(msg.stacks);
				}
			});
		}
	}
}
