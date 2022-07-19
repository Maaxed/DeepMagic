package fr.max2.deepmagic.block;

import fr.max2.deepmagic.init.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class TransportationBlock extends Block implements EntityBlock
{
	public TransportationBlock(Properties properties)
	{
		super(properties);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
	{
		return new TransportationBlockEntity(pos, state);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type)
	{
		return type == ModBlocks.TRANSPORTATION_BLOCKENTITY.get() ? (BlockEntityTicker<T>) (BlockEntityTicker<TransportationBlockEntity>)TransportationBlockEntity::tick : null;
	}

	public boolean triggerEvent(BlockState p_49226_, Level p_49227_, BlockPos p_49228_, int p_49229_, int p_49230_)
	{
		super.triggerEvent(p_49226_, p_49227_, p_49228_, p_49229_, p_49230_);
		BlockEntity blockentity = p_49227_.getBlockEntity(p_49228_);
		return blockentity == null ? false : blockentity.triggerEvent(p_49229_, p_49230_);
	}
}
