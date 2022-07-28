package fr.max2.deepmagic.client;

import com.mojang.blaze3d.vertex.PoseStack;

import fr.max2.deepmagic.DeepMagicMod;
import fr.max2.deepmagic.block.TransportationBlockEntity;
import fr.max2.deepmagic.init.ModBlocks;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(modid = DeepMagicMod.MOD_ID, bus = Bus.MOD, value = Dist.CLIENT)
public class TransportationBlockEntityRenderer implements BlockEntityRenderer<TransportationBlockEntity>
{
	@SubscribeEvent
	public static void registerRenderer(EntityRenderersEvent.RegisterRenderers event)
	{
		event.registerBlockEntityRenderer(ModBlocks.TRANSPORTATION_BLOCKENTITY.get(), TransportationBlockEntityRenderer::new);
	}

	private final ItemRenderer itemRenderer;

	private TransportationBlockEntityRenderer(BlockEntityRendererProvider.Context context)
	{
		this.itemRenderer = context.getItemRenderer();
	}

	@Override
	public void render(TransportationBlockEntity be, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay)
	{
		poseStack.pushPose();
		BlockPos pos = be.getBlockPos();
		poseStack.translate(-pos.getX(), -pos.getY(), -pos.getZ());
		TransportationRenderer.renderTransportation(poseStack, buffer, this.itemRenderer, be, Vec3.atCenterOf(pos).add(0.0, 0.5, 0.0), partialTicks, (int)be.getLevel().getGameTime(), combinedLight, combinedOverlay, be.getBlockPos().hashCode());
		poseStack.popPose();
	}
}
