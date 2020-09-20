package org.processmining.plugins.inductiveVisualMiner.alignment;

import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.extension.std.XLifecycleExtension;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.plugins.InductiveMiner.Triple;
import org.processmining.plugins.inductiveVisualMiner.alignment.Move.Type;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogNotFiltered;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogNotFilteredImpl;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTraceImpl;
import org.processmining.plugins.inductiveVisualMiner.performance.Performance;
import org.processmining.plugins.inductiveVisualMiner.performance.Performance.PerformanceTransition;
import org.processmining.plugins.petrinet.replayresult.StepTypes;
import org.processmining.plugins.replayer.replayresult.SyncReplayResult;

import gnu.trove.map.TObjectIntMap;

public class AcceptingPetriNetAlignmentCallbackImplEfficientTree implements AcceptingPetriNetAlignmentCallback {

	//input
	private final XLog xLog;
	private final IvMModel model;
	private final IvMEventClasses activityEventClasses;

	private final TObjectIntMap<Transition> performanceTransition2node;
	private final Set<Transition> skipEnqueueTransitions;

	//output
	private final IvMLogNotFilteredImpl alignedLog;

	public AcceptingPetriNetAlignmentCallbackImplEfficientTree(XLog xLog, IvMModel model,
			IvMEventClasses activityEventClasses,
			Triple<AcceptingPetriNet, TObjectIntMap<Transition>, Set<Transition>> p) {
		assert model.isTree();

		this.xLog = xLog;
		this.model = model;
		this.activityEventClasses = activityEventClasses;

		this.performanceTransition2node = p.getB();
		this.skipEnqueueTransitions = p.getC();

		alignedLog = new IvMLogNotFilteredImpl(xLog.size(), xLog.getAttributes());
	}

	public static String getTraceName(XTrace xTrace) {
		if (xTrace.getAttributes().containsKey("concept:name")) {
			return xTrace.getAttributes().get("concept:name").toString();
		} else {
			return "";
		}
	}

	public void traceAlignmentComplete(SyncReplayResult aTrace, SortedSet<Integer> xTraces,
			IvMEventClasses performanceEventClasses) {

		for (Integer traceIndex : xTraces) {
			XTrace xTrace = xLog.get(traceIndex);

			//get trace name
			String traceName = getTraceName(xTrace);

			IvMTrace iTrace = new IvMTraceImpl(traceName, xTrace.getAttributes(), aTrace.getNodeInstance().size());
			Iterator<StepTypes> itType = aTrace.getStepTypes().iterator();
			Iterator<Object> itNode = aTrace.getNodeInstance().iterator();
			int eventIndex = 0;
			int previousModelNode = -1;
			int moveIndex = 0;
			while (itType.hasNext()) {
				StepTypes type = itType.next();
				Object node = itNode.next();
				Move move = getMove(xTrace, type, node, performanceEventClasses, eventIndex, previousModelNode,
						moveIndex);

				if (move != null) {
					iTrace.add(ETMAlignmentCallbackImpl.move2ivmMove(model, move, xTrace, eventIndex));
					moveIndex++;
				}

				if (move != null && (type == StepTypes.L || type == StepTypes.LMGOOD)) {
					eventIndex++;
				}

			}

			alignedLog.set(traceIndex, iTrace);
		}
	}

	/**
	 * 
	 * @param trace
	 * @param type
	 * @param node
	 * @param performanceEventClasses
	 * @param event
	 * @param previousModelNode
	 * @return the move and the model move
	 */
	private Move getMove(XTrace trace, StepTypes type, Object node, IvMEventClasses performanceEventClasses, int event,
			int previousModelNode, int moveIndex) {

		//get log part of move
		XEventClass performanceActivity = null;
		XEventClass activity = null;
		PerformanceTransition lifeCycleTransition = null;
		if (type == StepTypes.L || type == StepTypes.LMGOOD) {
			//a log-move happened

			if (type == StepTypes.L) {
				assert node instanceof XEventClass;
				performanceActivity = (XEventClass) node;
			} else { //type == StepTypes.LMGOOD
				assert node instanceof Transition;
				performanceActivity = performanceEventClasses.getClassOf(trace.get(event));
			}
			activity = Performance.getActivity(performanceActivity, activityEventClasses);
			lifeCycleTransition = Performance.getLifeCycleTransition(performanceActivity);
		}

		//get model part of move
		Transition performanceTransition = null;
		int unode = -1;
		if (type == StepTypes.MREAL || type == StepTypes.LMGOOD || type == StepTypes.MINVI) {
			//a model-move happened

			assert (node instanceof Transition);

			performanceTransition = (Transition) node;
			unode = performanceTransition2node.get(performanceTransition);
			lifeCycleTransition = Performance.getLifeCycleTransition(performanceTransition);

			if (!model.isTau(unode) && !model.isActivity(unode)) {
				//we are only interested in activity and tau moves; not in other model elements
				return null;
			}

			if (performanceTransition.isInvisible() && model.isActivity(unode)) {
				//this is a tau that represents that the start/enqueue of an activity is skipped
				if (skipEnqueueTransitions.contains(performanceTransition)) {
					lifeCycleTransition = PerformanceTransition.enqueue;
					performanceActivity = performanceEventClasses
							.getByIdentity(model.getActivityName(unode) + "+enqueue");
				} else {
					lifeCycleTransition = PerformanceTransition.start;
					performanceActivity = performanceEventClasses.getByIdentity(
							model.getActivityName(unode) + "+" + XLifecycleExtension.StandardModel.START);
				}
				activity = activityEventClasses.getByIdentity(model.getActivityName(unode));
			} else if (performanceTransition.isInvisible()) {
				//a model move happened on a tau; make it a completion
				lifeCycleTransition = PerformanceTransition.complete;
			}

		}

		if (performanceTransition != null || performanceActivity != null) {
			if (performanceTransition != null && performanceTransition.isInvisible() && model.isActivity(unode)) {
				//tau-enqueue or tau-start
				return new Move(model, Type.ignoredModelMove, -2, unode, activity, performanceActivity,
						lifeCycleTransition, moveIndex);
			} else if ((performanceTransition != null && performanceActivity != null)
					|| (performanceTransition != null && performanceTransition.isInvisible())) {
				//synchronous move
				return new Move(model, Type.synchronousMove, -2, unode, activity, performanceActivity,
						lifeCycleTransition, moveIndex);
			} else if (performanceTransition != null) {
				//model move
				return new Move(model, Type.modelMove, -2, unode, activity, performanceActivity, lifeCycleTransition,
						moveIndex);
			} else {
				//log move
				if (lifeCycleTransition == PerformanceTransition.complete) {
					//only log moves of complete events are interesting
					return new Move(model, Type.logMove, -2, unode, activity, performanceActivity, lifeCycleTransition,
							moveIndex);
				} else {
					//log moves of other transitions are ignored
					return new Move(model, Type.ignoredLogMove, -2, -1, activity, performanceActivity,
							lifeCycleTransition, moveIndex);
				}
			}
		}
		return null;
	}

	public void alignmentFailed() throws Exception {
		// TODO Auto-generated method stub

	}

	public IvMLogNotFiltered getAlignedLog() {
		return alignedLog;
	}

}
