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
	/** Content of the circular queue **/
	protected TransportStack[] itemQueue;
	protected int queueStartIndex;
	/** Number of elements currently in the queue **/
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
				// Remove element from the queue
				this.itemQueue[extractIndex] = null;
				this.queueStartIndex = (this.queueStartIndex + 1) % this.itemQueue.length;
				this.queueSize--;

				head.setTarget(targetPosition);
				this.onExtracted(head, extractIndex);
				return head.getStack();
			}
		}
		else
		{
			ItemStack extractedStack = ItemHandlerHelper.copyStackWithSize(head.getStack(), toExtract);
			if (!simulate)
			{
				// Remove part of the stack
				this.itemQueue[extractIndex] = head.copyWithSize(stack.getCount() - toExtract);

				TransportStack extracted = new TransportStack(extractedStack, head.getOriginPosition(), head.getTicksAlive());
				extracted.setTarget(targetPosition);
				this.onExtracted(extracted, extractIndex);
			}
			return extractedStack;
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
				return f1 * (2.0f - f1);

			float f2 = (this.targetTicks + partialTick) / TRANSFERT_TIME;
			if (f2 > 1.0f)
				f2 = 1.0f;

			return Math.min(f1, 1.0f - (f2 * f2));
		}

		public Vec3 getCurrentPosition(Vec3 targetPos, float partialTick)
		{
			float f1 = (this.ticksAlive + partialTick) / TRANSFERT_TIME;
			if (f1 > 1.0f)
				f1 = 1.0f;

			Vec3 pos = this.originPosition.lerp(targetPos, f1 * (2.0f - f1));

			if (this.targetPosition == null)
				return pos;

			float f2 = (this.targetTicks + partialTick) / TRANSFERT_TIME;
			if (f2 > 1.0f)
				f2 = 1.0f;

			return pos.lerp(this.targetPosition, f2*f2);
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
