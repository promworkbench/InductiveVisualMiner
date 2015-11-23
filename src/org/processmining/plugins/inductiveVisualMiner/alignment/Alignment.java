package org.processmining.plugins.inductiveVisualMiner.alignment;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import nl.tue.astar.AStarException;
import nl.tue.astar.AStarThread.ASynchronousMoveSorting;
import nl.tue.astar.AStarThread.Canceller;
import nl.tue.astar.AStarThread.Type;
import nl.tue.astar.Tail;
import nl.tue.astar.Trace;
import nl.tue.astar.impl.AbstractAStarThread;
import nl.tue.astar.impl.AbstractAStarThread.QueueingModel;
import nl.tue.astar.impl.memefficient.MemoryEfficientAStarAlgorithm;
import nl.tue.astar.impl.memefficient.StorageAwareDelegate;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.model.XLog;
import org.processmining.plugins.etm.model.narytree.NAryTree;
import org.processmining.plugins.etm.model.narytree.conversion.ProcessTreeToNAryTree;
import org.processmining.plugins.etm.model.narytree.replayer.AbstractNAryTreeDelegate;
import org.processmining.plugins.etm.model.narytree.replayer.NAryTreeHead;
import org.processmining.plugins.etm.model.narytree.replayer.StubbornNAryTreeAStarThread;
import org.processmining.plugins.etm.model.narytree.replayer.TreeRecord;
import org.processmining.plugins.etm.model.narytree.replayer.hybridilp.NAryTreeHybridILPDelegate;
import org.processmining.processtree.Block;
import org.processmining.processtree.Node;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.impl.AbstractTask.Manual;

/**
 * Align a log with the ETM replayer, but do not compute precision.
 * 
 * @author sleemans
 *
 */

public class Alignment {

	public final static int maxStates = 1 << 24;;
	public final static double traceTimeOutInSec = -1;
	public final static int numberOfThreads = Math.max(Runtime.getRuntime().availableProcessors() - 2, 1);
	private static final int modelCost = 2;
	private static final Integer logCost = 2;

	private final AtomicBoolean wasReliable;
	private final Canceller canceller;
	private final XEventClasses eventClasses;
	private final NAryTree nTree;
	private final int[] node2cost;
	private final Map<XEventClass, Integer> activity2Cost;
	private final XLog log;
	private final AlignmentResult callback;

	private final AtomicInteger tracesStarted;

	/**
	 * Make sure that all event classes of log AND MODEL are in eventclasses.
	 * (use the static function addAllLeavesAsEventClasses to achieve that).
	 * 
	 * @param tree
	 * @param log
	 * @param eventClasses
	 * @param callback
	 * @param canceller
	 */
	public Alignment(ProcessTree tree, XLog log, XEventClasses eventClasses, AlignmentResult callback,
			Canceller canceller) {
		wasReliable = new AtomicBoolean(true);
		this.canceller = canceller;
		this.log = log;
		this.callback = callback;
		this.eventClasses = eventClasses;

		//convert the process tree to an narytree using the event classes
		nTree = new ProcessTreeToNAryTree(eventClasses).convert(tree);

		node2cost = getNode2cost(nTree);
		activity2Cost = createActivity2cost(eventClasses);
		tracesStarted = new AtomicInteger(-1);
	}

	/**
	 * Align a log.
	 * 
	 * @param tree
	 * @param log
	 * @param eventClasses
	 * @param activity2Cost
	 * @throws Exception
	 */
	public void alignLog() throws Exception {
		final AStarAlgorithmWithLog algorithm = new AStarAlgorithmWithLog(log, eventClasses, activity2Cost);
		final AbstractNAryTreeDelegate<? extends Tail> delegate = new NAryTreeHybridILPDelegate(algorithm, nTree, -1,
				node2cost, numberOfThreads, false);
		final MemoryEfficientAStarAlgorithm<NAryTreeHead, Tail> memEffAlg = new MemoryEfficientAStarAlgorithm<NAryTreeHead, Tail>(
				(StorageAwareDelegate<NAryTreeHead, Tail>) delegate);

		final Trace[] traces = algorithm.getConvertedLog().keys(new Trace[algorithm.getConvertedLog().size()]);
		ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
		for (int t = 0; t < numberOfThreads; t++) {
			executor.execute(new Runnable() {
				public void run() {
					while (wasReliable.get() && !canceller.isCancelled()) {
						int traceIndex = tracesStarted.incrementAndGet();
						if (traceIndex < traces.length) {
							TreeRecord traceAlignment;
							try {
								traceAlignment = alignTrace(nTree, traces[traceIndex], delegate, memEffAlg);
								if (wasReliable.get()) {
									callback.traceAlignmentComplete(traces[traceIndex], traceAlignment, algorithm
											.getXTracesOf(traces[traceIndex]).toArray());
								}
							} catch (AStarException e) {
								wasReliable.set(false);
								e.printStackTrace();
							}
						} else {
							return;
						}
					}
				}
			});
		}
		executor.shutdown();
		executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);

		if (!wasReliable.get()) {
			callback.alignmentFailed();
		}
	}

	/**
	 * Align a single trace. Thread-safe.
	 * 
	 * @param tree
	 * @param trace
	 * @throws AStarException
	 */
	private TreeRecord alignTrace(final NAryTree tree, final Trace trace,
			final AbstractNAryTreeDelegate<? extends Tail> delegate,
			MemoryEfficientAStarAlgorithm<NAryTreeHead, Tail> memEffAlg) throws AStarException {
		NAryTreeHead initialHead = new NAryTreeHead(delegate, trace);

		AbstractAStarThread<NAryTreeHead, Tail> thread = new StubbornNAryTreeAStarThread.MemoryEfficient<NAryTreeHead, Tail>(
				tree, memEffAlg, initialHead, trace, maxStates);

		thread.setQueueingModel(QueueingModel.DEPTHFIRST);
		thread.setType(Type.PLAIN);
		thread.setASynchronousMoveSorting(ASynchronousMoveSorting.NONE);//MODELMOVEFIRST);

		TreeRecord rec = (TreeRecord) thread.getOptimalRecord(canceller, traceTimeOutInSec);

		/*
		 * Check whether this trace was aligned reliably, i.e. whether it did
		 * not time out or ran out of memory.
		 */
		wasReliable.compareAndSet(true, thread.wasReliable());
		if (!wasReliable.get()) {
			/*
			 * Some trace was unreliable. We don't want to show false results to
			 * the user, so consider the whole alignment failed.
			 */
			return null;
		}

		double cost = rec.getCostSoFar();

		int len = 0;
		TreeRecord r = rec;
		while (r != null) {
			if (r.getModelMove() < tree.size()) {
				cost--;
			}
			cost -= r.getInternalMovesCost();
			r = r.getPredecessor();
			len++;
		}
		cost++;
		assert (cost % delegate.getScaling() == 0);

		return rec;
	}

	public static int[] getNode2cost(NAryTree nTree) {
		int[] node2cost = new int[nTree.size()];
		for (int i = 0; i < nTree.size(); i++) {
			if (nTree.isLeaf(i) && nTree.getType(i) != NAryTree.TAU) {
				node2cost[i] = modelCost;
			}
		}
		return node2cost;
	}

	private static Map<XEventClass, Integer> createActivity2cost(XEventClasses eventClasses) {
		Map<XEventClass, Integer> activity2Cost = new HashMap<XEventClass, Integer>();
		for (XEventClass activity : eventClasses.getClasses()) {
			activity2Cost.put(activity, logCost);
		}
		return activity2Cost;
	}

	public static void addAllLeavesAsEventClasses(XEventClasses eventClasses, ProcessTree tree) {
		addAllLeavesAsEventClasses(eventClasses, tree.getRoot());
		eventClasses.harmonizeIndices();
	}

	private static void addAllLeavesAsEventClasses(XEventClasses eventClasses, Node node) {
		if (node instanceof Manual) {
			eventClasses.register(node.getName());
		} else if (node instanceof Block) {
			for (Node child : ((Block) node).getChildren()) {
				addAllLeavesAsEventClasses(eventClasses, child);
			}
		}
	}
}