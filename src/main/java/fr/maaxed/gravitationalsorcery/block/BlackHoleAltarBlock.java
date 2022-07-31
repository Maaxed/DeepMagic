package fr.maaxed.gravitationalsorcery.block;

import fr.maaxed.gravitationalsorcery.capability.BaseGravitationHandler;
import fr.maaxed.gravitationalsorcery.capability.CapabilityGravitationHandler;
import fr.maaxed.gravitationalsorcery.init.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BlackHoleAltarBlock extends Block implements EntityBlock
{
	protected static final VoxelShape SHAPE = Shapes.or(
			box(0.0, 0.0, 0.0, 16.0, 5.0, 16.0),
			box(5.0, 5.0, 5.0, 11.0, 11.0, 11.0),
			box( 5.0, 5.0,  0.0, 11.0, 9.0,  5.0),
			box( 0.0, 5.0,  5.0,  5.0, 9.0, 11.0),
			box( 5.0, 5.0, 11.0, 11.0, 9.0, 16.0),
			box(11.0, 5.0,  5.0, 16.0, 9.0, 11.0)
		);

	public BlackHoleAltarBlock(Properties properties)
	{
		super(properties);
	}

	public VoxelShape getShape(BlockState state, BlockGetter lvl, BlockPos pos, CollisionContext context)
	{
		return SHAPE;
	}

	@Override
	public RenderShape getRenderShape(BlockState state)
	{
		return RenderShape.MODEL;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
	{
		return new BlackHoleAltarBlockEntity(pos, state);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type)
	{
		return type == ModBlocks.BLACK_HOLE_ALTAR_BLOCKENTITY.get() ? (BlockEntityTicker<T>) (BlockEntityTicker<BlackHoleAltarBlockEntity>)BlackHoleAltarBlockEntity::tick : null;
	}

	public boolean triggerEvent(BlockState state, Level lvl, BlockPos pos, int eventId, int eventValue)
	{
		super.triggerEvent(state, lvl, pos, eventId, eventValue);

		BlockEntity be = lvl.getBlockEntity(pos);
		if (be == null)
			return false;

		return be.triggerEvent(eventId, eventValue);
	}

	public void onRemove(BlockState state, Level lvl, BlockPos pos, BlockState newState, boolean someFlag)
	{
		if (state.is(newState.getBlock()))
			return;

		BlockEntity be = lvl.getBlockEntity(pos);
		if (be != null)
		{
			// Drop transportation items
			be.getCapability(CapabilityGravitationHandler.GRAVITATION_HANDLER_CAPABILITY).ifPresent(transportation ->
			{
				if (!(transportation instanceof BaseGravitationHandler bth))
					return;

				Containers.dropContents(lvl, pos, bth.getDrops());
			});
		}

		super.onRemove(state, lvl, pos, newState, someFlag);
	}
}
