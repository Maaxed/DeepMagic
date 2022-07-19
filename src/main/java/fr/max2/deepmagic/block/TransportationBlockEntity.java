package fr.max2.deepmagic.block;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import fr.max2.deepmagic.capability.BaseTransportationHandler;
import fr.max2.deepmagic.capability.CapabilityTransportationHandler;
import fr.max2.deepmagic.capability.ITransportationHandler;
import fr.max2.deepmagic.capability.TransportationUtils;
import fr.max2.deepmagic.init.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class TransportationBlockEntity extends BlockEntity
{
	private static final int USE_COUNT = 16;
	private static final int USE_TIME = 5; // 0.25sec
	private static final int SWITCH_TIME = 20; // 1sec

	private final BaseTransportationHandler transportationHandler;
	private final LazyOptional<ITransportationHandler> lazyCapa;
	private final List<Action> actions = new ArrayList<>();
	private int currentAction = 0;
	private int actionTimer = 0;

	public TransportationBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
	{
		super(type, pos, state);
		int size = 16;
		this.transportationHandler = new BaseTransportationHandler(size);
		this.lazyCapa = LazyOptional.of(() -> this.transportationHandler);

		this.actions.add(new Action(pos.north(2), Direction.SOUTH, false));
		this.actions.add(new Action(pos.south(2), Direction.NORTH, true));
	}

	public TransportationBlockEntity(BlockPos pos, BlockState state)
	{
		this(ModBlocks.TRANSPORTATION_BLOCKENTITY.get(), pos, state);
	}

	@Override
	protected void saveAdditional(CompoundTag tags)
	{
		super.saveAdditional(tags);
		tags.put("content", this.transportationHandler.serializeNBT());
		ListTag actions = new ListTag();
		for (Action action : this.actions)
		{
			actions.add(action.toTag());
		}
		tags.put("actions", actions);
		tags.putInt("currentAction", this.currentAction);
		tags.putInt("actionTimer", this.actionTimer);
	}

	@Override
	public void load(CompoundTag tags)
	{
		super.load(tags);
		this.transportationHandler.deserializeNBT(tags.getCompound("content"));
		ListTag actions = tags.getList("actions", Tag.TAG_COMPOUND);
		this.actions.clear();
		for (int i = 0; i < actions.size(); i++)
		{
			this.actions.add(Action.fromTag(actions.getCompound(i)));
		}
		this.currentAction = tags.getInt("currentAction");
		this.actionTimer = tags.getInt("actionTimer");
	}

	@Override
	public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side)
	{
		if (cap == CapabilityTransportationHandler.TRANSPORTATION_HANDLER_CAPABILITY)
			return this.lazyCapa.cast();

		return super.getCapability(cap, side);
	}

	protected void tick()
	{
		this.transportationHandler.update();

		updateAction();
	}

	protected void updateAction()
	{
		this.actionTimer++;
		if (this.actionTimer < SWITCH_TIME)
			return;

		int useTime = (this.actionTimer - SWITCH_TIME) % USE_TIME;
		if (useTime != 0)
			return;

		if (this.currentAction < 0 || this.currentAction >= this.actions.size())
		{
			this.actionTimer = 0;
			this.currentAction = 0;
			return;
		}

		int useCount = (this.actionTimer - SWITCH_TIME) / USE_TIME;

		boolean shouldContinue = this.actions.get(this.currentAction).activate(this);

		if (!shouldContinue || useCount >= USE_COUNT)
		{
			this.actionTimer = 0;
			this.currentAction++;
			if (this.currentAction > this.actions.size())
				this.currentAction = 0;
		}
	}

	public static void tick(Level level, BlockPos pos, BlockState state, TransportationBlockEntity blockEntity)
	{
		blockEntity.tick();
	}

	public static class Action
	{
		private final BlockPos pos;
		private final Direction face;
		private final boolean insert;
		private LazyOptional<IItemHandler> capa = LazyOptional.empty();

		public Action(BlockPos pos, Direction face, boolean insert)
		{
			this.pos = pos;
			this.face = face;
			this.insert = insert;
		}

		public boolean activate(TransportationBlockEntity blockentity)
		{
			if (!capa.isPresent())
			{
				BlockEntity be = blockentity.level.getBlockEntity(this.pos);
				if (be == null)
					return false;

				capa = be.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
			}

			return capa.map( inventory ->
			{
                Vec3 pos = new Vec3(
                        this.pos.getX() + 0.5 + this.face.getStepX() * 0.5,
                        this.pos.getY() + 0.5 + this.face.getStepY() * 0.5,
                        this.pos.getZ() + 0.5 + this.face.getStepZ() * 0.5);

				if (this.insert)
				{
					return TransportationUtils.insert(blockentity.transportationHandler, inventory, pos);
				}
				else
				{
					return TransportationUtils.extract(blockentity.transportationHandler, inventory, pos);
				}
			}).orElse(false);
		}

		public CompoundTag toTag()
		{
			CompoundTag tag = new CompoundTag();
			tag.put("pos", NbtUtils.writeBlockPos(this.pos));
			tag.putInt("face", this.face.get3DDataValue());
			tag.putBoolean("insert", this.insert);
			return tag;
		}

		public static Action fromTag(CompoundTag tag)
		{
			BlockPos pos = NbtUtils.readBlockPos(tag.getCompound("pos"));
			Direction face = Direction.from3DDataValue(tag.getInt("face"));
			boolean insert = tag.getBoolean("insert");
			return new Action(pos, face, insert);
		}
	}
}
