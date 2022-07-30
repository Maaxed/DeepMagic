package fr.max2.deepmagic.network;

import java.util.function.Supplier;

import fr.max2.deepmagic.capability.BaseTransportationHandler;
import fr.max2.deepmagic.capability.CapabilityTransportationHandler;
import fr.max2.deepmagic.capability.BaseTransportationHandler.TransportStack;
import fr.max2.deepmagic.util.CapabilityProviderHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

public class ExtractTransportationMessage
{
	private final CapabilityProviderHolder capabilityHolder;
	private final int count;
	private final Vec3 targetPos;

	private ExtractTransportationMessage(CapabilityProviderHolder capabilityHolder, int count, Vec3 targetPos)
	{
		this.capabilityHolder = capabilityHolder;
		this.count = count;
		this.targetPos = targetPos;
	}

	public ExtractTransportationMessage(CapabilityProviderHolder capabilityHolder, TransportStack stack)
	{
		this(capabilityHolder, stack.getStack().getCount(), stack.getTargetPosition());
	}

	public void encode(FriendlyByteBuf buf)
	{
		this.capabilityHolder.encode(buf);
		buf.writeInt(this.count);
		buf.writeDouble(this.targetPos.x);
		buf.writeDouble(this.targetPos.y);
		buf.writeDouble(this.targetPos.z);
	}

	public static ExtractTransportationMessage decode(FriendlyByteBuf buf)
	{
		return new ExtractTransportationMessage(
			CapabilityProviderHolder.decode(buf),
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
		public static void handle(ExtractTransportationMessage msg)
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
					bth.extractItem(msg.count, msg.targetPos, false);
				}
			});
		}
	}
}
