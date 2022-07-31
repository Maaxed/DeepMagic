package fr.maaxed.gravitationalsorcery.network;

import java.util.function.Supplier;

import fr.maaxed.gravitationalsorcery.capability.BaseGravitationHandler;
import fr.maaxed.gravitationalsorcery.capability.CapabilityGravitationHandler;
import fr.maaxed.gravitationalsorcery.capability.BaseGravitationHandler.TransportStack;
import fr.maaxed.gravitationalsorcery.util.CapabilityProviderHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

public class InsertGravitationMessage
{
	private final CapabilityProviderHolder capabilityHolder;
	private final ItemStack stack;
	private final Vec3 pos;

	private InsertGravitationMessage(CapabilityProviderHolder capabilityHolder, ItemStack stack, Vec3 pos)
	{
		this.capabilityHolder = capabilityHolder;
		this.stack = stack;
		this.pos = pos;
	}

	public InsertGravitationMessage(CapabilityProviderHolder capabilityHolder, TransportStack stack)
	{
		this(capabilityHolder, stack.getStack(), stack.getOriginPosition());
	}

	public void encode(FriendlyByteBuf buf)
	{
		this.capabilityHolder.encode(buf);
		buf.writeItem(this.stack);
		buf.writeDouble(this.pos.x);
		buf.writeDouble(this.pos.y);
		buf.writeDouble(this.pos.z);
	}

	public static InsertGravitationMessage decode(FriendlyByteBuf buf)
	{
		return new InsertGravitationMessage(
			CapabilityProviderHolder.decode(buf),
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
		public static void handle(InsertGravitationMessage msg)
		{
			Level lvl = Minecraft.getInstance().level;
			if (lvl == null)
				return;

			msg.capabilityHolder.init(lvl);
			ICapabilityProvider capaProvider = msg.capabilityHolder.getCapabilityProvider();
			if (capaProvider == null)
				return;

			capaProvider.getCapability(CapabilityGravitationHandler.GRAVITATION_HANDLER_CAPABILITY).ifPresent(transportation ->
			{
				if (transportation instanceof BaseGravitationHandler bth)
				{
					bth.insertItem(msg.stack, msg.pos);
				}
			});
		}
	}
}
