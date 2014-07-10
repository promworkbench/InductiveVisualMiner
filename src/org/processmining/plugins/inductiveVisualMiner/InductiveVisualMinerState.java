package org.processmining.plugins.inductiveVisualMiner;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.model.XLog;
import org.processmining.plugins.InductiveMiner.mining.IMLog;
import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;
import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.AlignedLogVisualisationInfo;
import org.processmining.plugins.inductiveVisualMiner.alignment.AlignedLog;
import org.processmining.plugins.inductiveVisualMiner.alignment.AlignedLogInfo;
import org.processmining.plugins.inductiveVisualMiner.alignment.AlignmentResult;
import org.processmining.plugins.inductiveVisualMiner.animation.TimedLog;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.UnfoldedNode;

public class InductiveVisualMinerState {

	public InductiveVisualMinerState(XLog xLog) {
		this.xLog = xLog;
		miningParameters = new MiningParametersIvM();
	}

	//==log==
	private final XLog xLog;
	private XLogInfo xLogInfo;
	private IMLog imLog;
	private IMLogInfo imLogInfo;

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
	private IMLog activityFilteredIMLog;
	private IMLogInfo activityFilteredIMLogInfo;
	private Set<XEventClass> filteredActivities;

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
	private ProcessTree tree = null;

	public ProcessTree getTree() {
		return tree;
	}

	public MiningParameters getMiningParameters() {
		return miningParameters;
	}

	public synchronized void setTree(ProcessTree tree) {
		this.tree = tree;
	}

	public synchronized void setMiningParameters(MiningParameters miningParameters) {
		this.miningParameters = miningParameters;
	}

	//==layout==
	private Dot dot;
	private AlignedLogVisualisationInfo visualisationInfo;

	public void setLayout(Dot dot, AlignedLogVisualisationInfo visualisationInfo) {
		this.dot = dot;
		this.visualisationInfo = visualisationInfo;
	}

	public AlignedLogVisualisationInfo getVisualisationInfo() {
		return visualisationInfo;
	}

	public Dot getDot() {
		return dot;
	}

	//==alignment==
	private AlignedLog alignedLog = null;
	private AlignedLogInfo alignedLogInfo = null;
	private Map<UnfoldedNode, AlignedLogInfo> dfgLogInfos = null;
	private AlignedLog alignedFilteredLog = null;
	private AlignedLogInfo alignedFilteredLogInfo = null;
	private Map<UnfoldedNode, AlignedLogInfo> dfgFilteredLogInfos = null;

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

	public Map<UnfoldedNode, AlignedLogInfo> getDfgFilteredLogInfos() {
		return dfgFilteredLogInfos;
	}

	public Map<UnfoldedNode, AlignedLogInfo> getDfgLogInfos() {
		return dfgLogInfos;
	}

	/*
	 * Reset alignment to null
	 */
	public synchronized void resetAlignment() {
		this.alignedLog = null;
		this.alignedFilteredLog = null;
		this.alignedLogInfo = null;
		this.alignedFilteredLogInfo = null;
		this.dfgLogInfos = null;
		this.dfgFilteredLogInfos = null;
	}

	/*
	 * Finish alignment computation
	 */
	public synchronized void setAlignment(AlignmentResult alignment, Map<UnfoldedNode, AlignedLogInfo> dfgLogInfos) {
		this.alignedLog = alignment.log;
		this.alignedFilteredLog = alignment.log;
		this.alignedLogInfo = alignment.logInfo;
		this.alignedFilteredLogInfo = alignment.logInfo;
		this.dfgLogInfos = dfgLogInfos;
		this.dfgFilteredLogInfos = dfgLogInfos;
	}

	/*
	 * Apply a new filter
	 */
	public synchronized void setAlignedFilteredLog(AlignedLog alignedFilteredLog,
			AlignedLogInfo alignedFilteredLogInfo, Map<UnfoldedNode, AlignedLogInfo> dfgFilteredLogInfos) {
		this.alignedFilteredLog = alignedFilteredLog;
		this.alignedFilteredLogInfo = alignedFilteredLogInfo;
		this.dfgFilteredLogInfos = dfgFilteredLogInfos;
	}

	//==gui-parameters==
	public enum ColourMode {
		paths, deviations, both
	};

	private double activitiesThreshold = 1.0;
	private ColourMode colourMode = ColourMode.paths;

	public double getActivitiesThreshold() {
		return activitiesThreshold;
	}

	public ColourMode getColourMode() {
		return colourMode;
	}

	public synchronized void setActivitiesThreshold(double activitiesThreshold) {
		this.activitiesThreshold = activitiesThreshold;
	}

	public synchronized void setColourMode(ColourMode modus) {
		colourMode = modus;
	}

	//==node selection==
	private Set<UnfoldedNode> selectedNodes = new HashSet<UnfoldedNode>();

	public Set<UnfoldedNode> getSelectedNodes() {
		return selectedNodes;
	}

	public synchronized void setSelectedNodes(Set<UnfoldedNode> selectedNodes) {
		this.selectedNodes = selectedNodes;
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
