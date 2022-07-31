package fr.maaxed.gravitationalsorcery.capability;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ClientGravitationHandler extends BaseGravitationHandler
{
	private final List<IndexStack> extracted = new ArrayList<>();
	public ClientGravitationHandler(int capacity)
	{
		super(capacity);
	}

	public int getSize()
	{
		return this.itemQueue.length;
	}

	public void getIndexStacks(IIndexStackConsumer consumer)
	{
		for (int i = 0; i < this.itemQueue.length; i++)
		{
			consumer.apply(this.itemQueue[i], i);
		}

		for (IndexStack is : this.extracted)
		{
			consumer.apply(is.stack, is.index);
		}
	}

	@Override
	protected void onExtracted(TransportStack stack, int index)
	{
		super.onExtracted(stack, index);

		this.extracted.add(new IndexStack(stack, index));
	}

	@Override
	public void update()
	{
		super.update();
		Iterator<IndexStack> iter = this.extracted.iterator();
		while(iter.hasNext())
		{
			TransportStack stack = iter.next().stack;
			stack.update();
			if (stack.shouldGetRomoved())
			{
				iter.remove();
			}
		}
	}

	@FunctionalInterface
	public static interface IIndexStackConsumer
	{
		void apply(TransportStack stack, int index);
	}

	private static class IndexStack
	{
		public final TransportStack stack;
		public final int index;

		public IndexStack(TransportStack stack, int index)
		{
			this.stack = stack;
			this.index = index;
		}
	}
}
