package fr.max2.deepmagic.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;

import fr.max2.deepmagic.DeepMagicMod;
import fr.max2.deepmagic.capability.CapabilityTransportationHandler;
import fr.max2.deepmagic.capability.ClientTransportationHandler;
import fr.max2.deepmagic.capability.BaseTransportationHandler.TransportStack;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(modid = DeepMagicMod.MOD_ID, bus = Bus.FORGE, value = Dist.CLIENT)
public class TransportationRenderer
{
	@SubscribeEvent
	public static void renderFirstPerson(RenderLevelStageEvent event)
	{
		if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES)
			return;

		CameraType cameratype = Minecraft.getInstance().options.getCameraType();
		if (!cameratype.isFirstPerson())
			return;

		Entity player = event.getCamera().getEntity();
		PoseStack poseStack = event.getPoseStack();
		float partialTick = event.getPartialTick();
		MultiBufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();

		poseStack.pushPose();
		Vec3 camPos = event.getCamera().getPosition();
		poseStack.translate(-camPos.x, -camPos.y, -camPos.z);
		renderTransportation(poseStack, buffer, player, partialTick);
		poseStack.popPose();
	}

	@SubscribeEvent
	public static void renderThirdPerson(RenderPlayerEvent.Post event)
	{
		Player player = event.getEntity();
		PoseStack poseStack = event.getPoseStack();
		float partialTick = event.getPartialTick();
		MultiBufferSource buffer = event.getMultiBufferSource();

		poseStack.pushPose();
		Vec3 playerPos = player.getPosition(partialTick);
		poseStack.translate(-playerPos.x, -playerPos.y, -playerPos.z);
		renderTransportation(poseStack, buffer, player, partialTick);
		poseStack.popPose();
	}

	private static void renderTransportation(PoseStack poseStack, MultiBufferSource buffer, Entity player, float partialTick)
	{
		player.getCapability(CapabilityTransportationHandler.TRANSPORTATION_HANDLER_CAPABILITY).ifPresent(transportation ->
		{
			if (!(transportation instanceof ClientTransportationHandler bth))
				return;

			EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
			ItemRenderer renderer = Minecraft.getInstance().getItemRenderer();
			float fullTick = player.tickCount + partialTick;
			int stackCount = bth.getSize();

			bth.getIndexStacks((stack, i) ->
			{
				renderStack(renderer, poseStack, buffer, fullTick, Mth.TWO_PI * i / stackCount, stack, player.getPosition(partialTick).add(0, 0.5, 0), partialTick, dispatcher.getPackedLightCoords(player, partialTick), player.getId() + i);
			});
		});
	}

	private static void renderStack(ItemRenderer renderer, PoseStack poseStack, MultiBufferSource buffer, float fullTick, float rotAngle, TransportStack stack, Vec3 targetPos, float partialTick, int packedLight, int randSeed)
	{
		if (stack == null)
			return;

		poseStack.pushPose();

		Vec3 pos = stack.getCurrentPosition(targetPos, partialTick);
		poseStack.translate(pos.x, pos.y, pos.z);

		poseStack.mulPose(Quaternion.fromXYZ(0.0f, fullTick * 0.2f - rotAngle, 0.0f));

		float f = stack.getTransitionFactor(partialTick);
		poseStack.translate(0f, Mth.sin(fullTick * 0.4f - rotAngle) * 0.2 * f, 1f * f);

		renderer.renderStatic(stack.getStack(), TransformType.GROUND, packedLight, OverlayTexture.NO_OVERLAY, poseStack, buffer, randSeed);

		poseStack.popPose();
	}
}
