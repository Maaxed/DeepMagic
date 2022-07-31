package fr.maaxed.gravitationalsorcery.capability;

import org.jetbrains.annotations.NotNull;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public interface ITransportationHandler
{
	boolean isEmpty();
	boolean isFull();

	boolean insertItem(@NotNull ItemStack stack, @NotNull Vec3 originPosition);
	@NotNull ItemStack extractItem(int maxCount, @NotNull Vec3 targetPosition, boolean simulate);
}
