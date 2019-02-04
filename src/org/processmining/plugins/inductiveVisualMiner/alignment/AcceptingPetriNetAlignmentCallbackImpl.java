package org.processmining.plugins.inductiveVisualMiner.alignment;

import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;

import org.apache.commons.lang.ArrayUtils;
import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.InductiveMiner.Septuple;
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

public class AcceptingPetriNetAlignmentCallbackImpl implements AcceptingPetriNetAlignmentCallback {

	//input
	private final XLog xLog;
	private final IvMModel model;
	private final IvMEventClasses activityEventClasses;

	private final TObjectIntMap<Transition> activity2skipEnqueue;
	private final TObjectIntMap<Transition> activity2skipStart;
	private final Set<Transition> startTransitions;
	private final Set<Transition> endTransitions;
	private final Set<Transition> interTransitions;
	private final Transition emptyTraceTransition;

	//output
	private final IvMLogNotFilteredImpl alignedLog;

	public AcceptingPetriNetAlignmentCallbackImpl(XLog xLog, IvMModel model, IvMEventClasses activityEventClasses,
			Septuple<AcceptingPetriNet, TObjectIntMap<Transition>, TObjectIntMap<Transition>, Set<Transition>, Set<Transition>, Set<Transition>, Transition> p) {
		this.xLog = xLog;
		this.model = model;
		this.activityEventClasses = activityEventClasses;

		this.activity2skipEnqueue = p.getB();
		this.activity2skipStart = p.getC();
		this.startTransitions = p.getD();
		this.endTransitions = p.getE();
		this.interTransitions = p.getF();
		this.emptyTraceTransition = p.getG();

		alignedLog = new IvMLogNotFilteredImpl(xLog.size());
	}

	public void traceAlignmentComplete(SyncReplayResult aTrace, SortedSet<Integer> xTraces,
			IvMEventClasses performanceEventClasses) {

		for (Integer traceIndex : xTraces) {
			XTrace xTrace = xLog.get(traceIndex);

			//get trace name
			String traceName;
			if (xTrace.getAttributes().containsKey("concept:name")) {
				traceName = xTrace.getAttributes().get("concept:name").toString();
			} else {
				traceName = "";
			}

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
						previousModelNode, moveIndex);

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
			IvMEventClasses performanceEventClasses, int event, int previousModelNode, int moveIndex) {

		//get log part of move
		if (type == StepTypes.L) {
			//a log-move happened
			XEventClass performanceActivity = (XEventClass) node;
			XEventClass activity = Performance.getActivity(performanceActivity, activityEventClasses);
			PerformanceTransition lifeCycleTransition = Performance.getLifeCycleTransition(performanceActivity);

			//log move
			if (lifeCycleTransition == PerformanceTransition.complete) {
				//only log moves of complete events are interesting
				return Pair.of(new Move(model, Type.logMove, -1, -1, activity, performanceActivity, lifeCycleTransition,
						moveIndex), previousModelNode);
			} else {
				//log moves of other transitions are ignored
				return Pair.of(new Move(model, Type.ignoredLogMove, -1, -1, activity, performanceActivity,
						lifeCycleTransition, moveIndex), previousModelNode);
			}
		} else if (type == StepTypes.MINVI) {
			//enqueue- or start-skip, or start-tau
			if (startTransitions.contains(node) || endTransitions.contains(node) || interTransitions.contains(node)) {
				//start-tau
				return null;
			} else {
				//enqueue- or start-skip
				assert (node instanceof Transition);
				Transition performanceUnode = (Transition) node;

				PerformanceTransition lifeCycleTransition;
				int activityIndex;
				if (node.equals(emptyTraceTransition)) {
					//empty trace
					return Pair.of(new Move(model, Type.ignoredModelMove, previousModelNode, -100, null, null,
							PerformanceTransition.complete, moveIndex), previousModelNode);
				} else if (activity2skipEnqueue.containsKey(performanceUnode)) {
					lifeCycleTransition = PerformanceTransition.enqueue;
					activityIndex = activity2skipEnqueue.get(performanceUnode);
				} else {
					lifeCycleTransition = PerformanceTransition.start;
					activityIndex = activity2skipStart.get(performanceUnode);
				}
				XEventClass activity = activityEventClasses
						.getByIdentity(model.getDfg().getActivityOfIndex(activityIndex));
				XEventClass performanceActivity = performanceEventClasses
						.getByIdentity(activity.getId() + "+" + lifeCycleTransition);
				return Pair.of(new Move(model, Type.ignoredModelMove, previousModelNode, activityIndex, activity,
						performanceActivity, lifeCycleTransition, moveIndex), previousModelNode);
			}
		} else if (type == StepTypes.MREAL) {
			//model move
			assert (node instanceof Transition);
			Transition performanceUnode = (Transition) node;
			PerformanceTransition lifeCycleTransition = Performance.getLifeCycleTransition(performanceUnode.getLabel());
			XEventClass performanceActivity = performanceEventClasses.getByIdentity(((Transition) node).getLabel());
			XEventClass activity = Performance.getActivity(performanceActivity, activityEventClasses);
			int activityIndex = ArrayUtils.indexOf(model.getDfg().getAllActivities(), activity.getId());
			assert (activity != null);
			int newPreviousModelNode = lifeCycleTransition == PerformanceTransition.complete ? activityIndex
					: previousModelNode;
			return Pair.of(new Move(model, Type.modelMove, previousModelNode, activityIndex, null, null,
					lifeCycleTransition, moveIndex), newPreviousModelNode);
		} else if (type == StepTypes.LMGOOD) {
			assert (node instanceof Transition);
			Transition performanceUnode = (Transition) node;
			XEventClass performanceActivity = performanceEventClasses.getClassOf(trace.get(event));
			XEventClass activity = Performance.getActivity(performanceActivity, activityEventClasses);
			int activityIndex = ArrayUtils.indexOf(model.getDfg().getAllActivities(), activity.getId());
			PerformanceTransition lifeCycleTransition = Performance.getLifeCycleTransition(performanceUnode.getLabel());

			int newPreviousModelNode = lifeCycleTransition == PerformanceTransition.complete ? activityIndex
					: previousModelNode;
			return Pair.of(new Move(model, Type.synchronousMove, previousModelNode, activityIndex, activity,
					performanceActivity, lifeCycleTransition, moveIndex), newPreviousModelNode);
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
