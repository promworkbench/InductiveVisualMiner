package org.processmining.plugins.inductiveVisualMiner;

import gnu.trove.set.hash.THashSet;
import gnu.trove.set.hash.TIntHashSet;

import java.util.List;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.model.XLog;
import org.processmining.plugins.InductiveMiner.dfgOnly.log2logInfo.IMLog2IMLogInfo;
import org.processmining.plugins.InductiveMiner.dfgOnly.log2logInfo.IMLog2IMLogInfoDefault;
import org.processmining.plugins.InductiveMiner.efficienttree.UnknownTreeNodeException;
import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;
import org.processmining.plugins.InductiveMiner.mining.MiningParametersThesisIM;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;
import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.visualisation.DotPanelUserSettings;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.data.AlignedLogVisualisationData;
import org.processmining.plugins.inductiveVisualMiner.alignment.LogMovePosition;
import org.processmining.plugins.inductiveVisualMiner.animation.GraphVizTokens;
import org.processmining.plugins.inductiveVisualMiner.animation.Scaler;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMEfficientTree;
import org.processmining.plugins.inductiveVisualMiner.histogram.HistogramData;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.IvMFilter;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFilteredImpl;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogInfo;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogNotFiltered;
import org.processmining.plugins.inductiveVisualMiner.mode.Mode;
import org.processmining.plugins.inductiveVisualMiner.mode.ModePaths;
import org.processmining.plugins.inductiveVisualMiner.performance.PerformanceWrapper;
import org.processmining.plugins.inductiveVisualMiner.performance.XEventPerformanceClassifier;
import org.processmining.plugins.inductiveVisualMiner.traceview.TraceViewColourMap;
import org.processmining.plugins.inductiveVisualMiner.visualMinerWrapper.VisualMinerWrapper;
import org.processmining.plugins.inductiveVisualMiner.visualMinerWrapper.miners.NoLifeCycleSplitLog;
import org.processmining.plugins.inductiveVisualMiner.visualisation.ProcessTreeVisualisationInfo;
import org.processmining.processtree.ProcessTree;

import com.kitfox.svg.SVGDiagram;

public class InductiveVisualMinerState {

	public InductiveVisualMinerState(XLog xLog, ProcessTree preMinedTree) throws UnknownTreeNodeException {
		this.xLog = xLog;
		//miningParameters = new NoLifeCycleMiningParametersIvM();
		miningParameters = new MiningParametersThesisIM();
		if (preMinedTree != null) {
			this.tree = new IvMEfficientTree(preMinedTree);
			this.preMinedTree = this.tree;
		}
	}

	//==log==
	private XEventPerformanceClassifier performanceClassifier = new XEventPerformanceClassifier(
			new XEventNameClassifier());
	private IMLog2IMLogInfo log2logInfo = new IMLog2IMLogInfoDefault();
	private final XLog xLog;
	private XLogInfo xLogInfo;
	private XLogInfo xLogInfoPerformance;
	private IMLog IMLog;
	private IMLogInfo IMLogInfo;

	public XEventPerformanceClassifier getPerformanceClassifier() {
		return performanceClassifier;
	}

	public XEventClassifier getActivityClassifier() {
		return performanceClassifier.getActivityClassifier();
	}

	public synchronized void setClassifier(XEventClassifier classifier) {
		this.performanceClassifier = new XEventPerformanceClassifier(classifier);
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

	public XLogInfo getXLogInfoPerformance() {
		return xLogInfoPerformance;
	}

	public IMLog getLog() {
		return IMLog;
	}

	public IMLogInfo getLogInfo() {
		return IMLogInfo;
	}

	public synchronized void setLog(XLogInfo xLogInfo, XLogInfo xLogInfoPerformance, IMLog IMLog, IMLogInfo IMLogInfo) {
		this.IMLog = IMLog;
		this.IMLogInfo = IMLogInfo;
		this.xLogInfo = xLogInfo;
		this.xLogInfoPerformance = xLogInfoPerformance;
	}

	//==activity-filtered log==
	private double activitiesThreshold = 1.0;
	private IMLog activityFilteredIMLog;
	private IMLogInfo activityFilteredIMLogInfo;
	private Set<XEventClass> filteredActivities;
	private List<IvMFilter> preMiningFilters;

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
	
	public List<IvMFilter> getPreMiningFilters() {
		return preMiningFilters;
	}

	public void setPreMiningFilters(List<IvMFilter> preMiningFilters) {
		this.preMiningFilters = preMiningFilters;
	}

	//==mining==
	private MiningParameters miningParameters;
	private VisualMinerWrapper miner = new NoLifeCycleSplitLog();
	private double paths = 0.8;
	private IvMEfficientTree tree = null;
	private IvMEfficientTree preMinedTree = null;

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

	public IvMEfficientTree getTree() {
		return tree;
	}

	public synchronized void setTree(IvMEfficientTree tree) {
		this.tree = tree;
	}

	public IvMEfficientTree getPreMinedTree() {
		return preMinedTree;
	}

	//==layout==
	private Dot dot;
	private SVGDiagram svgDiagram;
	private ProcessTreeVisualisationInfo visualisationInfo;
	private AlignedLogVisualisationData visualisationData;
	private DotPanelUserSettings graphUserSettings;
	private TraceViewColourMap traceViewColourMap;

	public void setLayout(Dot dot, SVGDiagram svgDiagram, ProcessTreeVisualisationInfo visualisationInfo,
			TraceViewColourMap traceViewColourMap) {
		this.dot = dot;
		this.svgDiagram = svgDiagram;
		this.visualisationInfo = visualisationInfo;
		this.traceViewColourMap = traceViewColourMap;
	}

	public Dot getDot() {
		return dot;
	}

	public SVGDiagram getSVGDiagram() {
		return svgDiagram;
	}

	public ProcessTreeVisualisationInfo getVisualisationInfo() {
		return visualisationInfo;
	}

	public DotPanelUserSettings getGraphUserSettings() {
		return graphUserSettings;
	}

	public void setGraphUserSettings(DotPanelUserSettings graphUserSettings) {
		this.graphUserSettings = graphUserSettings;
	}

	public TraceViewColourMap getTraceViewColourMap() {
		return traceViewColourMap;
	}

	public void setVisualisationData(AlignedLogVisualisationData visualisationData) {
		this.visualisationData = visualisationData;
	}

	public AlignedLogVisualisationData getVisualisationData() {
		return visualisationData;
	}

	//==gui-parameters==
	private Mode mode = new ModePaths();

	public Mode getMode() {
		return mode;
	}

	public synchronized void setMode(Mode mode) {
		this.mode = mode;
	}

	//==colour filtering ( & node selection)==
	private Selection selection;
	private List<IvMFilter> colouringFilters;

	public Selection getSelection() {
		return selection;
	}

	public void setSelection(Selection selection) {
		this.selection = selection;
	}

	public void removeModelAndLogMovesSelection() {
		selection = new Selection(selection.getSelectedActivities(), new THashSet<LogMovePosition>(),
				new TIntHashSet(10, 0.5f, -1), selection.getSelectedTaus());
	}

	public List<IvMFilter> getColouringFilters() {
		return colouringFilters;
	}

	public void setColouringFilters(List<IvMFilter> colouringFilters2) {
		this.colouringFilters = colouringFilters2;
	}

	//==timed log==
	private IvMLogNotFiltered ivmLog;
	private IvMLogInfo ivmLogInfo;
	private IvMLogFilteredImpl ivmLogFiltered;
	private IvMLogInfo ivmLogInfoFiltered;

	public void setIvMLog(IvMLogNotFiltered ivMLog, IvMLogInfo ivmLogInfo) {
		this.ivmLog = ivMLog;
		this.ivmLogInfo = ivmLogInfo;
		this.ivmLogFiltered = new IvMLogFilteredImpl(ivMLog);
		this.ivmLogInfoFiltered = ivmLogInfo;
	}

	public IvMLogNotFiltered getIvMLog() {
		return ivmLog;
	}

	public void setIvMLogFiltered(IvMLogFilteredImpl ivmLogFiltered, IvMLogInfo ivmLogInfoFiltered) {
		this.ivmLogFiltered = ivmLogFiltered;
		this.ivmLogInfoFiltered = ivmLogInfoFiltered;
	}

	public IvMLogInfo getIvMLogInfo() {
		return ivmLogInfo;
	}

	public IvMLogFilteredImpl getIvMLogFiltered() {
		return ivmLogFiltered;
	}

	public IvMLogInfo getIvMLogInfoFiltered() {
		return ivmLogInfoFiltered;
	}

	public boolean isAlignmentReady() {
		return ivmLog != null;
	}

	/*
	 * Reset alignment to null
	 */
	public synchronized void resetAlignment() {
		ivmLog = null;
		ivmLogFiltered = null;
		ivmLogInfoFiltered = null;
	}

	//==histogram==
	private HistogramData histogramData;
	private int histogramWidth;

	public void resetHistogramData() {
		histogramData = null;
	}

	public void setHistogramData(HistogramData histogramData) {
		this.histogramData = histogramData;
	}

	public HistogramData getHistogramData() {
		return histogramData;
	}

	public void setHistogramWidth(int histogramWidth) {
		this.histogramWidth = histogramWidth;
	}

	public int getHistogramWidth() {
		return histogramWidth;
	}

	//==playing animation
	private Scaler animationScaler;
	private GraphVizTokens animationGraphVizTokens;

	public void setAnimation(GraphVizTokens animationGraphVizTokens) {
		this.animationGraphVizTokens = animationGraphVizTokens;
	}

	public void setAnimationScaler(Scaler animationScaler) {
		this.animationScaler = animationScaler;
	}

	public void resetAnimation() {
		animationScaler = null;
		animationGraphVizTokens = null;
	}

	public Scaler getAnimationScaler() {
		return animationScaler;
	}

	public GraphVizTokens getAnimationGraphVizTokens() {
		return animationGraphVizTokens;
	}

	//==queue lengths
	private PerformanceWrapper performance;

	public void setPerformance(PerformanceWrapper performance) {
		this.performance = performance;
	}

	public PerformanceWrapper getPerformance() {
		return performance;
	}

	public void resetPerformance() {
		performance = null;
	}

	public boolean isPerformanceReady() {
		return performance != null;
	}
}
