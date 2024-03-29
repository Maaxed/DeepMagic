package fr.maaxed.gravitationalsorcery.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;

import fr.maaxed.gravitationalsorcery.GravitationalSorceryMod;
import fr.maaxed.gravitationalsorcery.capability.CapabilityGravitationHandler;
import fr.maaxed.gravitationalsorcery.capability.ClientGravitationHandler;
import fr.maaxed.gravitationalsorcery.capability.BaseGravitationHandler.TransportStack;
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
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(modid = GravitationalSorceryMod.MOD_ID, bus = Bus.FORGE, value = Dist.CLIENT)
public class GravitationRenderer
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
		ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
		EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
		renderTransportation(poseStack, buffer, itemRenderer, player, player.getPosition(partialTick).add(0, 0.5, 0), partialTick, player.tickCount, dispatcher.getPackedLightCoords(player, partialTick), OverlayTexture.NO_OVERLAY, player.getId());
	}

	public static void renderTransportation(PoseStack poseStack, MultiBufferSource buffer, ItemRenderer itemRenderer, ICapabilityProvider capabilityProvider, Vec3 pos, float partialTick, long tickCount, int packedLight, int packedOverlay, int randSeed)
	{
		capabilityProvider.getCapability(CapabilityGravitationHandler.GRAVITATION_HANDLER_CAPABILITY).ifPresent(gravitation ->
		{
			if (!(gravitation instanceof ClientGravitationHandler baseGravitation)) return;

			double fullTick = (double)tickCount + partialTick;
			int stackCount = baseGravitation.getSize();

			baseGravitation.getIndexStacks((stack, i) ->
			{
				renderStack(itemRenderer, poseStack, buffer, fullTick, Mth.TWO_PI * i / stackCount, stack, pos, partialTick, packedLight, packedOverlay, randSeed + i);
			});
		});
	}

	private static void renderStack(ItemRenderer renderer, PoseStack poseStack, MultiBufferSource buffer, double fullTick, float rotAngle, TransportStack stack, Vec3 targetPos, float partialTick, int packedLight, int packedOverlay, int randSeed)
	{
		if (stack == null)
			return;

		poseStack.pushPose();

		Vec3 pos = stack.getCurrentPosition(targetPos, partialTick);
		poseStack.translate(pos.x, pos.y, pos.z);

		poseStack.mulPose(Quaternion.fromXYZ(0.0f, (float)((fullTick * 0.2) % Mth.TWO_PI) - rotAngle, 0.0f));

		float f = stack.getTransitionFactor(partialTick);
		poseStack.translate(0.0, Math.sin(fullTick * 0.4 - rotAngle) * 0.2 * f, 1.0 * f);

		renderer.renderStatic(stack.getStack(), TransformType.GROUND, packedLight, packedOverlay, poseStack, buffer, randSeed);

		poseStack.popPose();
	}
}
