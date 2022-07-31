package fr.maaxed.gravitationalsorcery.capability;

import fr.maaxed.gravitationalsorcery.init.ModNetwork;
import fr.maaxed.gravitationalsorcery.network.ExtractTransportationMessage;
import fr.maaxed.gravitationalsorcery.network.InsertTransportationMessage;
import fr.maaxed.gravitationalsorcery.util.CapabilityProviderHolder;

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
