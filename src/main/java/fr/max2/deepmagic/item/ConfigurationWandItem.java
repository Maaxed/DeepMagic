package fr.max2.deepmagic.item;

import java.util.List;

import fr.max2.deepmagic.block.TransportationBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

// Right click a transportation block to select it
// Right click / shift + right click on a container to configure the transportation block to extract / insert from this container
public class ConfigurationWandItem extends Item
{
	public ConfigurationWandItem(Properties properties)
	{
		super(properties.stacksTo(1));
	}

	@Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context)
    {
		BlockEntity be = context.getLevel().getBlockEntity(context.getClickedPos());
		if (be instanceof TransportationBlockEntity tbe)
		{
			setTargetPosition(stack, context.getClickedPos());
			return InteractionResult.SUCCESS;
		}
		else
		{
			BlockPos targetPos = getTargetPosition(stack);
			if (targetPos == null)
				return InteractionResult.PASS;

			BlockEntity targetBe = context.getLevel().getBlockEntity(targetPos);
			if (!(targetBe instanceof TransportationBlockEntity tbe))
				return InteractionResult.PASS;

			Player player = context.getPlayer();
			boolean insert = player != null && player.isShiftKeyDown();

			tbe.setTransportationTarget(context.getClickedPos(), context.getClickedFace(), insert);
			return InteractionResult.SUCCESS;
		}
	}

	@Override
	public void appendHoverText(ItemStack stack, Level level, List<Component> texts, TooltipFlag flags)
	{
		// TODO Auto-generated method stub
		super.appendHoverText(stack, level, texts, flags);
	}

	@Override
	public boolean isFoil(ItemStack stack)
	{
		return stack.getTagElement(TARGET_POSITION_NBT) != null;
	}

	private static final String TARGET_POSITION_NBT = "TargetPos";

	private static BlockPos getTargetPosition(ItemStack stack)
	{
		CompoundTag targetPosTag = stack.getTagElement(TARGET_POSITION_NBT);
		return targetPosTag == null ? null : NbtUtils.readBlockPos(targetPosTag);
	}

	private static void setTargetPosition(ItemStack stack, BlockPos pos)
	{
		stack.getOrCreateTag().put(TARGET_POSITION_NBT, NbtUtils.writeBlockPos(pos));
	}
}
