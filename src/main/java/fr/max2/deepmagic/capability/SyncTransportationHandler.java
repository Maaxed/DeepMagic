package fr.max2.deepmagic.capability;

import fr.max2.deepmagic.init.ModNetwork;
import fr.max2.deepmagic.network.ExtractTransportationMessage;
import fr.max2.deepmagic.util.CapabilityProviderHolder;
import fr.max2.deepmagic.network.InsertTransportationMessage;

public class SyncTransportationHandler extends BaseTransportationHandler
{
	private final CapabilityProviderHolder target;

	public SyncTransportationHandler(int capacity, CapabilityProviderHolder target)
	{
		super(capacity);
		this.target = target;
	}

	@Override
	protected void onInserted(TransportStack stack, int index)
	{
		super.onInserted(stack, index);
		this.target.setChanged();

		ModNetwork.CHANNEL.send(this.target.getPacketDistributor(), new InsertTransportationMessage(this.target, stack));
	}

	@Override
	protected void onExtracted(TransportStack stack, int index)
	{
		super.onExtracted(stack, index);
		this.target.setChanged();

		ModNetwork.CHANNEL.send(this.target.getPacketDistributor(), new ExtractTransportationMessage(this.target, stack));
	}
}
