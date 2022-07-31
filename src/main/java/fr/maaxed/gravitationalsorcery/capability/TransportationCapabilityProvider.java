package fr.maaxed.gravitationalsorcery.capability;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.core.Direction;
import net.minecraft.nbt.Tag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;

public class TransportationCapabilityProvider<T extends Tag, C extends ITransportationHandler & INBTSerializable<T>> implements ICapabilitySerializable<T>
{
	private final C capabilityValue;
	private final LazyOptional<ITransportationHandler> lazyCapability;

	public TransportationCapabilityProvider(C capabilityValue)
	{
		this.capabilityValue = capabilityValue;
		this.lazyCapability = LazyOptional.of(() -> capabilityValue);
	}

	public void invalidate()
	{
		this.lazyCapability.invalidate();
	}

	@Override
	public <U> @NotNull LazyOptional<U> getCapability(@NotNull Capability<U> cap, @Nullable Direction side)
	{
		return CapabilityTransportationHandler.TRANSPORTATION_HANDLER_CAPABILITY.orEmpty(cap, this.lazyCapability);
	}

	@Override
	public T serializeNBT()
	{
		return this.capabilityValue.serializeNBT();
	}

	@Override
	public void deserializeNBT(T nbt)
	{
		this.capabilityValue.deserializeNBT(nbt);
	}
	
}
