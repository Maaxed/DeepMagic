package fr.maaxed.gravitationalsorcery.block;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import fr.maaxed.gravitationalsorcery.capability.BaseGravitationHandler;
import fr.maaxed.gravitationalsorcery.capability.CapabilityGravitationHandler;
import fr.maaxed.gravitationalsorcery.capability.ClientTransportationHandler;
import fr.maaxed.gravitationalsorcery.capability.IGravitationHandler;
import fr.maaxed.gravitationalsorcery.capability.SyncGravitationHandler;
import fr.maaxed.gravitationalsorcery.capability.GravitationUtils;
import fr.maaxed.gravitationalsorcery.init.ModBlocks;
import fr.maaxed.gravitationalsorcery.init.ModNetwork;
import fr.maaxed.gravitationalsorcery.network.BlockReplaceActionMessage;
import fr.maaxed.gravitationalsorcery.util.CapabilityProviderHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.network.PacketDistributor;

public class BlackHoleAltarBlockEntity extends BlockEntity
{
	private static final int USE_COUNT = 16;
	private static final int USE_TIME = 5; // 0.25sec
	private static final int SWITCH_TIME = 20; // 1sec
	private static final int MAX_USE_DISTANCE = 32;

	private CompoundTag handlerTag = null;
	private BaseGravitationHandler transportationHandler = null;
	private LazyOptional<IGravitationHandler> lazyCapa = LazyOptional.empty();
	private final List<Action> actions = new ArrayList<>();
	private int currentAction = 0;
	private int actionTimer = 0;

	public BlackHoleAltarBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
	{
		super(type, pos, state);
	}

	@Override
	public void setLevel(Level lvl)
	{
		super.setLevel(lvl);
		if (this.transportationHandler != null)
			return;

		int size = 16;
		this.transportationHandler = lvl.isClientSide ? new ClientTransportationHandler(size) : new SyncGravitationHandler(size, CapabilityProviderHolder.blockEntity(this));
		if (this.handlerTag != null)
		{
			this.transportationHandler.deserializeNBT(this.handlerTag);
		}
		this.lazyCapa = LazyOptional.of(() -> this.transportationHandler);
	}

	public BlackHoleAltarBlockEntity(BlockPos pos, BlockState state)
	{
		this(ModBlocks.BLACK_HOLE_ALTAR_BLOCKENTITY.get(), pos, state);
	}

	@Override
	protected void saveAdditional(CompoundTag tags)
	{
		super.saveAdditional(tags);
		saveCommonData(tags);
		tags.putInt("currentAction", this.currentAction);
		tags.putInt("actionTimer", this.actionTimer);
	}

	private void saveCommonData(CompoundTag tags)
	{
		if (this.transportationHandler != null)
		{
			tags.put("content", this.transportationHandler.serializeNBT());
		}
		else if (this.handlerTag != null)
		{
			tags.put("content", this.handlerTag);
		}
		ListTag actions = new ListTag();
		for (Action action : this.actions)
		{
			actions.add(action.toTag());
		}
		tags.put("actions", actions);
	}

	@Override
	public void load(CompoundTag tags)
	{
		super.load(tags);
		if (this.transportationHandler != null)
		{
			this.handlerTag = null;
			this.transportationHandler.deserializeNBT(tags.getCompound("content"));
		}
		else
		{
			this.handlerTag = tags.getCompound("content");
		}
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
	public CompoundTag getUpdateTag()
	{
		CompoundTag tags = super.getUpdateTag();
		saveCommonData(tags);
		return tags;
	}

	@Override
	public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side)
	{
		if (cap == CapabilityGravitationHandler.GRAVITATION_HANDLER_CAPABILITY)
			return this.lazyCapa.cast();

		return super.getCapability(cap, side);
	}

	public List<Action> getActions()
	{
		return List.copyOf(actions);
	}

	public void setActions(List<Action> actions)
	{
		this.actions.clear();
		this.actions.addAll(actions);
		this.currentAction = 0;
		this.actionTimer = 0;
		this.setChanged();
	}

	public void setTransportationTarget(BlockPos targetPos, Direction targetFace, boolean insert)
	{
		if (targetPos.distSqr(this.getBlockPos()) > MAX_USE_DISTANCE * MAX_USE_DISTANCE)
			return;

		Action action = new Action(targetPos, targetFace, insert);

		if (this.actions.isEmpty())
		{
			this.actions.add(action);
		}
		else if (this.actions.get(0).insert == insert)
		{
			this.actions.set(0, action);
		}
		else if (this.actions.size() >= 2)
		{
			this.actions.set(1, action);
		}
		else
		{
			this.actions.add(insert ? 1 : 0, action);
		}
		this.actionTimer = 0;

		this.setChanged();

		if (!this.getLevel().isClientSide)
			ModNetwork.CHANNEL.send(PacketDistributor.TRACKING_CHUNK.with(() -> this.getLevel().getChunkAt(this.getBlockPos())), new BlockReplaceActionMessage(this));
	}

	protected void tick()
	{
		this.transportationHandler.update();

		if (this.level.isClientSide)
			return;

		updateAction();
		this.setChanged();
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

	public static void tick(Level level, BlockPos pos, BlockState state, BlackHoleAltarBlockEntity blockEntity)
	{
		blockEntity.tick();
	}

	public void renderEffect()
	{
		Vec3 pos1 = Vec3.atCenterOf(this.getBlockPos());
		level.addParticle(ParticleTypes.PORTAL, pos1.x, pos1.y, pos1.z, level.random.nextGaussian() * 0.3, -0.2, level.random.nextGaussian() * 0.3);
		if (this.level.dayTime() % 2 != 0)
			return;

		for (Action action : this.actions)
		{
			if (action.insert)
			{
				Vec3 pos = action.getPos();
				Vec3 targetPos = Vec3.atCenterOf(this.getBlockPos());
				Vec3 delta = targetPos.subtract(pos);
				level.addParticle(ParticleTypes.PORTAL, pos.x, pos.y, pos.z, delta.x, delta.y - 0.5, delta.z);
			}
			else
			{
				Vec3 pos = Vec3.atCenterOf(this.getBlockPos());
				Vec3 targetPos = action.getPos();
				Vec3 delta = targetPos.subtract(pos);
				level.addParticle(ParticleTypes.PORTAL, pos.x, pos.y, pos.z, delta.x, delta.y - 0.5, delta.z);
			}

		}
	}

	@Override
	public AABB getRenderBoundingBox()
	{
		BlockPos pos = this.getBlockPos();
		return new AABB(pos.offset(-1, 0, -1), pos.offset(2, 2, 2));
	}

	public static class Action
	{
		public final BlockPos pos;
		public final Direction face;
		public final boolean insert;
		private LazyOptional<IItemHandler> capa = LazyOptional.empty();

		public Action(BlockPos pos, Direction face, boolean insert)
		{
			this.pos = pos;
			this.face = face;
			this.insert = insert;
		}

		public Vec3 getPos()
		{
			return new Vec3(
				this.pos.getX() + 0.5 + this.face.getStepX() * 0.5,
				this.pos.getY() + 0.5 + this.face.getStepY() * 0.5,
				this.pos.getZ() + 0.5 + this.face.getStepZ() * 0.5);
		}

		public boolean activate(BlackHoleAltarBlockEntity blockentity)
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
                Vec3 pos = this.getPos();

				if (this.insert)
				{
					return GravitationUtils.insert(blockentity.transportationHandler, inventory, pos);
				}
				else
				{
					return GravitationUtils.extract(blockentity.transportationHandler, inventory, pos);
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
