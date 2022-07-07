package fr.max2.deepmagic.capability;

import org.jetbrains.annotations.NotNull;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.items.ItemHandlerHelper;

public class BaseTransportationHandler implements ITransportationHandler, INBTSerializable<CompoundTag>
{
	private NonNullList<ItemStack> itemQueue;
	private int queueStartIndex;
	private int queueSize;

	public BaseTransportationHandler(int capacity)
	{
		itemQueue = NonNullList.withSize(capacity, ItemStack.EMPTY);
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
		return this.queueSize == this.itemQueue.size();
	}

	@Override
	public boolean insertItem(@NotNull ItemStack stack)
	{
		if (this.isFull() || stack.isEmpty())
			return false;
		
		this.itemQueue.set((this.queueStartIndex + this.queueSize) % this.itemQueue.size(), stack);
		this.queueSize++;
		return true;
	}

	@Override
	public @NotNull ItemStack extractItem(int maxCount, boolean simulate)
	{
		if (this.isEmpty())
			return ItemStack.EMPTY;
		
		ItemStack head = this.itemQueue.get(this.queueStartIndex);

		if (head.isEmpty())
			return ItemStack.EMPTY;
		
		int toExtract = Math.min(maxCount, head.getMaxStackSize());

		if (head.getCount() <= toExtract)
		{
			if (simulate)
			{
				return head.copy();
			}
			else
			{
				this.itemQueue.set(this.queueStartIndex, ItemStack.EMPTY);
				this.queueStartIndex = (this.queueStartIndex + 1) % this.itemQueue.size();
				this.queueSize--;
				return head;
			}
		}
		else
		{
			if (!simulate)
			{
				this.itemQueue.set(this.queueStartIndex, ItemHandlerHelper.copyStackWithSize(head, head.getCount() - toExtract));
			}
			return ItemHandlerHelper.copyStackWithSize(head, toExtract);
		}
	}

	@Override
	public CompoundTag serializeNBT()
	{
        ListTag items = new ListTag();
        for (int i = 0; i < this.queueSize; i++)
        {
			int index = (this.queueStartIndex + i) % this.itemQueue.size();
            if (!this.itemQueue.get(index).isEmpty())
            {
                CompoundTag itemTag = new CompoundTag();
                this.itemQueue.get(index).save(itemTag);
                items.add(itemTag);
            }
        }
		
		CompoundTag tag = new CompoundTag();
		tag.put("Items", items);
		tag.putInt("Size", this.itemQueue.size());
		return tag;
	}

	@Override
	public void deserializeNBT(CompoundTag nbt)
	{
		int size = nbt.contains("Size", Tag.TAG_INT) ? nbt.getInt("Size") : this.itemQueue.size();
		this.itemQueue = NonNullList.withSize(size, ItemStack.EMPTY);
		this.queueStartIndex = 0;
		ListTag tagList = nbt.getList("Items", Tag.TAG_COMPOUND);
		for (int i = 0; i < tagList.size(); i++)
		{
			CompoundTag itemTags = tagList.getCompound(i);
			this.itemQueue.set(i, ItemStack.of(itemTags));
		}
	}
}
