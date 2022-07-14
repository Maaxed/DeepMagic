package fr.max2.deepmagic.capability;

import fr.max2.deepmagic.init.ModNetwork;
import fr.max2.deepmagic.network.EntityExtractTransportationMessage;
import fr.max2.deepmagic.network.EntityInsertTransportationMessage;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.PacketDistributor;

public class SyncTransportationHandler extends BaseTransportationHandler
{
	private final Entity target;

	public SyncTransportationHandler(int capacity, Entity target)
	{
		super(capacity);
		this.target = target;
	}

	@Override
	protected void onInserted(TransportStack stack, int index)
	{
		super.onInserted(stack, index);

		ModNetwork.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> target), new EntityInsertTransportationMessage(this.target, index, stack));
	}

	@Override
	protected void onExtracted(TransportStack stack, int index)
	{
		super.onExtracted(stack, index);

		ModNetwork.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> target), new EntityExtractTransportationMessage(this.target, index, stack));
	}
}
