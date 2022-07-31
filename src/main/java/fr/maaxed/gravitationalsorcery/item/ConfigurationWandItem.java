package fr.maaxed.gravitationalsorcery.item;

import java.util.List;

import fr.maaxed.gravitationalsorcery.block.BlackHoleAltarBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
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
		BlockPos clickPos = context.getClickedPos();
		BlockEntity be = context.getLevel().getBlockEntity(clickPos);
		if (be instanceof BlackHoleAltarBlockEntity tbe)
		{
			setTargetPosition(stack, clickPos);
			return InteractionResult.SUCCESS;
		}
		else
		{
			BlockPos targetPos = getTargetPosition(stack);
			if (targetPos == null)
				return InteractionResult.PASS;

			BlockEntity targetBe = context.getLevel().getBlockEntity(targetPos);
			if (!(targetBe instanceof BlackHoleAltarBlockEntity tbe))
				return InteractionResult.PASS;

			Player player = context.getPlayer();
			boolean insert = player != null && player.isShiftKeyDown();

			tbe.setTransportationTarget(clickPos, context.getClickedFace(), insert);
			return InteractionResult.SUCCESS;
		}
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand)
	{
		// Use in the air
		if (player != null && player.isShiftKeyDown())
		{
			ItemStack stack = player.getItemInHand(hand);
			setTargetPosition(stack, null);
			return InteractionResultHolder.success(stack);
		}
		return super.use(level, player, hand);
	}

	@Override
	public void appendHoverText(ItemStack stack, Level level, List<Component> texts, TooltipFlag flags)
	{
		// TODO Auto-generated method stub
		super.appendHoverText(stack, level, texts, flags);
	}

	@Override
	public void inventoryTick(ItemStack stack, Level level, Entity player, int slotIndex, boolean selected)
	{
		BlockPos targetPos = getTargetPosition(stack);
		if (!level.isClientSide || targetPos == null)
			return;

		BlockEntity targetBe = level.getBlockEntity(targetPos);
		if (!(targetBe instanceof BlackHoleAltarBlockEntity tbe))
			return;

		tbe.renderEffect();
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
		if (pos == null)
		{
			stack.removeTagKey(TARGET_POSITION_NBT);
		}
		else
		{
			stack.addTagElement(TARGET_POSITION_NBT, NbtUtils.writeBlockPos(pos));
		}
	}
}
