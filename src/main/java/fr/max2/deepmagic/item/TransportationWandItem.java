package fr.max2.deepmagic.item;

import fr.max2.deepmagic.capability.CapabilityTransportationHandler;
import fr.max2.deepmagic.capability.TransportationUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.items.CapabilityItemHandler;

// On right click : extract items from the target container, put the items in a levitation state around the player
// On shift + right click : insert levitating items in the target container
public class TransportationWandItem extends Item
{
    private static final int USE_DURATION = 60 * 60 * 20; // 1h
    private static final int USE_TIME = 5; // 0.25sec


    public TransportationWandItem(Properties properteies)
    {
        super(properteies.stacksTo(1));
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context)
    {
        if (context.getPlayer() == null)
            return InteractionResult.FAIL;

        context.getPlayer().startUsingItem(context.getHand());

        setTargetPosition(stack, context.getClickedPos());
        setTargetFace(stack, context.getClickedFace());

        return InteractionResult.CONSUME;
    }

    @Override
    public void onUsingTick(ItemStack stack, LivingEntity player, int count)
    {
        super.onUsingTick(stack, player, count);

        if (player.level.isClientSide)
            return;

        if ((USE_DURATION - count + 1) % USE_TIME != 0)
            return;

        BlockPos targetPos = getTargetPosition(stack);
        Direction targetFace = getTargetFace(stack);
        if (targetPos == null || targetFace == null)
            return;

        BlockEntity be = player.level.getBlockEntity(targetPos);
        if (be == null)
            return;

        be.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent( inventory ->
        {
            player.getCapability(CapabilityTransportationHandler.TRANSPORTATION_HANDLER_CAPABILITY).ifPresent(transportation ->
            {
                Vec3 pos = new Vec3(
                        targetPos.getX() + 0.5 + targetFace.getStepX() * 0.5,
                        targetPos.getY() + 0.5 + targetFace.getStepY() * 0.5,
                        targetPos.getZ() + 0.5 + targetFace.getStepZ() * 0.5);
                if (player.isShiftKeyDown())
                {
                    TransportationUtils.insert(transportation, inventory, pos);
                }
                else
                {
                    TransportationUtils.extract(transportation, inventory, pos);
                }
            });
        });
    }

    @Override
    public int getUseDuration(ItemStack p_41454_)
    {
        return USE_DURATION;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack p_41452_)
    {
        return UseAnim.BOW;
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


    private static final String TARGET_FACE_NBT = "TargetFace";

    private static Direction getTargetFace(ItemStack stack)
    {
        if (!stack.hasTag())
            return null;

        CompoundTag tag = stack.getTag();
        if (!tag.contains(TARGET_FACE_NBT, Tag.TAG_INT))
            return null;

        return Direction.from3DDataValue(tag.getInt(TARGET_FACE_NBT));
    }

    private static void setTargetFace(ItemStack stack, Direction face)
    {
        stack.getOrCreateTag().putInt(TARGET_FACE_NBT, face.get3DDataValue());
    }
}
