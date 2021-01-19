package org.processmining.plugins.inductiveVisualMiner.chain;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.processmining.framework.plugin.ProMCanceller;
import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerPanel;
import org.processmining.plugins.inductiveVisualMiner.configuration.InductiveVisualMinerConfiguration;

/**
 * Idea: keep the requests in a non-blocking queue, and have a separate thread
 * process them.
 * 
 * @author sander
 *
 */
public class DataChainImplNonBlocking extends DataChainAbstract {

	protected static class QueueItem {

	}

	protected static class QueueItemExecuteLink extends QueueItem {
		protected final DataChainLink chainLink;

		public QueueItemExecuteLink(DataChainLink chainLink) {
			this.chainLink = chainLink;
		}
	}

	protected static class QueueItemSetObject<C> extends QueueItem {
		public final IvMObject<C> object;
		public final C value;

		public QueueItemSetObject(IvMObject<C> object, C value) {
			this.object = object;
			this.value = value;
		}
	}

	protected static class QueueItemSetFixedObject<C> extends QueueItem {
		public final IvMObject<C> object;
		public final C value;

		public QueueItemSetFixedObject(IvMObject<C> object, C value) {
			this.object = object;
			this.value = value;
		}
	}

	protected static class QueueItemResults extends QueueItem {
		public final IvMCanceller canceller;
		public final DataChainLinkComputation chainLink;
		public final IvMObjectValues outputs;

		public QueueItemResults(IvMCanceller canceller, DataChainLinkComputation chainLink, IvMObjectValues outputs) {
			this.canceller = canceller;
			this.chainLink = chainLink;
			this.outputs = outputs;
		}
	}

	protected static class QueueItemRegister extends QueueItem {
		public final DataChainLink chainLink;

		public QueueItemRegister(DataChainLink chainLink) {
			this.chainLink = chainLink;
		}
	}

	protected static class QueueItemGetObjectValues extends QueueItem {
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
	private final ConcurrentLinkedQueue<QueueItem> queue = new ConcurrentLinkedQueue<>();
	private final Semaphore semaphore = new Semaphore(0);

	private final DataChainInternal chainInternal;
	private final ProMCanceller globalCanceller;

	public DataChainImplNonBlocking(DataState state, ProMCanceller canceller, Executor executor,
			InductiveVisualMinerConfiguration configuration, InductiveVisualMinerPanel panel) {
		this.globalCanceller = canceller;
		chainInternal = new DataChainInternal(this, state, canceller, executor, configuration, panel);

		Thread thread = new Thread(chainThread, "IvM chain thread");
		thread.start();
	}

	@Override
	public void register(DataChainLink chainLink) {
		addQueueItem(new QueueItemRegister(chainLink));
	}

	@Override
	public <C> void setObject(IvMObject<C> objectName, C object) {
		addQueueItem(new QueueItemSetObject<C>(objectName, object));
	}

	@Override
	public void executeLink(Class<? extends DataChainLink> clazz) {
		DataChainLink chainLink = chainInternal.getChainLink(clazz);
		if (chainLink != null) {
			executeLink(chainLink);
		}
	}

	@Override
	public void executeLink(DataChainLink chainLink) {
		addQueueItem(new QueueItemExecuteLink(chainLink));
	}

	@Override
	public FutureImpl getObjectValues(IvMObject<?>... objects) {
		QueueItemGetObjectValues queueItem = new QueueItemGetObjectValues(objects);
		addQueueItem(queueItem);
		return queueItem.values;
	}

	@Override
	public <C> void setFixedObject(IvMObject<C> object, C value) {
		addQueueItem(new QueueItemSetFixedObject<C>(object, value));
	}

	private void addQueueItem(QueueItem queueItem) {
		queue.add(queueItem);
		semaphore.release();
	}

	public void processOutputsOfChainLink(IvMCanceller canceller, DataChainLinkComputation chainLink,
			IvMObjectValues outputs) {
		queue.add(new QueueItemResults(canceller, chainLink, outputs));
		semaphore.release();
	}

	private Runnable chainThread = new Runnable() {
		public void run() {
			while (!globalCanceller.isCancelled()) { //loop until IvM closes

				//wait for a signal on the queue
				try {
					semaphore.tryAcquire(1, TimeUnit.SECONDS);
				} catch (InterruptedException e) {
					return;
				}

				if (!queue.isEmpty()) {
					QueueItem item = queue.poll();

					if (item instanceof QueueItemSetObject<?>) {
						chainThreadSetObject((QueueItemSetObject<?>) item);
					} else if (item instanceof QueueItemExecuteLink) {
						chainInternal.executeLink(((QueueItemExecuteLink) item).chainLink);
					} else if (item instanceof QueueItemResults) {
						chainInternal.processOutputsOfChainLink(((QueueItemResults) item).canceller,
								((QueueItemResults) item).chainLink, ((QueueItemResults) item).outputs);
					} else if (item instanceof QueueItemRegister) {
						chainInternal.register(((QueueItemRegister) item).chainLink);
					} else if (item instanceof QueueItemGetObjectValues) {
						chainInternal.getObjectValues(((QueueItemGetObjectValues) item).objects,
								((QueueItemGetObjectValues) item).values);
					} else if (item instanceof QueueItemSetFixedObject<?>) {
						chainThreadSetFixedObject((QueueItemSetFixedObject<?>) item);
					}
				}
			}
		}

		private <C> void chainThreadSetObject(QueueItemSetObject<C> item) {
			chainInternal.setObject(item.object, item.value);
		}

		private <C> void chainThreadSetFixedObject(QueueItemSetFixedObject<C> item) {
			chainInternal.setObject(item.object, item.value);
		}

	};

	@Override
	public Dot toDot() {
		return chainInternal.toDot();
	}

}