package fr.max2.deepmagic.capability;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.items.ItemHandlerHelper;

public class BaseTransportationHandler implements ITransportationHandler, INBTSerializable<CompoundTag>
{
	protected TransportStack[] itemQueue;
	protected int queueStartIndex;
	protected int queueSize;

	public BaseTransportationHandler(int capacity)
	{
		itemQueue = new TransportStack[capacity];
		this.queueStartIndex = 0;
		this.queueSize = 0;
	}

	@Override
	public boolean isEmpty()
	{
		return this.queueSize <= 0;
	}

	@Override
	public boolean isFull()
	{
		return this.queueSize >= this.itemQueue.length;
	}

	@Override
	public boolean insertItem(@NotNull ItemStack stack, @NotNull Vec3 originPosition)
	{
		if (this.isFull() || stack.isEmpty())
			return false;

		TransportStack ts = new TransportStack(stack, originPosition);
		int index = (this.queueStartIndex + this.queueSize) % this.itemQueue.length;
		this.itemQueue[index] = ts;
		this.queueSize++;
		this.onInserted(ts, index);
		return true;
	}

	@Override
	public @NotNull ItemStack extractItem(int maxCount, @NotNull Vec3 targetPosition, boolean simulate)
	{
		if (this.isEmpty())
			return ItemStack.EMPTY;

		int extractIndex = this.queueStartIndex;
		TransportStack head = this.itemQueue[extractIndex];

		if (head == null || head.getStack().isEmpty())
			return ItemStack.EMPTY;
		ItemStack stack = head.getStack();

		int toExtract = Math.min(maxCount, stack.getMaxStackSize());

		if (stack.getCount() <= toExtract)
		{
			if (simulate)
			{
				return head.getStack().copy();
			}
			else
			{
				head.setTarget(targetPosition);
				this.itemQueue[extractIndex] = null;
				this.queueStartIndex = (this.queueStartIndex + 1) % this.itemQueue.length;
				this.queueSize--;
				this.onExtracted(head, extractIndex);
				return head.getStack();
			}
		}
		else
		{
			if (!simulate)
			{
				this.itemQueue[extractIndex] = head.copyWithSize(stack.getCount() - toExtract);
			}
			return ItemHandlerHelper.copyStackWithSize(head.getStack(), toExtract);
		}
	}

	@Override
	public CompoundTag serializeNBT()
	{
        ListTag items = new ListTag();
        for (int i = 0; i < this.queueSize; i++)
        {
			int index = (this.queueStartIndex + i) % this.itemQueue.length;
            if (this.itemQueue[index] != null)
            {
                CompoundTag itemTag = this.itemQueue[index].toNbt();
                items.add(itemTag);
            }
        }

		CompoundTag tag = new CompoundTag();
		tag.put("Items", items);
		tag.putInt("Size", this.itemQueue.length);
		return tag;
	}

	@Override
	public void deserializeNBT(CompoundTag nbt)
	{
		int size = nbt.contains("Size", Tag.TAG_INT) ? nbt.getInt("Size") : this.itemQueue.length;
		if (size == this.itemQueue.length)
		{
			this.clear();
		}
		else
		{
			this.itemQueue = new TransportStack[size];
		}
		this.queueStartIndex = 0;
		ListTag tagList = nbt.getList("Items", Tag.TAG_COMPOUND);
		for (int i = 0; i < tagList.size(); i++)
		{
			CompoundTag itemTags = tagList.getCompound(i);
			this.itemQueue[i] = TransportStack.fromNBT(itemTags);
		}
		this.queueSize = tagList.size();
	}

	protected void onInserted(TransportStack stack, int index)
	{ }

	protected void onExtracted(TransportStack stack, int index)
	{ }

	// Internal methods

	private void clear()
	{
		Arrays.fill(this.itemQueue, null);
	}

	public List<TransportStack> getContent()
	{
		return IntStream.range(0, this.queueSize).mapToObj(i ->
		{
			int index = (this.queueStartIndex + i) % this.itemQueue.length;
			TransportStack stack = this.itemQueue[index];
			if (stack == null)
				return null;
			else
				return stack.copy();
		}).toList();
	}

	public void update()
	{
		for (int i = 0; i < this.queueSize; i++)
		{
			int index = (this.queueStartIndex + i) % this.itemQueue.length;
			if (this.itemQueue[index] == null)
				continue;

			this.itemQueue[index].update();
		}
	}

	public NonNullList<ItemStack> getDrops()
	{
		NonNullList<ItemStack> drops = NonNullList.create();

		for (int i = 0; i < this.queueSize; i++)
		{
			int index = (this.queueStartIndex + i) % this.itemQueue.length;
			TransportStack item = this.itemQueue[index];
			if (item == null || item.getStack().isEmpty())
				continue;

			drops.add(item.getStack());
		}

		return drops;
	}

	public boolean insertItemAt(int index, @Nullable TransportStack stack)
	{
		if (index < 0 || index >= this.itemQueue.length)
			return false;

		int expectedSize = (index - this.queueStartIndex + this.itemQueue.length) % this.itemQueue.length + 1;

		if (this.queueSize < expectedSize)
			this.queueSize = expectedSize;

		this.itemQueue[index] = stack;
		this.onInserted(stack, index);
		return true;
	}

	public boolean extractItemAt(int index, Vec3 targetPosition)
	{
		if (index < 0 || index >= this.itemQueue.length)
			return false;

		if (!this.isFull())
		{
			int endIndex = (this.queueStartIndex + this.queueSize) % this.itemQueue.length;
			if (endIndex < this.queueStartIndex)
			{
				if (index >= endIndex && index < this.queueStartIndex)
					return false;
			}
			else
			{
				if (index >= endIndex || index < this.queueStartIndex)
					return false;
			}
		}

		int oldStart = this.queueStartIndex;
		int newStart = (index + 1) % this.itemQueue.length;
		int removedCount = (newStart - this.queueStartIndex + this.itemQueue.length) % this.itemQueue.length;
		this.queueSize -= removedCount;
		this.queueStartIndex = newStart;

		for (int i = 0; i < removedCount; i++)
		{
			int removeIndex = (oldStart + i) % this.itemQueue.length;

			TransportStack stack = this.itemQueue[removeIndex];
			if (stack == null || stack.getStack().isEmpty())
				continue;

			stack.setTarget(targetPosition);
			this.itemQueue[removeIndex] = null;
			this.onExtracted(stack, removeIndex);
		}

		return true;
	}

	public boolean replaceContent(List<TransportStack> newContent)
	{
		if (newContent.size() > this.itemQueue.length)
			return false;

		this.queueStartIndex = 0;
		this.queueSize = newContent.size();
		this.clear();

		for (int i = 0; i < newContent.size(); i++)
		{
			this.itemQueue[i] = newContent.get(i);
		}

		return true;
	}

	public static class TransportStack
	{

		private static final int TRANSFERT_TIME = 20; // 1sec
		private ItemStack stack;
		private Vec3 originPosition;
		private @Nullable Vec3 targetPosition;
		private int ticksAlive;
		private int targetTicks;

		public TransportStack(@NotNull ItemStack stack, @NotNull Vec3 originPosition, int ticksAlive)
		{
			this.stack = stack;
			this.originPosition = originPosition;
			this.ticksAlive = ticksAlive;
			this.targetPosition = null;
			this.targetTicks = 0;
		}

		public TransportStack(@NotNull ItemStack stack, @NotNull Vec3 originPosition)
		{
			this(stack, originPosition, 0);
		}

		public void setTarget(@NotNull Vec3 targetPosition)
		{
			this.targetPosition = targetPosition;
		}

		public ItemStack getStack()
		{
			return this.stack;
		}

		public Vec3 getOriginPosition()
		{
			return this.originPosition;
		}

		@Nullable
		public Vec3 getTargetPosition()
		{
			return this.targetPosition;
		}

		public float getTransitionFactor(float partialTick)
		{
			float f1 = (this.ticksAlive + partialTick) / TRANSFERT_TIME;
			if (f1 > 1.0f)
				f1 = 1.0f;

			if (this.targetPosition == null)
				return f1;

			float f2 = (this.targetTicks + partialTick) / TRANSFERT_TIME;
			if (f2 > 1.0f)
				f2 = 1.0f;

			return Math.min(f1, 1.0f - f2);
		}

		public Vec3 getCurrentPosition(Vec3 targetPos, float partialTick)
		{
			float f1 = (this.ticksAlive + partialTick) / TRANSFERT_TIME;
			if (f1 > 1.0f)
				f1 = 1.0f;

			Vec3 pos = this.originPosition.lerp(targetPos, f1);

			if (this.targetPosition == null)
				return pos;

			float f2 = (this.targetTicks + partialTick) / TRANSFERT_TIME;
			if (f2 > 1.0f)
				f2 = 1.0f;

			return pos.lerp(this.targetPosition, f2);
		}

		public int getTicksAlive()
		{
			return ticksAlive;
		}

		public void update()
		{
			this.ticksAlive++;
			if (this.targetPosition != null)
				this.targetTicks++;
		}

		public boolean shouldGetRomoved()
		{
			return this.targetTicks >= TRANSFERT_TIME;
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
