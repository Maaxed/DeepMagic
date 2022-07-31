package fr.maaxed.gravitationalsorcery.capability;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

public class GravitationUtils
{
	private static final int STACK_SIZE = 8;

	public static boolean extract(IGravitationHandler gravitation, IItemHandler inventory, Vec3 pos)
	{
		if (gravitation.isFull())
			return false;

		for (int slot = 0; slot < inventory.getSlots(); slot++)
		{
			ItemStack extractedStack = inventory.extractItem(slot, STACK_SIZE, false);
			if (extractedStack.isEmpty())
				continue;

			if (gravitation.insertItem(extractedStack, pos))
				return true;
		}

		return false;
	}

	public static boolean insert(IGravitationHandler gravitation, IItemHandler inventory, Vec3 pos)
	{
		ItemStack toInsert = gravitation.extractItem(STACK_SIZE, pos, true);
		if (toInsert.isEmpty())
			return false;

		ItemStack remainingStack = ItemHandlerHelper.insertItem(inventory, toInsert, false);

		if (remainingStack.getCount() == toInsert.getCount())
			return false;

		gravitation.extractItem(toInsert.getCount() - remainingStack.getCount(), pos, false);
		return true;
	}
}
