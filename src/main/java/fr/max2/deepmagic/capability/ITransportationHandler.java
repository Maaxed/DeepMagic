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
		private static final int TRANSFERT_TIME = 20; // 1sec
		private ItemStack stack;
		private Vec3 originPosition;
		private int ticksAlive;

		public TransportStack(@NotNull ItemStack stack, @NotNull Vec3 originPosition, int ticksAlive)
		{
			this.stack = stack;
			this.originPosition = originPosition;
			this.ticksAlive = ticksAlive;
		}

		public TransportStack(@NotNull ItemStack stack, @NotNull Vec3 originPosition)
		{
			this(stack, originPosition, 0);
		}

		public ItemStack getStack()
		{
			return this.stack;
		}

		public Vec3 getOriginPosition()
		{
			return this.originPosition;
		}

		public float getTransitionFactor(float partialTick)
		{
			float ticks = this.ticksAlive + partialTick;

			float f = ticks / TRANSFERT_TIME;
			if (f > 1.0f)
				f = 1.0f;

			return f;
		}

		public Vec3 getCurrentPosition(Vec3 targetPos, float partialTick)
		{
			float f = getTransitionFactor(partialTick);

			return this.originPosition.lerp(targetPos, f);
		}

		public int getTicksAlive()
		{
			return ticksAlive;
		}

		public void update()
		{
			this.ticksAlive++;
		}

		public TransportStack copy()
		{
			return new TransportStack(this.stack, this.originPosition, this.ticksAlive);
		}

		public TransportStack copyWithSize(int size)
		{
			return new TransportStack(ItemHandlerHelper.copyStackWithSize(this.stack, size), this.originPosition, this.ticksAlive);
		}

		public CompoundTag toNbt()
		{
			CompoundTag tag = new CompoundTag();

			CompoundTag stackTag = new CompoundTag();
			this.stack.save(stackTag);
			tag.put("Stack", stackTag);

			// Position and timeAlive doesn't really need to be saved on disk

			return tag;
		}

		public static TransportStack fromNBT(CompoundTag tag)
		{
			return new TransportStack(ItemStack.of(tag.getCompound("Stack")), new Vec3(0, 0, 0), TRANSFERT_TIME);
		}
	}
}
