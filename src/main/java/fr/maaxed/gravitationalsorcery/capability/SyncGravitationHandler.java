package fr.maaxed.gravitationalsorcery.capability;

import fr.maaxed.gravitationalsorcery.init.ModNetwork;
import fr.maaxed.gravitationalsorcery.network.ExtractGravitationMessage;
import fr.maaxed.gravitationalsorcery.network.InsertGravitationMessage;
import fr.maaxed.gravitationalsorcery.util.CapabilityProviderHolder;

public class SyncGravitationHandler extends BaseGravitationHandler
{
	private final CapabilityProviderHolder target;

	public SyncGravitationHandler(int capacity, CapabilityProviderHolder target)
	{
		super(capacity);
		this.target = target;
	}

	@Override
	protected void onInserted(TransportStack stack, int index)
	{
		super.onInserted(stack, index);
		this.target.setChanged();

		ModNetwork.CHANNEL.send(this.target.getPacketDistributor(), new InsertGravitationMessage(this.target, stack));
	}

	@Override
	protected void onExtracted(TransportStack stack, int index)
	{
		super.onExtracted(stack, index);
		this.target.setChanged();

		ModNetwork.CHANNEL.send(this.target.getPacketDistributor(), new ExtractGravitationMessage(this.target, stack));
	}
}
