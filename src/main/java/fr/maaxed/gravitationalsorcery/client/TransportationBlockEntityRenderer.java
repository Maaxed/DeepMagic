package fr.maaxed.gravitationalsorcery.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;

import fr.maaxed.gravitationalsorcery.GravitationalSorceryMod;
import fr.maaxed.gravitationalsorcery.block.TransportationBlockEntity;
import fr.maaxed.gravitationalsorcery.init.ModBlocks;
import fr.maaxed.gravitationalsorcery.init.ModItems;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(modid = GravitationalSorceryMod.MOD_ID, bus = Bus.MOD, value = Dist.CLIENT)
public class TransportationBlockEntityRenderer implements BlockEntityRenderer<TransportationBlockEntity>
{
	private final ItemStack blackHoleItem = new ItemStack(ModItems.DEEP_DARK_PEARL.get());

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
		int randomSeed = be.getBlockPos().hashCode();
		long tickCount = be.getLevel().getGameTime();
		double fullTick = tickCount + partialTicks;

		// Render black hole
		poseStack.pushPose();
		poseStack.translate(0.5, 1.25, 0.5);
		poseStack.translate(0.0, Math.sin(fullTick * 0.04 + randomSeed) * 0.0625, 0.0);
		poseStack.scale(2.0f, 2.0f, 2.0f);

		Camera cam = Minecraft.getInstance().gameRenderer.getMainCamera();
		if (cam != null)
		{
			// Always face towards the player
			Vec3 renderPos = Vec3.atLowerCornerOf(be.getBlockPos()).add(0.5, 1.25, 0.5);
			Vec3 lookDir = renderPos.subtract(cam.getPosition());
			double yaw = Mth.atan2(lookDir.x, lookDir.z);
			poseStack.mulPose(Vector3f.YP.rotation((float)yaw));
			double forward = lookDir.horizontalDistance();
			double pitch = Mth.atan2(-lookDir.y, forward);
			poseStack.mulPose(Vector3f.XP.rotation((float)pitch));
		}

		poseStack.mulPose(Vector3f.ZP.rotationDegrees(45.0f));
		poseStack.translate(0.0, -0.125, 0);
		this.itemRenderer.renderStatic(blackHoleItem, TransformType.GROUND, combinedLight, combinedOverlay, poseStack, buffer, randomSeed);
		poseStack.popPose();

		// Render content
		poseStack.pushPose();
		BlockPos pos = be.getBlockPos();
		poseStack.translate(-pos.getX(), -pos.getY(), -pos.getZ());
		TransportationRenderer.renderTransportation(poseStack, buffer, this.itemRenderer, be, Vec3.atCenterOf(pos).add(0.0, 0.5, 0.0), partialTicks, tickCount, combinedLight, combinedOverlay, randomSeed + 1);
		poseStack.popPose();
	}
}
