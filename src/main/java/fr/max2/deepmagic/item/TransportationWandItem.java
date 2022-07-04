package fr.max2.deepmagic.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

// On right click : extract items from the target container, put the items in a levitation state around the player
// On shift + right click : insert levitating items in the target container
public class TransportationWandItem extends Item
{
    public TransportationWandItem(Properties properteies)
    {
        super(properteies);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context)
    {
        if (context.getPlayer() == null)
            return InteractionResult.FAIL;
        context.getPlayer().startUsingItem(context.getHand());
        return InteractionResult.CONSUME;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand)
    {
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }

    @Override
    public void onUsingTick(ItemStack stack, LivingEntity player, int count)
    {
        super.onUsingTick(stack, player, count);
    }

    @Override
    public void onUseTick(Level p_41428_, LivingEntity p_41429_, ItemStack p_41430_, int p_41431_)
    {
        // TODO Auto-generated method stub
        super.onUseTick(p_41428_, p_41429_, p_41430_, p_41431_);
    }

    @Override
    public int getUseDuration(ItemStack p_41454_)
    {
        return 60*60*20; // 1h
    }

    @Override
    public UseAnim getUseAnimation(ItemStack p_41452_) {
        return UseAnim.BOW;
    }
}
