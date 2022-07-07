package fr.max2.deepmagic.capability;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.items.ItemHandlerHelper;

public interface ITransportationHandler
{
	boolean isEmpty();
	boolean isFull();

	boolean insertItem(@Nullable TransportStack stack);
	@Nullable TransportStack extractItem(int maxCount, boolean simulate);

	public static class TransportStack
	{
		private ItemStack stack;
		private Vec3 position;

		public TransportStack(@NotNull ItemStack stack, @NotNull Vec3 position)
		{
			this.stack = stack;
			this.position = position;
		}

		public ItemStack getStack()
		{
			return stack;
		}

		public Vec3 getPosition()
		{
			return position;
		}

		public void updatePosition(Vec3 targetPos)
		{
			position.lerp(targetPos, 0.1);
		}

		public TransportStack copy()
		{
			return new TransportStack(this.stack, this.position);
		}

		public TransportStack copyWithSize(int size)
		{
			return new TransportStack(ItemHandlerHelper.copyStackWithSize(this.stack, size), this.position);
		}

		public CompoundTag toNbt()
		{
			CompoundTag tag = new CompoundTag();

			CompoundTag stackTag = new CompoundTag();
			this.stack.save(stackTag);
			tag.put("Stack", stackTag);
			tag.putDouble("PosX", this.position.x);
			tag.putDouble("PosY", this.position.y);
			tag.putDouble("PosZ", this.position.z);

			return tag;
		}

		public static TransportStack fromNBT(CompoundTag tag)
		{
			return new TransportStack(
				ItemStack.of(tag),
				new Vec3(
					tag.getDouble("PosX"),
					tag.getDouble("PosY"),
					tag.getDouble("PosZ")));
		}
	}
}
