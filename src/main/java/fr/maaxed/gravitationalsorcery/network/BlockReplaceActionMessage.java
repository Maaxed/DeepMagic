package fr.maaxed.gravitationalsorcery.network;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import fr.maaxed.gravitationalsorcery.block.TransportationBlockEntity;
import fr.maaxed.gravitationalsorcery.block.TransportationBlockEntity.Action;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

public class BlockReplaceActionMessage
{
	private final BlockPos blockPos;
	private final List<TransportationBlockEntity.Action> actions;

	private BlockReplaceActionMessage(BlockPos blockPos, List<Action> actions)
	{
		this.blockPos = blockPos;
		this.actions = actions;
	}

	public BlockReplaceActionMessage(TransportationBlockEntity be)
	{
		this(be.getBlockPos(), be.getActions());
	}

	public void encode(FriendlyByteBuf buf)
	{
		buf.writeBlockPos(this.blockPos);
		buf.writeInt(this.actions.size());
		for (var action : this.actions)
		{
			buf.writeBlockPos(action.pos);
			buf.writeEnum(action.face);
			buf.writeBoolean(action.insert);
		}
	}

	public static BlockReplaceActionMessage decode(FriendlyByteBuf buf)
	{
		BlockPos blockPos = buf.readBlockPos();
		List<TransportationBlockEntity.Action> actions = new ArrayList<>();
		int count = buf.readInt();
		for (int i = 0; i < count; i++)
		{
			actions.add(new TransportationBlockEntity.Action(
				buf.readBlockPos(),
				buf.readEnum(Direction.class),
				buf.readBoolean()
			));
		}
		return new BlockReplaceActionMessage(blockPos, actions);
	}

	public void handleMainThread(Supplier<NetworkEvent.Context> ctx)
	{
		DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientHandler.handle(this));
	}

	// Client only code
	private static class ClientHandler
	{
		public static void handle(BlockReplaceActionMessage msg)
		{
			Level lvl = Minecraft.getInstance().level;
			if (lvl == null)
				return;

			if (!(lvl.getBlockEntity(msg.blockPos) instanceof TransportationBlockEntity tbe))
				return;

			tbe.setActions(msg.actions);
		}
	}
}
