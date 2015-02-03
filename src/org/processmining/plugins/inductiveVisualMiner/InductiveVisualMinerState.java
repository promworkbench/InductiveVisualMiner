package org.processmining.plugins.inductiveVisualMiner;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.model.XLog;
import org.processmining.plugins.InductiveMiner.dfgOnly.log2logInfo.IMLog2IMLogInfo;
import org.processmining.plugins.InductiveMiner.dfgOnly.log2logInfo.IMLog2IMLogInfoDefault;
import org.processmining.plugins.InductiveMiner.mining.IMLog;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLogInfo;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.AlignedLogVisualisationInfo;
import org.processmining.plugins.inductiveVisualMiner.alignment.AlignedLog;
import org.processmining.plugins.inductiveVisualMiner.alignment.AlignedLogInfo;
import org.processmining.plugins.inductiveVisualMiner.alignment.AlignmentResult;
import org.processmining.plugins.inductiveVisualMiner.alignment.LogMovePosition;
import org.processmining.plugins.inductiveVisualMiner.animation.TimedLog;
import org.processmining.plugins.inductiveVisualMiner.colouringFilter.ColouringFilter;
import org.processmining.plugins.inductiveVisualMiner.visualMinerWrapper.VisualMinerWrapper;
import org.processmining.plugins.inductiveVisualMiner.visualMinerWrapper.miners.IMi;
import org.processmining.plugins.inductiveVisualMiner.visualMinerWrapper.miners.MiningParametersIvM;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.UnfoldedNode;

public class InductiveVisualMinerState {
	
	public InductiveVisualMinerState(XLog xLog, ProcessTree preMinedTree) {
		this.xLog = xLog;
		miningParameters = new MiningParametersIvM();
		if (preMinedTree != null) {
			this.tree = preMinedTree;
			this.preMinedTree = preMinedTree;
		}
	}

	//==log==
	private XEventClassifier classifier = new XEventNameClassifier();
	private IMLog2IMLogInfo log2logInfo = new IMLog2IMLogInfoDefault();
	private final XLog xLog;
	private XLogInfo xLogInfo;
	private IMLog imLog;
	private IMLogInfo imLogInfo;

	public XEventClassifier getClassifier() {
		return classifier;
	}
	
	public synchronized void setClassifier (XEventClassifier classifier) {
		this.classifier = classifier;
	}

	public IMLog2IMLogInfo getLog2logInfo() {
		return log2logInfo;
	}

	public void setLog2logInfo(IMLog2IMLogInfo log2logInfo) {
		this.log2logInfo = log2logInfo;
	}
	
	public XLog getXLog() {
		return xLog;
	}

	public XLogInfo getXLogInfo() {
		return xLogInfo;
	}

	public IMLog getLog() {
		return imLog;
	}

	public IMLogInfo getLogInfo() {
		return imLogInfo;
	}

	public synchronized void setLog(XLogInfo xLogInfo, IMLog imLog, IMLogInfo imLogInfo) {
		this.imLog = imLog;
		this.imLogInfo = imLogInfo;
		this.xLogInfo = xLogInfo;
	}

	//==activity-filtered log==
	private double activitiesThreshold = 1.0;
	private IMLog activityFilteredIMLog;
	private IMLogInfo activityFilteredIMLogInfo;
	private Set<XEventClass> filteredActivities;
	
	public double getActivitiesThreshold() {
		return activitiesThreshold;
	}
	
	public synchronized void setActivitiesThreshold(double activitiesThreshold) {
		this.activitiesThreshold = activitiesThreshold;
	}

	public IMLog getActivityFilteredIMLog() {
		return activityFilteredIMLog;
	}

	public IMLogInfo getActivityFilteredIMLogInfo() {
		return activityFilteredIMLogInfo;
	}

	public Set<XEventClass> getFilteredActivities() {
		return filteredActivities;
	}

	public synchronized void setActivityFilteredIMLog(IMLog activityFilteredIMLog, IMLogInfo activityFilteredIMLogInfo,
			Set<XEventClass> filteredActivities) {
		this.activityFilteredIMLog = activityFilteredIMLog;
		this.activityFilteredIMLogInfo = activityFilteredIMLogInfo;
		this.filteredActivities = filteredActivities;
	}

	//==mining==
	private MiningParameters miningParameters;
	private VisualMinerWrapper miner = new IMi();
	private double paths = 0.8;
	private ProcessTree tree = null;
	private ProcessTree preMinedTree = null;

	public MiningParameters getMiningParameters2() {
		return miningParameters;
	}
	
	public synchronized void setMiningParameters(MiningParameters miningParameters) {
		this.miningParameters = miningParameters;
	}
	
	public VisualMinerWrapper getMiner() {
		return miner;
	}
	
	public void setMiner(VisualMinerWrapper miner) {
		this.miner = miner;
	}
	
	public double getPaths() {
		return paths;
	}

	public synchronized void setPaths(double paths) {
		this.paths = paths;
	}
	
	public ProcessTree getTree() {
		return tree;
	}
	
	public synchronized void setTree(ProcessTree tree) {
		this.tree = tree;
	}
	
	public ProcessTree getPreMinedTree() {
		return preMinedTree;
	}

	//==layout==
	private AlignedLogVisualisationInfo visualisationInfo;

	public void setLayout(AlignedLogVisualisationInfo visualisationInfo) {
		this.visualisationInfo = visualisationInfo;
	}

	public AlignedLogVisualisationInfo getVisualisationInfo() {
		return visualisationInfo;
	}

	//==alignment==
	private AlignedLog alignedLog = null;
	private AlignedLogInfo alignedLogInfo = null;
	private AlignedLog alignedFilteredLog = null;
	private AlignedLogInfo alignedFilteredLogInfo = null;
	private IMLog alignedFilteredXLog = null;

	public boolean isAlignmentReady() {
		return alignedLog != null;
	}

	public AlignedLog getAlignedLog() {
		return alignedLog;
	}

	public AlignedLogInfo getAlignedLogInfo() {
		return alignedLogInfo;
	}

	public AlignedLog getAlignedFilteredLog() {
		return alignedFilteredLog;
	}

	public AlignedLogInfo getAlignedFilteredLogInfo() {
		return alignedFilteredLogInfo;
	}
	
	public IMLog getAlignedFilteredXLog() {
		return alignedFilteredXLog;
	}

	/*
	 * Reset alignment to null
	 */
	public synchronized void resetAlignment() {
		this.alignedLog = null;
		this.alignedFilteredLog = null;
		this.alignedLogInfo = null;
		this.alignedFilteredLogInfo = null;
	}

	/*
	 * Finish alignment computation
	 */
	public synchronized void setAlignment(AlignmentResult alignment) {
		this.alignedLog = alignment.log;
		this.alignedFilteredLog = alignment.log;
		this.alignedLogInfo = alignment.logInfo;
		this.alignedFilteredLogInfo = alignment.logInfo;
	}

	/*
	 * Apply a new filter
	 */
	public synchronized void setAlignedFilteredLog(AlignedLog alignedFilteredLog,
			AlignedLogInfo alignedFilteredLogInfo, IMLog alignedFilteredXLog) {
		this.alignedFilteredLog = alignedFilteredLog;
		this.alignedFilteredLogInfo = alignedFilteredLogInfo;
		this.alignedFilteredXLog = alignedFilteredXLog;
	}

	//==gui-parameters==
	private ColourMode colourMode = ColourMode.paths;
	
	public enum ColourMode {
		paths, deviations, both
	};
	
	public ColourMode getColourMode() {
		return colourMode;
	}

	public synchronized void setColourMode(ColourMode modus) {
		colourMode = modus;
	}

	//==colour filtering ( & node selection)==
	private Set<UnfoldedNode> selectedNodes = new HashSet<>();
	private Set<LogMovePosition> selectedLogMoves = new HashSet<>();
	private List<ColouringFilter> colouringFilters;

	public Set<UnfoldedNode> getSelectedNodes() {
		return selectedNodes;
	}

	public synchronized void setSelectedNodes(Set<UnfoldedNode> selectedNodes) {
		this.selectedNodes = selectedNodes;
	}
	
	public Set<LogMovePosition> getSelectedLogMoves() {
		return selectedLogMoves;
	}
	
	public synchronized void setSelectedLogMoves(Set<LogMovePosition> selectedLogMoves) {
		this.selectedLogMoves = selectedLogMoves;
	}

	public List<ColouringFilter> getColouringFilters() {
		return colouringFilters;
	}

	public void setColouringFilters(List<ColouringFilter> colouringFilters) {
		this.colouringFilters = colouringFilters;
	}

	//==timed log==
	private TimedLog timedLog;

	public void setTimedLog(TimedLog timedLog) {
		this.timedLog = timedLog;
	}

	public TimedLog getTimedLog() {
		return timedLog;
	}
}
