package fr.max2.deepmagic.capability;

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
		return this.queueSize == 0;
	}

	@Override
	public boolean isFull()
	{
		return this.queueSize == this.itemQueue.length;
	}

	@Override
	public boolean insertItem(@Nullable TransportStack stack)
	{
		if (this.isFull() || stack == null)
			return false;

		this.itemQueue[(this.queueStartIndex + this.queueSize) % this.itemQueue.length] = stack;
		this.queueSize++;
		return true;
	}

	@Override
	public @Nullable TransportStack extractItem(int maxCount, boolean simulate)
	{
		if (this.isEmpty())
			return null;

		TransportStack head = this.itemQueue[this.queueStartIndex];

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
				this.itemQueue[this.queueStartIndex] = null;
				this.queueStartIndex = (this.queueStartIndex + 1) % this.itemQueue.length;
				this.queueSize--;
				return head;
			}
		}
		else
		{
			if (!simulate)
			{
				this.itemQueue[this.queueStartIndex] = head.copyWithSize(stack.getCount() - toExtract);
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
		this.itemQueue = new TransportStack[size];
		this.queueStartIndex = 0;
		ListTag tagList = nbt.getList("Items", Tag.TAG_COMPOUND);
		for (int i = 0; i < tagList.size(); i++)
		{
			CompoundTag itemTags = tagList.getCompound(i);
			this.itemQueue[i] = TransportStack.fromNBT(itemTags);
		}
	}
}
