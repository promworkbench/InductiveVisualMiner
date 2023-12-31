package org.processmining.plugins.inductiveVisualMiner.chain;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.processmining.framework.plugin.ProMCanceller;
import org.processmining.plugins.graphviz.dot.Dot;

/**
 * Idea: keep the requests in a non-blocking queue, and have a separate thread
 * process them.
 * 
 * @author sander
 * @param <C>
 *            configuration
 * @param <P>
 *            panel
 *
 */
public class DataChainImplNonBlocking<C, P> extends DataChainAbstract<C> {

	protected static class QueueItem<C> {

	}

	protected static class QueueItemExecuteLink<C> extends QueueItem<C> {
		protected final DataChainLink<C> chainLink;

		public QueueItemExecuteLink(DataChainLink<C> chainLink) {
			this.chainLink = chainLink;
		}
	}

	protected static class QueueItemSetObject<C, O> extends QueueItem<C> {
		public final IvMObject<O> object;
		public final O value;

		public QueueItemSetObject(IvMObject<O> object, O value) {
			this.object = object;
			this.value = value;
		}
	}

	protected static class QueueItemSetFixedObject<C, O> extends QueueItem<C> {
		public final IvMObject<O> object;
		public final O value;

		public QueueItemSetFixedObject(IvMObject<O> object, O value) {
			this.object = object;
			this.value = value;
		}
	}

	protected static class QueueItemResults<C> extends QueueItem<C> {
		public final IvMCanceller canceller;
		public final DataChainLinkComputation<C> chainLink;
		public final IvMObjectValues outputs;

		public QueueItemResults(IvMCanceller canceller, DataChainLinkComputation<C> chainLink,
				IvMObjectValues outputs) {
			this.canceller = canceller;
			this.chainLink = chainLink;
			this.outputs = outputs;
		}
	}

	protected static class QueueItemRegister<C> extends QueueItem<C> {
		public final DataChainLink<C> chainLink;

		public QueueItemRegister(DataChainLink<C> chainLink) {
			this.chainLink = chainLink;
		}
	}

	protected static class QueueItemGetObjectValues<C> extends QueueItem<C> {
		public final IvMObject<?>[] objects;
		public final FutureImpl values;

		public QueueItemGetObjectValues(IvMObject<?>[] objects) {
			this.objects = objects;
			values = new FutureImpl();
		}
	}

	/**
	 * The threading works using a queue (to hold what is in the elements), and
	 * a semaphore (to wake up the processing thread). This avoids locks in the
	 * methods, such that the gui remains responsive.
	 */
	private final ConcurrentLinkedQueue<QueueItem<C>> queue = new ConcurrentLinkedQueue<>();
	private final Semaphore semaphore = new Semaphore(0);

	private final DataChainInternal<C, P> chainInternal;
	private final ProMCanceller globalCanceller;

	public DataChainImplNonBlocking(DataState state, ProMCanceller canceller, Executor executor, C configuration,
			P panel) {
		this.globalCanceller = canceller;
		chainInternal = new DataChainInternal<>(this, state, canceller, executor, configuration, panel);

		Thread thread = new Thread(chainThread, "IvM chain thread");
		thread.start();
	}

	@Override
	public void register(DataChainLink<C> chainLink) {
		addQueueItem(new QueueItemRegister<>(chainLink));
	}

	@Override
	public <O> void setObject(IvMObject<O> objectName, O object) {
		addQueueItem(new QueueItemSetObject<C, O>(objectName, object));
	}

	@Override
	public void executeLink(Class<? extends DataChainLink<C>> clazz) {
		DataChainLink<C> chainLink = chainInternal.getChainLink(clazz);
		if (chainLink != null) {
			executeLink(chainLink);
		}
	}

	@Override
	public void executeLink(DataChainLink<C> chainLink) {
		addQueueItem(new QueueItemExecuteLink<>(chainLink));
	}

	@Override
	public FutureImpl getObjectValues(IvMObject<?>... objects) {
		QueueItemGetObjectValues<C> queueItem = new QueueItemGetObjectValues<>(objects);
		addQueueItem(queueItem);
		return queueItem.values;
	}

	@Override
	public <O> void setFixedObject(IvMObject<O> object, O value) {
		addQueueItem(new QueueItemSetFixedObject<C, O>(object, value));
	}

	private void addQueueItem(QueueItem<C> queueItem) {
		queue.add(queueItem);
		semaphore.release();
	}

	public void processOutputsOfChainLink(IvMCanceller canceller, DataChainLinkComputation<C> chainLink,
			IvMObjectValues outputs) {
		queue.add(new QueueItemResults<>(canceller, chainLink, outputs));
		semaphore.release();
	}

	private Runnable chainThread = new Runnable() {
		@SuppressWarnings("unchecked")
		public void run() {
			while (!globalCanceller.isCancelled()) { //loop until IvM closes

				//wait for a signal on the queue
				try {
					semaphore.tryAcquire(1, TimeUnit.SECONDS);
				} catch (InterruptedException e) {
					return;
				}

				if (!queue.isEmpty()) {
					QueueItem<C> item = queue.poll();

					if (item instanceof QueueItemSetObject) {
						chainThreadSetObject((QueueItemSetObject<C, ?>) item);
					} else if (item instanceof QueueItemExecuteLink) {
						chainInternal.executeLink(((QueueItemExecuteLink<C>) item).chainLink);
					} else if (item instanceof QueueItemResults) {
						chainInternal.processOutputsOfChainLink(((QueueItemResults<C>) item).canceller,
								((QueueItemResults<C>) item).chainLink, ((QueueItemResults<C>) item).outputs);
					} else if (item instanceof QueueItemRegister) {
						chainInternal.register(((QueueItemRegister<C>) item).chainLink);
					} else if (item instanceof QueueItemGetObjectValues) {
						chainInternal.getObjectValues(((QueueItemGetObjectValues<C>) item).objects,
								((QueueItemGetObjectValues<C>) item).values);
					} else if (item instanceof QueueItemSetFixedObject) {
						chainThreadSetFixedObject(((QueueItemSetFixedObject<C, ?>) item));
					}
				}
			}
		}

		private <O> void chainThreadSetObject(QueueItemSetObject<C, O> item) {
			chainInternal.setObject(item.object, item.value);
		}

		private <O> void chainThreadSetFixedObject(QueueItemSetFixedObject<C, O> item) {
			chainInternal.setFixedObject(item.object, item.value);
		}

	};

	@Override
	public Dot toDot() {
		return chainInternal.toDot();
	}

}