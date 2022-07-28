package fr.max2.deepmagic.network;

import java.util.function.Supplier;

import fr.max2.deepmagic.capability.BaseTransportationHandler;
import fr.max2.deepmagic.capability.CapabilityTransportationHandler;
import fr.max2.deepmagic.capability.BaseTransportationHandler.TransportStack;
import fr.max2.deepmagic.util.CapabilityProviderHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

public class InsertTransportationMessage
{
	private final CapabilityProviderHolder capabilityHolder;
	private final int index;
	private final ItemStack stack;
	private final Vec3 pos;

	private InsertTransportationMessage(CapabilityProviderHolder capabilityHolder, int index, ItemStack stack, Vec3 pos)
	{
		this.capabilityHolder = capabilityHolder;
		this.index = index;
		this.stack = stack;
		this.pos = pos;
	}

	public InsertTransportationMessage(CapabilityProviderHolder capabilityHolder, int index, TransportStack stack)
	{
		this(capabilityHolder, index, stack.getStack(), stack.getOriginPosition());
	}

	public void encode(FriendlyByteBuf buf)
	{
		this.capabilityHolder.encode(buf);
		buf.writeInt(this.index);
		buf.writeItem(this.stack);
		buf.writeDouble(this.pos.x);
		buf.writeDouble(this.pos.y);
		buf.writeDouble(this.pos.z);
	}

	public static InsertTransportationMessage decode(FriendlyByteBuf buf)
	{
		return new InsertTransportationMessage(
			CapabilityProviderHolder.decode(buf),
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
		public static void handle(InsertTransportationMessage msg)
		{
			Level lvl = Minecraft.getInstance().level;
			if (lvl == null)
				return;

			msg.capabilityHolder.init(lvl);
			ICapabilityProvider capaProvider = msg.capabilityHolder.getCapabilityProvider();
			if (capaProvider == null)
				return;

			capaProvider.getCapability(CapabilityTransportationHandler.TRANSPORTATION_HANDLER_CAPABILITY).ifPresent(transportation ->
			{
				if (transportation instanceof BaseTransportationHandler bth)
				{
					bth.insertItemAt(msg.index, new TransportStack(msg.stack, msg.pos));
				}
			});
		}
	}
}
