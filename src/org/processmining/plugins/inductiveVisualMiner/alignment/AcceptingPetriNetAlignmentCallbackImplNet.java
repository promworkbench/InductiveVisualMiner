package org.processmining.plugins.inductiveVisualMiner.alignment;

import java.util.Iterator;
import java.util.SortedSet;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.InductiveMiner.Quadruple;
import org.processmining.plugins.inductiveVisualMiner.alignment.Move.Type;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.decoration.IvMDecoratorI;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogNotFiltered;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogNotFilteredImpl;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTraceImpl;
import org.processmining.plugins.inductiveVisualMiner.performance.PerformanceTransition;
import org.processmining.plugins.inductiveVisualMiner.performance.PerformanceUtils;
import org.processmining.plugins.petrinet.replayresult.StepTypes;
import org.processmining.plugins.replayer.replayresult.SyncReplayResult;

import gnu.trove.map.TObjectIntMap;

public class AcceptingPetriNetAlignmentCallbackImplNet implements AcceptingPetriNetAlignmentCallback {

	//input
	private final XLog xLog;
	private final IvMModel model;
	private final IvMEventClasses activityEventClasses;

	private final TObjectIntMap<Transition> activity2skipEnqueue;
	private final TObjectIntMap<Transition> activity2skipStart;
	private final TObjectIntMap<Transition> activity2node;
	private final IvMDecoratorI decorator;

	//output
	private final IvMLogNotFilteredImpl alignedLog;

	public AcceptingPetriNetAlignmentCallbackImplNet(XLog xLog, IvMModel model, IvMEventClasses activityEventClasses,
			Quadruple<AcceptingPetriNet, TObjectIntMap<Transition>, TObjectIntMap<Transition>, TObjectIntMap<Transition>> p,
			IvMDecoratorI decorator) {
		this.decorator = decorator;
		assert model.isNet();

		this.xLog = xLog;
		this.model = model;
		this.activityEventClasses = activityEventClasses;

		this.activity2skipEnqueue = p.getB();
		this.activity2skipStart = p.getC();
		this.activity2node = p.getD();

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
				Pair<Move, Integer> p = getMove(xTrace, type, node, performanceEventClasses, eventIndex,
						previousModelNode, moveIndex, decorator);

				if (p != null) {
					Move move = p.getA();
					previousModelNode = p.getB();

					if (move != null) {
						iTrace.add(ETMAlignmentCallbackImpl.move2ivmMove(model, move, xTrace, eventIndex));
						moveIndex++;
					}

					if (move != null && (type == StepTypes.L || type == StepTypes.LMGOOD)) {
						eventIndex++;
					}
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
	private Pair<Move, Integer> getMove(XTrace trace, StepTypes type, Object node,
			IvMEventClasses performanceEventClasses, int event, int previousModelNode, int moveIndex,
			IvMDecoratorI decorator) {

		if (type == StepTypes.L) {
			//a log-move happened
			XEventClass performanceActivity = (XEventClass) node;
			XEventClass activity = PerformanceUtils.getActivity(performanceActivity, activityEventClasses);
			PerformanceTransition lifeCycleTransition = PerformanceUtils.getLifeCycleTransition(performanceActivity);

			//log move
			if (lifeCycleTransition == PerformanceTransition.complete) {
				//only log moves of complete events are interesting
				return Pair.of(new Move(model, Type.logMove, -1, -1, activity, performanceActivity, lifeCycleTransition,
						moveIndex, decorator), previousModelNode);
			} else {
				//log moves of other transitions are ignored
				return Pair.of(new Move(model, Type.ignoredLogMove, -1, -1, activity, performanceActivity,
						lifeCycleTransition, moveIndex, decorator), previousModelNode);
			}
		} else if (type == StepTypes.MINVI) {
			//synchronous move on invisible transition
			assert (node instanceof Transition);
			Transition performanceUnode = (Transition) node;
			int activityIndex = activity2node.get(performanceUnode);

			if (!activity2node.containsKey(performanceUnode)) {
				//we are only interested in nodes that have a link to the original model
				return null;
			}

			if (model.isTau(activityIndex)) {
				//model move on tau
				PerformanceTransition lifeCycleTransition = PerformanceTransition.complete;
				return Pair.of(new Move(model, Type.synchronousMove, -2, activityIndex, null, null, lifeCycleTransition,
						moveIndex, decorator), previousModelNode);
			} else {
				//model move on activity: that is, on a transition that skips an enqueue or start
				PerformanceTransition lifeCycleTransition;
				if (activity2skipEnqueue.containsKey(performanceUnode)) {
					lifeCycleTransition = PerformanceTransition.enqueue;
				} else if (activity2skipStart.containsKey(performanceUnode)) {
					lifeCycleTransition = PerformanceTransition.start;
				} else {
					throw new RuntimeException("alignment result not supported");
				}
				XEventClass activity = activityEventClasses.getByIdentity(model.getActivityName(activityIndex));
				XEventClass performanceActivity = performanceEventClasses
						.getByIdentity(activity.getId() + "+" + lifeCycleTransition);
				return Pair.of(new Move(model, Type.ignoredModelMove, previousModelNode, activityIndex, activity,
						performanceActivity, lifeCycleTransition, moveIndex, decorator), previousModelNode);
			}
		} else if (type == StepTypes.MREAL) {
			//model move
			assert (node instanceof Transition);
			Transition performanceUnode = (Transition) node;
			PerformanceTransition lifeCycleTransition = PerformanceUtils
					.getLifeCycleTransition(performanceUnode.getLabel());
			XEventClass performanceActivity = performanceEventClasses.getByIdentity(((Transition) node).getLabel());
			XEventClass activity = PerformanceUtils.getActivity(performanceActivity, activityEventClasses);
			int activityIndex = activity2node.get(performanceUnode);
			assert (activity != null);
			int newPreviousModelNode = lifeCycleTransition == PerformanceTransition.complete ? activityIndex
					: previousModelNode;
			return Pair.of(new Move(model, Type.modelMove, previousModelNode, activityIndex, null, null,
					lifeCycleTransition, moveIndex, decorator), newPreviousModelNode);
		} else if (type == StepTypes.LMGOOD) {
			assert (node instanceof Transition);
			Transition performanceUnode = (Transition) node;
			XEventClass performanceActivity = performanceEventClasses.getClassOf(trace.get(event));
			XEventClass activity = PerformanceUtils.getActivity(performanceActivity, activityEventClasses);
			int activityIndex = activity2node.get(performanceUnode);
			PerformanceTransition lifeCycleTransition = PerformanceUtils
					.getLifeCycleTransition(performanceUnode.getLabel());

			int newPreviousModelNode = lifeCycleTransition == PerformanceTransition.complete ? activityIndex
					: previousModelNode;
			return Pair.of(new Move(model, Type.synchronousMove, previousModelNode, activityIndex, activity,
					performanceActivity, lifeCycleTransition, moveIndex, decorator), newPreviousModelNode);
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
