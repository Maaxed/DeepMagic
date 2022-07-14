package fr.max2.deepmagic.capability;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.INBTSerializable;

public class BaseTransportationHandler implements ITransportationHandler, INBTSerializable<CompoundTag>
{
	private TransportStack[] itemQueue;
	private int queueStartIndex;
	private int queueSize;

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
		return this.queueSize >= this.itemQueue.length - 1;
	}

	@Override
	public boolean insertItem(@Nullable TransportStack stack)
	{ // TODO sync with client
		if (this.isFull() || stack == null)
			return false;

		int index = (this.queueStartIndex + this.queueSize) % this.itemQueue.length;
		this.itemQueue[index] = stack;
		this.queueSize++;
		this.onInserted(stack, index);
		return true;
	}

	@Override
	public @Nullable TransportStack extractItem(int maxCount, boolean simulate)
	{ // TODO sync with client
		if (this.isEmpty())
			return null;

		int extractIndex = this.queueStartIndex;
		TransportStack head = this.itemQueue[extractIndex];

		if (head == null)
			return null;
		ItemStack stack = head.getStack();

		int toExtract = Math.min(maxCount, stack.getMaxStackSize());

		if (stack.getCount() <= toExtract)
		{
			if (simulate)
			{
				return head.copy();
			}
			else
			{
				this.itemQueue[extractIndex] = null;
				this.queueStartIndex = (this.queueStartIndex + 1) % this.itemQueue.length;
				this.queueSize--;
				this.onExtracted(head, extractIndex);
				return head;
			}
		}
		else
		{
			if (!simulate)
			{
				this.itemQueue[extractIndex] = head.copyWithSize(stack.getCount() - toExtract);
			}
			return head.copyWithSize(toExtract);
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

	public int getSize()
	{
		return this.itemQueue.length;
	}

	@Nullable
	public TransportStack getStack(int index)
	{
		return this.itemQueue[index];
	}

	public boolean insertItemAt(int index, @Nullable TransportStack stack)
	{
		if (index < 0 || index >= this.itemQueue.length)
			return false;

		int expectedSize = (index - this.queueStartIndex + this.itemQueue.length) % this.itemQueue.length + 1;

		if (this.queueSize < expectedSize)
			this.queueSize = expectedSize;

		this.itemQueue[index] = stack;
		return true;
	}

	public boolean extractItemAt(int index)
	{
		if (index < 0 || index >= this.itemQueue.length)
			return false;

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

		int newStart = (index + 1) % this.itemQueue.length;
		this.itemQueue[index] = null;
		this.queueSize -= (newStart - this.queueStartIndex + this.itemQueue.length) % this.itemQueue.length;
		this.queueStartIndex = newStart;
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
}
