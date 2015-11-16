package org.processmining.plugins.inductiveVisualMiner.alignment;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import nl.tue.astar.Trace;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.extension.std.XLifecycleExtension;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.plugins.etm.model.narytree.replayer.TreeRecord;
import org.processmining.plugins.inductiveVisualMiner.alignment.Move.Type;
import org.processmining.plugins.inductiveVisualMiner.animation.TimestampsAdder;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogNotFiltered;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogNotFilteredImpl;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMMove;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTraceImpl;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.ResourceFunctions;
import org.processmining.plugins.inductiveVisualMiner.performance.Performance;
import org.processmining.plugins.inductiveVisualMiner.performance.Performance.PerformanceTransition;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.UnfoldedNode;
import org.processmining.processtree.impl.AbstractTask.Automatic;
import org.processmining.processtree.impl.AbstractTask.Manual;

/**
 * Keep trace of alignment results. This class is slightly more complicated then
 * you'd expect, as it also handles the performance extension.
 * 
 * @author sleemans
 *
 */
public class AlignmentResultImplPerformance implements AlignmentResult {

	//input
	private final XLog xLog;
	private final XEventClasses activityEventClasses;
	private final XEventClasses performanceEventClasses;
	private final Map<UnfoldedNode, UnfoldedNode> performanceNodeMapping;
	private final UnfoldedNode[] nodeId2performanceNode;
	private final Set<UnfoldedNode> enqueueTaus;

	//output
	private final AtomicInteger tracesDone;
	private final IvMLogNotFilteredImpl alignedLog;

	public AlignmentResultImplPerformance(XLog xLog, XEventClasses activityEventClasses,
			Map<UnfoldedNode, UnfoldedNode> performanceNodeMapping, XEventClasses performanceEventClasses,
			UnfoldedNode[] nodeId2performanceNode, Set<UnfoldedNode> enqueueTaus) {
		this.xLog = xLog;
		this.activityEventClasses = activityEventClasses;
		this.performanceNodeMapping = performanceNodeMapping;
		this.performanceEventClasses = performanceEventClasses;
		this.nodeId2performanceNode = nodeId2performanceNode;
		this.enqueueTaus = enqueueTaus;

		tracesDone = new AtomicInteger(0);
		alignedLog = new IvMLogNotFilteredImpl(xLog.size());
	}

	public void traceAlignmentComplete(Trace trace, TreeRecord traceAlignment, int[] xtracesRepresented) {

		//for each XTrace in the log that this aligned trace represents...
		for (int traceIndex : xtracesRepresented) {

			XTrace xTrace = xLog.get(traceIndex);
			//get trace name
			String traceName;
			if (xTrace.getAttributes().containsKey("concept:name")) {
				traceName = xTrace.getAttributes().get("concept:name").toString();
			} else {
				traceName = "";
			}

			//reverse the alignment result
			List<TreeRecord> aTrace = TreeRecord.getHistory(traceAlignment);

			IvMTrace iTrace = new IvMTraceImpl(traceName, xTrace.getAttributes(), aTrace.size());

			//walk through the trace and translate moves
			for (TreeRecord aMove : aTrace) {
				Move move = getMove(aMove, trace);
				if (move != null) {
					iTrace.add(move2ivmMove(move, xTrace, aMove.getMovedEvent()));
				}
			}

			alignedLog.set(traceIndex, iTrace);
		}
	}

	public Move getMove(TreeRecord naryMove, Trace trace) {
		//get log part of move
		XEventClass performanceActivity = null;
		XEventClass activity = null;
		PerformanceTransition lifeCycleTransition = null;
		if (naryMove.getMovedEvent() >= 0) {
			//an ETM-log-move happened
			performanceActivity = performanceEventClasses.getByIndex(trace.get(naryMove.getMovedEvent()));
			activity = Performance.getActivity(performanceActivity, activityEventClasses);
			lifeCycleTransition = Performance.getLifeCycleTransition(performanceActivity);
		}

		//get model part of move
		UnfoldedNode performanceUnode = null;
		UnfoldedNode unode = null;
		if (naryMove.getModelMove() >= 0) {
			//an ETM-model-move happened
			performanceUnode = nodeId2performanceNode[naryMove.getModelMove()];
			unode = performanceNodeMapping.get(performanceUnode);
			lifeCycleTransition = Performance.getLifeCycleTransition(performanceUnode);

			if (performanceUnode.getNode() instanceof Automatic && unode.getNode() instanceof Manual) {
				//this is a tau that represents that the start/enqueue of an activity is skipped
				if (enqueueTaus.contains(performanceUnode)) {
					lifeCycleTransition = PerformanceTransition.enqueue;
					performanceActivity = performanceEventClasses.getByIdentity(unode.getNode().getName() + "+enqueue");
				} else {
					lifeCycleTransition = PerformanceTransition.start;
					performanceActivity = performanceEventClasses.getByIdentity(unode.getNode().getName() + "+"
							+ XLifecycleExtension.StandardModel.START);
				}
				activity = activityEventClasses.getByIdentity(unode.getNode().getName());
			} else if (performanceUnode.getNode() instanceof Automatic) {
				//a model move happened on a tau; make it a completion
				lifeCycleTransition = PerformanceTransition.complete;
			}

			//we are only interested in moves on leaves, not in moves on nodes
			if (!(performanceUnode.getNode() instanceof Manual) && !(performanceUnode.getNode() instanceof Automatic)) {
				return null;
			}
		}

		if (performanceUnode != null || performanceActivity != null) {
			if (performanceUnode != null && performanceUnode.getNode() instanceof Automatic
					&& unode.getNode() instanceof Manual) {
				//tau-start
				return new Move(Type.ignoredModelMove, unode, activity, performanceActivity, lifeCycleTransition);
			} else if ((performanceUnode != null && performanceActivity != null)
					|| (performanceUnode != null && performanceUnode.getNode() instanceof Automatic)) {
				//synchronous move
				return new Move(Type.synchronousMove, unode, activity, performanceActivity, lifeCycleTransition);
			} else if (performanceUnode != null) {
				//model move
				return new Move(Type.modelMove, unode, activity, performanceActivity, lifeCycleTransition);
			} else {
				//log move
				if (lifeCycleTransition == PerformanceTransition.complete) {
					//only log moves of complete events are interesting
					return new Move(Type.logMove, unode, activity, performanceActivity, lifeCycleTransition);
				} else {
					//log moves of other transitions are ignored
					return new Move(Type.ignoredLogMove, null, activity, performanceActivity, lifeCycleTransition);
				}
			}
		}
		return null;
	}

	public static IvMMove move2ivmMove(Move move, XTrace trace, int eventIndex) {
		if (move.isTauStart()) {
			//tau-start
			return new IvMMove(move, null, null, null);
		} else if (move.getActivityEventClass() != null) {
			//sync move or log move

			XEvent event = trace.get(eventIndex);
			Long timestamp = TimestampsAdder.getTimestamp(event);

			String resource = ResourceFunctions.getResource(event);

			return new IvMMove(move, timestamp, resource, event.getAttributes());
		} else {
			//model move or tau
			return new IvMMove(move, null, null, null);
		}
	}

	public void alignmentFailed() throws Exception {
		throw new Exception("alignment failed");
	}

	public IvMLogNotFiltered getAlignedLog() {
		return alignedLog;
	}

}
