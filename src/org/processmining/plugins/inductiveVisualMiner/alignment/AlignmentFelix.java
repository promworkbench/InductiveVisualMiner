package org.processmining.plugins.inductiveVisualMiner.alignment;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.processmining.framework.plugin.Progress;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.petrinetwithdata.newImpl.PNWDTransition;
import org.processmining.models.graphbased.directed.petrinetwithdata.newImpl.PetriNetWithData;
import org.processmining.models.graphbased.directed.petrinetwithdata.newImpl.PetriNetWithDataFactory;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.DataConformance.Alignment;
import org.processmining.plugins.DataConformance.framework.ExecutionStep;
import org.processmining.plugins.DataConformance.visualization.DataAwareStepTypes;
import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.IMTraceG;
import org.processmining.plugins.balancedconformance.BalancedDataConformancePlusPlugin;
import org.processmining.plugins.balancedconformance.config.BalancedProcessorConfiguration;
import org.processmining.plugins.balancedconformance.controlflow.ControlFlowAlignmentException;
import org.processmining.plugins.balancedconformance.result.BalancedReplayResult;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.plugins.inductiveVisualMiner.alignment.AlignedLog;
import org.processmining.plugins.inductiveVisualMiner.alignment.AlignedLogInfo;
import org.processmining.plugins.inductiveVisualMiner.alignment.AlignmentResult;
import org.processmining.plugins.inductiveVisualMiner.alignment.Move;
import org.processmining.plugins.inductiveVisualMiner.alignment.Move.Type;
import org.processmining.plugins.petrinet.replayer.algorithms.costbasedcomplete.CostBasedCompleteParam;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.conversion.ProcessTree2Petrinet;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.InvalidProcessTreeException;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.NotYetImplementedException;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.PetrinetWithMarkings;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.UnfoldedNode;
import org.processmining.processtree.impl.AbstractTask;


public class AlignmentFelix {
	public static AlignmentResult alignTree(ProcessTree tree, XEventClassifier classifier, IMLogInfo logInfo, XLog log,
			XLogInfo xLogInfo, Set<XEventClass> skipActivities) {

		long start = System.nanoTime();

		//convert tree to Petri net
		PetrinetWithMarkings pnwm;
		try {
			pnwm = ProcessTree2Petrinet.convertKeepStructure(tree);
		} catch (NotYetImplementedException e1) {
			e1.printStackTrace();
			return null;
		} catch (InvalidProcessTreeException e1) {
			e1.printStackTrace();
			return null;
		}
		Petrinet petrinet = pnwm.petrinet;
		Marking initialMarking = pnwm.initialMarking;
		Marking finalMarking = pnwm.finalMarking;
		Map<Transition, UnfoldedNode> mapTransition2Path = pnwm.mapTransition2Path;

		PetriNetWithDataFactory factory = new PetriNetWithDataFactory(petrinet, "New version of " + petrinet.getLabel());
		PetriNetWithData petriNetWithData = factory.getRetValue();

		//convert transition mapping
		Map<PNWDTransition, UnfoldedNode> mapDataTransition2Path = new HashMap<PNWDTransition, ProcessTree2Petrinet.UnfoldedNode>();
		for (Transition t : factory.getTransMapping().keySet()) {
			mapDataTransition2Path.put((PNWDTransition) factory.getTransMapping().get(t), mapTransition2Path.get(t));
		}

		//create mapping transition -> eventclass
		XEventClass dummy = new XEventClass("", 1);
		TransEvClassMapping mapping = new TransEvClassMapping(classifier, dummy);
		for (Transition t : petriNetWithData.getTransitions()) {
			if (t.isInvisible()) {
				mapping.put(t, dummy);
			} else {
				mapping.put(t, mapTransitionToEventClass(t, logInfo, mapDataTransition2Path));
			}
		}
		Collection<XEventClass> activities = logInfo.getActivities().toSet();

		//create alignment shizzle
		Marking finalMarkingData = new Marking();
		for (Place p : finalMarking) {
			for (Place p2 : petriNetWithData.getPlaces()) {
				if (p2.getLabel().equals(p.getLabel())) {
					finalMarkingData.add(p2);
				}
			}
		}
		Marking initialMarkingData = new Marking();
		for (Place p : initialMarking) {
			for (Place p2 : petriNetWithData.getPlaces()) {
				if (p2.getLabel().equals(p.getLabel())) {
					initialMarkingData.add(p2);
				}
			}
		}
		BalancedDataConformancePlusPlugin bdcpp = new BalancedDataConformancePlusPlugin();
		BalancedProcessorConfiguration config = new BalancedProcessorConfiguration();
		config.setConcurrentThreads(Runtime.getRuntime().availableProcessors());
		CostBasedCompleteParam cbcp = new CostBasedCompleteParam(activities, dummy, petriNetWithData.getTransitions(),
				1, 1);
		cbcp.setFinalMarkings(finalMarkingData);
		cbcp.setInitialMarking(initialMarkingData);
		config.setCostBasedCompleteParam(cbcp);
		config.setActivityMapping(mapping);
		try {
			BalancedReplayResult repResult = bdcpp.doBalancedAlignmentDataConformanceChecking(petriNetWithData, log,
					new Progress() {

						public void setValue(int value) {
						}

						public void setMinimum(int value) {
						}

						public void setMaximum(int value) {
						}

						public void setIndeterminate(boolean makeIndeterminate) {
						}

						public void setCaption(String message) {
						}

						public boolean isIndeterminate() {
							return false;
						}

						public boolean isCancelled() {
							return false;
						}

						public void inc() {
						}

						public int getValue() {
							return 0;
						}

						public int getMinimum() {
							return 0;
						}

						public int getMaximum() {
							return 0;
						}

						public String getCaption() {
							return "";
						}

						public void cancel() {
						}
					}, config);

			//obtain the aligned log
			AlignedLog alignedLog = new AlignedLog();
			for (Alignment trace : repResult.labelStepArray) {
				Iterator<ExecutionStep> logIt = trace.getLogTrace().iterator();
				Iterator<ExecutionStep> modelIt = trace.getProcessTrace().iterator();
				Iterator<DataAwareStepTypes> typeIt = trace.getStepTypes().iterator();
				IMTraceG<Move> alignedTrace = new IMTraceG<Move>();

				//iterate over events
				while (logIt.hasNext()) {
					ExecutionStep logStep = logIt.next();
					ExecutionStep modelStep = modelIt.next();
					DataAwareStepTypes typeStep = typeIt.next();

					switch (typeStep) {
						case L : {
							//log move
							XEvent event = (XEvent) logStep.getActivityObject();
							XEventClass activity = null;
							if (event != null) {
								activity = xLogInfo.getEventClasses().getClassOf(event);
							}
							if (skipActivities.contains(activity)) {
								alignedTrace.add(new Move(Type.log, null, activity));
							}
							break;
						}
						case LMGOOD :
						case LMNOGOOD : {
							//synchronous, normal
							//TODO: Felix/Massimiliano must fix
							//							XEvent event = (XEvent) logStep.getActivityObject();
							//							XEventClass activity = null;
							//							if (event != null) {
							//								activity = xLogInfo.getEventClasses().getClassOf(event);
							//							}
							Transition model = (Transition) modelStep.getActivityObject();
							UnfoldedNode unode = mapDataTransition2Path.get(model);
							if (unode.getNode() instanceof AbstractTask) {
								alignedTrace.add(new Move(Type.synchronous, unode, null));
							}
							break;
						}
						case MINVI : {
							//synchronous, invisible
							Transition model = (Transition) modelStep.getActivityObject();
							UnfoldedNode unode = mapDataTransition2Path.get(model);
							if (unode.getNode() instanceof AbstractTask) {
								alignedTrace.add(new Move(Type.synchronous, unode, null));
							}
							break;
						}
						case MREAL : {
							//model move
							Transition model = (Transition) modelStep.getActivityObject();
							UnfoldedNode unode = mapDataTransition2Path.get(model);
							if (unode.getNode() instanceof AbstractTask) {
								alignedTrace.add(new Move(Type.model, unode, null));
							}
							break;
						}
						default :
							break;
					}
				}
				alignedLog.add(alignedTrace);
			}

			double fitness = repResult.meanFitness;

			AlignedLogInfo alignedLogInfo = new AlignedLogInfo(alignedLog);

			System.out.println("Felix alignment done, took " + String.format("%15d", System.nanoTime() - start) + ", fitness " + fitness);

			return new AlignmentResult(alignedLog, alignedLogInfo);
		} catch (ControlFlowAlignmentException e) {
			e.printStackTrace();
			return null;
		}
	}

	private static XEventClass mapTransitionToEventClass(Transition t, IMLogInfo logInfo,
			Map<PNWDTransition, UnfoldedNode> mapTransition2Path) {
		//find the event class with the same label as the transition
		for (XEventClass activity : logInfo.getActivities()) {
			UnfoldedNode unode = mapTransition2Path.get(t);
			if (unode.getNode().getName().equals(activity.toString())) {
				return activity;
			}
		}
		return null;
	}
}
