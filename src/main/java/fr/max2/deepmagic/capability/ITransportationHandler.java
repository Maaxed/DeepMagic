package fr.max2.deepmagic.capability;

import org.jetbrains.annotations.NotNull;

import net.minecraft.world.item.ItemStack;

public interface ITransportationHandler
{
	boolean isEmpty();
	boolean isFull();
	
	boolean insertItem(@NotNull ItemStack stack);
	@NotNull ItemStack extractItem(int maxCount, boolean simulate);
}
