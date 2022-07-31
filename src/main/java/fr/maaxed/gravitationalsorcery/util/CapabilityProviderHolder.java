package fr.maaxed.gravitationalsorcery.util;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.PacketDistributor.PacketTarget;

/**
 * A reference to a ICapabilityProvider that can be sent through network
 */
public interface CapabilityProviderHolder
{
	boolean isValid();
	void init(Level lvl);
	void setChanged();
	PacketDistributor.PacketTarget getPacketDistributor();
	ICapabilityProvider getCapabilityProvider();
	void encode(FriendlyByteBuf buf);

	public static CapabilityProviderHolder entity(Entity entity)
	{
		return new EntityHolder(entity);
	}

	public static CapabilityProviderHolder blockEntity(BlockEntity blockEntity)
	{
		return new BlockEntityHolder(blockEntity);
	}

	public static CapabilityProviderHolder decode(FriendlyByteBuf buf)
	{
		switch (buf.readByte())
		{
			case 0:
				return new EntityHolder(buf.readInt());
			case 1:
				return new BlockEntityHolder(buf.readBlockPos());
		}
		return null;
	}

	public static class EntityHolder implements CapabilityProviderHolder
	{
		private final int entityId;
		private Entity entity = null;

		private EntityHolder(int entityId)
		{
			this.entityId = entityId;
		}

		private EntityHolder(Entity entity)
		{
			this(entity.getId());
			this.entity = entity;
		}

		@Override
		public boolean isValid()
		{
			return this.entity != null;
		}

		@Override
		public void init(Level lvl)
		{
			if (this.entity != null)
				return;

			this.entity = lvl.getEntity(this.entityId);
		}

		@Override
		public void setChanged()
		{
			// Nothing to do
		}

		@Override
		public PacketTarget getPacketDistributor()
		{
			return PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> this.entity);
		}

		@Override
		public ICapabilityProvider getCapabilityProvider()
		{
			return this.entity;
		}

		@Override
		public void encode(FriendlyByteBuf buf)
		{
			buf.writeByte(0);
			buf.writeInt(this.entityId);
		}
	}

	public static class BlockEntityHolder implements CapabilityProviderHolder
	{

		private final BlockPos blockPos;
		private BlockEntity blockEntity = null;

		private BlockEntityHolder(BlockPos blockPos)
		{
			this.blockPos = blockPos;
		}

		private BlockEntityHolder(BlockEntity blockEntity)
		{
			this(blockEntity.getBlockPos());
			this.blockEntity = blockEntity;
		}

		@Override
		public boolean isValid()
		{
			return this.blockEntity != null;
		}

		@Override
		public void init(Level lvl)
		{
			if (this.blockEntity != null)
				return;

			this.blockEntity = lvl.getBlockEntity(this.blockPos);
		}

		@Override
		public void setChanged()
		{
			this.blockEntity.setChanged();
		}

		@Override
		public PacketTarget getPacketDistributor()
		{
			return PacketDistributor.TRACKING_CHUNK.with(() -> this.blockEntity.getLevel().getChunkAt(this.blockPos));
		}

		@Override
		public ICapabilityProvider getCapabilityProvider()
		{
			return this.blockEntity;
		}

		@Override
		public void encode(FriendlyByteBuf buf)
		{
			buf.writeByte(1);
			buf.writeBlockPos(this.blockPos);
		}
	}
}
