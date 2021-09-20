package org.processmining.plugins.inductiveVisualMiner.alignment;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.extension.std.XLifecycleExtension;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.plugins.InductiveMiner.Septuple;
import org.processmining.plugins.InductiveMiner.Triple;
import org.processmining.plugins.inductiveVisualMiner.alignment.Move.Type;
import org.processmining.plugins.inductiveVisualMiner.export.XModelAlignmentExtension;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMEfficientTree;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.ResourceTimeUtils;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.TreeUtils;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogNotFiltered;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogNotFilteredImpl;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMMove;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTraceImpl;
import org.processmining.plugins.inductiveVisualMiner.performance.PerformanceUtils;
import org.processmining.plugins.inductiveVisualMiner.performance.PerformanceTransition;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.UnfoldedNode;

import gnu.trove.map.TObjectIntMap;

public class ImportAlignment {

	public static IvMLogNotFiltered getIvMLog(InductiveVisualMinerAlignment alignment,
			XEventClasses activityEventClasses, XEventClasses performanceEventClasses) {
		XLog aLog = alignment.getAlignedLog();
		XLog xLog = alignment.getXLog(); //make sure that the XLog is already made, such that we can link to its attributes
		if (xLog == null) {
			return null;
		}
		IvMModel model = alignment.getModel(); //make sure that the model is available
		if (model == null) {
			return null;
		}

		//prepare the event classes
		//the event classes are not thread-safe; copy them
		IvMEventClasses activityEventClasses2 = new IvMEventClasses(activityEventClasses);
		IvMEventClasses performanceEventClasses2 = new IvMEventClasses(performanceEventClasses);
		if (model.isDfg()) {
			//make a Petri net to obtain the event classes from
			Septuple<AcceptingPetriNet, TObjectIntMap<Transition>, TObjectIntMap<Transition>, Set<Transition>, Set<Transition>, Set<Transition>, Transition> p = Dfm2AcceptingPetriNet
					.convertForPerformance(model.getDfg());

			AcceptingPetriNetAlignment.addAllLeavesAsEventClasses(activityEventClasses2, model.getDfg());
			AcceptingPetriNetAlignment.addAllLeavesAsPerformanceEventClasses(performanceEventClasses2, p.getA());
		} else if (model.isTree()) {
			Triple<ProcessTree, Map<UnfoldedNode, UnfoldedNode>, Set<UnfoldedNode>> t = ExpandProcessTree
					.expand(model.getTree().getDTree());
			IvMEfficientTree performanceTree = new IvMEfficientTree(t.getA());
			Map<UnfoldedNode, UnfoldedNode> performanceNodeMapping = t.getB(); //mapping performance node -> original node
			Set<UnfoldedNode> enqueueTaus = t.getC(); //set of taus involved in enqueueing

			//create mapping int->(performance)unfoldedNode
			List<UnfoldedNode> l = TreeUtils.unfoldAllNodes(new UnfoldedNode(performanceTree.getDTree().getRoot()));
			UnfoldedNode[] nodeId2performanceNode = l.toArray(new UnfoldedNode[l.size()]);

			ETMAlignment.addAllLeavesAsEventClasses(performanceEventClasses2, performanceTree.getDTree());
		} else {
			return null;
		}

		IvMLogNotFilteredImpl iLog = new IvMLogNotFilteredImpl(aLog.size(), xLog.getAttributes()); //i prefix: result IvM Log

		XModelAlignmentExtension alignmentExtension = XModelAlignmentExtension.instance();

		Iterator<XTrace> xTraceIt = xLog.iterator(); //x prefix: xLog (i.e. without alignment)
		int traceIndex = 0;
		for (XTrace aTrace : aLog) { //a prefix: aligned log (as the XES-aligned log, the input)
			String traceName = AcceptingPetriNetAlignmentCallbackImplDfg.getTraceName(aTrace);
			XTrace xTrace = xTraceIt.next();

			XAttributeMap xTraceAttributes = (XAttributeMap) xTrace.getAttributes().clone();
			IvMTrace iTrace = new IvMTraceImpl(traceName, xTraceAttributes, aTrace.size());

			int xEventIndex = 0;
			int aEventIndex = 0;
			for (XEvent aEvent : aTrace) {
				//depending on the type of move, we:
				//- set the type
				//- signal that the move is also present in the xTrace and increase the xEventIndex counter
				//- get performance event class
				//- get activity event class
				Type type;
				XEvent xEvent = null;
				XEventClass performanceEventClass = null;
				XEventClass activityEventClass = null;
				switch (alignmentExtension.extractMoveType(aEvent)) {
					case "ignoredLogMove" : {
						type = Type.ignoredLogMove;
						xEvent = xTrace.get(xEventIndex);
						performanceEventClass = performanceEventClasses2.getClassOf(aEvent);
						activityEventClass = PerformanceUtils.getActivity(performanceEventClass, activityEventClasses2);
						xEventIndex++;
						break;
					}
					case "ignoredModelMove" : {
						type = Type.ignoredModelMove;
						int node = alignmentExtension.extractMoveModelNode(aEvent);
						if (node == Integer.MIN_VALUE) {
							return null;
						}
						activityEventClass = activityEventClasses2.getByIdentity(model.getActivityName(node));
						break;
					}
					case "logMove" : {
						type = Type.logMove;
						xEvent = xTrace.get(xEventIndex);
						performanceEventClass = performanceEventClasses2.getClassOf(aEvent);
						activityEventClass = PerformanceUtils.getActivity(performanceEventClass, activityEventClasses2);
						xEventIndex++;
						break;
					}
					case "modelMove" : {
						type = Type.modelMove;
						int node = alignmentExtension.extractMoveModelNode(aEvent);
						if (node == Integer.MIN_VALUE) {
							return null;
						}
						activityEventClass = activityEventClasses2.getByIdentity(model.getActivityName(node));
						break;
					}
					case "synchronousMove" : {
						type = Type.synchronousMove;
						int node = alignmentExtension.extractMoveModelNode(aEvent);
						if (node == Integer.MIN_VALUE) {
							return null;
						}
						if (!model.isTau(node)) {
							xEvent = xTrace.get(xEventIndex);
							performanceEventClass = performanceEventClasses2.getClassOf(aEvent);
							activityEventClass = PerformanceUtils.getActivity(performanceEventClass, activityEventClasses2);
							xEventIndex++;
						} else {
							activityEventClass = null;
						}
						break;
					}
					default :
						return null;
				}

				//gather attributes for Move
				PerformanceTransition lifeCycleTransition = PerformanceUtils
						.getLifeCycleTransition("+" + XLifecycleExtension.instance().extractTransition(aEvent));

				int sourceNode = alignmentExtension.extractMoveSourceNode(aEvent);
				if (sourceNode == Integer.MIN_VALUE) {
					return null;
				}
				int node = alignmentExtension.extractMoveModelNode(aEvent);
				if (node == Integer.MIN_VALUE) {
					return null;
				}

				Move move = new Move(model, type, sourceNode, node, activityEventClass, performanceEventClass,
						lifeCycleTransition, aEventIndex);

				//gather attributes for IvMMove
				Long timestamp = ResourceTimeUtils.getTimestamp(aEvent);
				String resource = ResourceTimeUtils.getResource(aEvent);
				XAttributeMap attributes = xEvent == null ? null : xEvent.getAttributes();

				IvMMove iMove = new IvMMove(model, move, timestamp, resource, attributes);
				iTrace.add(iMove);

				aEventIndex++;
			}

			iLog.set(traceIndex, iTrace);
			traceIndex++;
		}

		return iLog;
	}
}
