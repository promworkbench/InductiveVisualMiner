package org.processmining.plugins.inductiveVisualMiner;

import java.util.Set;
import java.util.prefs.Preferences;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.model.XLog;
import org.processmining.cohortanalysis.cohort.Cohorts;
import org.processmining.plugins.InductiveMiner.AttributeClassifiers.AttributeClassifier;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree;
import org.processmining.plugins.InductiveMiner.efficienttree.UnknownTreeNodeException;
import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;
import org.processmining.plugins.InductiveMiner.mining.MiningParametersIMf;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;
import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.visualisation.DotPanelUserSettings;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.data.AlignedLogVisualisationData;
import org.processmining.plugins.inductiveVisualMiner.alignment.InductiveVisualMinerAlignment;
import org.processmining.plugins.inductiveVisualMiner.alignment.LogMovePosition;
import org.processmining.plugins.inductiveVisualMiner.animation.GraphVizTokens;
import org.processmining.plugins.inductiveVisualMiner.animation.Scaler;
import org.processmining.plugins.inductiveVisualMiner.configuration.InductiveVisualMinerConfiguration;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.traceattributes.TraceAttributeAnalysis;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.IvMFiltersController;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFilteredImpl;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogInfo;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogNotFiltered;
import org.processmining.plugins.inductiveVisualMiner.mode.Mode;
import org.processmining.plugins.inductiveVisualMiner.mode.ModePaths;
import org.processmining.plugins.inductiveVisualMiner.performance.PerformanceWrapper;
import org.processmining.plugins.inductiveVisualMiner.performance.XEventPerformanceClassifier;
import org.processmining.plugins.inductiveVisualMiner.popup.HistogramData;
import org.processmining.plugins.inductiveVisualMiner.tracecolouring.TraceColourMap;
import org.processmining.plugins.inductiveVisualMiner.tracecolouring.TraceColourMapSettings;
import org.processmining.plugins.inductiveVisualMiner.traceview.TraceViewEventColourMap;
import org.processmining.plugins.inductiveVisualMiner.visualMinerWrapper.VisualMinerWrapper;
import org.processmining.plugins.inductiveVisualMiner.visualMinerWrapper.miners.Miner;
import org.processmining.plugins.inductiveVisualMiner.visualisation.ProcessTreeVisualisationInfo;
import org.processmining.plugins.inductiveminer2.attributes.AttributesInfo;

import com.kitfox.svg.SVGDiagram;

import gnu.trove.set.hash.THashSet;
import gnu.trove.set.hash.TIntHashSet;

public class InductiveVisualMinerState {

	public InductiveVisualMinerState(XLog xLog) throws UnknownTreeNodeException {
		this.xLog = xLog;
		miningParameters = new MiningParametersIMf();
	}

	/**
	 * Deprecated. Use the log-only constructor instead and call
	 * setPreMinedModel().
	 * 
	 * @param xLog
	 * @param preMinedTree
	 */
	@Deprecated
	public InductiveVisualMinerState(XLog xLog, EfficientTree preMinedTree) {
		this(xLog);
		setPreMinedModel(new IvMModel(preMinedTree));
	}

	//==configuration
	private InductiveVisualMinerConfiguration configuration;

	public void setConfiguration(InductiveVisualMinerConfiguration configuration) {
		this.configuration = configuration;
	}

	public InductiveVisualMinerConfiguration getConfiguration() {
		return configuration;
	}

	//==attributes==
	private AttributesInfo attributesInfo;
	private AttributeClassifier initialClassifier;
	private AttributeClassifier[] classifiers;

	public AttributesInfo getAttributesInfo() {
		return attributesInfo;
	}

	public AttributeClassifier getInitialClassifier() {
		return initialClassifier;
	}

	public AttributeClassifier[] getClassifiers() {
		return classifiers;
	}

	public void setAttributesInfo(AttributesInfo info, AttributeClassifier initialClassifier,
			AttributeClassifier[] classifiers) {
		attributesInfo = info;
		this.initialClassifier = initialClassifier;
		this.classifiers = classifiers;
	}

	//==log==
	private XEventPerformanceClassifier performanceClassifier = new XEventPerformanceClassifier(
			new XEventNameClassifier());
	private XEventPerformanceClassifier preMinedPerformanceClassifier;
	private XLog xLog;
	private XLog sortedXLog;
	private XLogInfo xLogInfo;
	private XLogInfo xLogInfoPerformance;
	private IMLog IMLog;
	private IMLogInfo IMLogInfo;
	private boolean illogicalTimeStamps = false;

	public boolean isIllogicalTimeStamps() {
		return illogicalTimeStamps;
	}

	public void setIllogicalTimeStamps(boolean illogicalTimeStamps) {
		this.illogicalTimeStamps = illogicalTimeStamps;
	}

	public XEventPerformanceClassifier getPerformanceClassifier() {
		if (preMinedPerformanceClassifier != null) {
			return preMinedPerformanceClassifier;
		}
		return performanceClassifier;
	}

	public XEventClassifier getActivityClassifier() {
		return getPerformanceClassifier().getActivityClassifier();
	}

	public synchronized void setClassifier(XEventClassifier classifier) {
		this.performanceClassifier = new XEventPerformanceClassifier(classifier);
	}

	public void setPreMinedClassifier(XEventClassifier preMinedClassifier) {
		this.preMinedPerformanceClassifier = new XEventPerformanceClassifier(preMinedClassifier);
	}

	public XEventPerformanceClassifier getPreMinedPerformanceClassifier() {
		return preMinedPerformanceClassifier;
	}

	public XLog getXLog() {
		return xLog;
	}

	public void setXLog(XLog xLog) {
		this.xLog = xLog;
	}

	public XLog getSortedXLog() {
		return sortedXLog;
	}

	public void setSortedXLog(XLog xLog) {
		this.sortedXLog = xLog;
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

	//==filters==
	private IvMFiltersController filtersController;

	public IvMFiltersController getFiltersController() {
		return filtersController;
	}

	public void setFiltersController(IvMFiltersController filtersController) {
		this.filtersController = filtersController;
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
	private VisualMinerWrapper miner = new Miner();
	private double paths = 0.8;
	private IvMModel model = null;
	private IvMModel preMinedModel = null;

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

	public IvMModel getModel() {
		return model;
	}

	public synchronized void setModel(IvMModel model) {
		this.model = model;
	}

	public IvMModel getPreMinedModel() {
		return preMinedModel;
	}

	public void setPreMinedModel(IvMModel model) {
		this.preMinedModel = model;
	}

	//==layout==
	private Dot dot;
	private SVGDiagram svgDiagram;
	private ProcessTreeVisualisationInfo visualisationInfo;
	private AlignedLogVisualisationData visualisationData;
	private DotPanelUserSettings graphUserSettings;
	private TraceViewEventColourMap traceViewColourMap;

	public void setLayout(Dot dot, SVGDiagram svgDiagram, ProcessTreeVisualisationInfo visualisationInfo,
			TraceViewEventColourMap traceViewColourMap) {
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

	public TraceViewEventColourMap getTraceViewColourMap() {
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

	//==timed log==
	private IvMLogNotFiltered ivmLog;
	private InductiveVisualMinerAlignment preMinedAlignment = null;
	private IvMLogInfo ivmLogInfo;
	private IvMLogFilteredImpl ivmLogFiltered;
	private IvMLogInfo ivmLogInfoFiltered;
	private TraceColourMapSettings traceColourMapSettings;
	private TraceColourMap traceColourMap;

	public void setIvMLog(IvMLogNotFiltered ivMLog, IvMLogInfo ivmLogInfo) {
		this.ivmLog = ivMLog;
		this.ivmLogInfo = ivmLogInfo;
		if (ivMLog != null) {
			this.ivmLogFiltered = new IvMLogFilteredImpl(ivMLog);
		} else {
			this.ivmLogFiltered = null;
		}
		this.ivmLogInfoFiltered = ivmLogInfo;
	}

	public IvMLogNotFiltered getIvMLog() {
		return ivmLog;
	}

	public InductiveVisualMinerAlignment getPreMinedIvMLog() {
		return preMinedAlignment;
	}

	public void setPreMinedAlignment(InductiveVisualMinerAlignment preMinedAlignment) {
		this.preMinedAlignment = preMinedAlignment;
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

	public TraceColourMapSettings getTraceColourMapSettings() {
		return traceColourMapSettings;
	}

	public void setTraceColourMapSettings(TraceColourMapSettings traceColourMapSettings) {
		this.traceColourMapSettings = traceColourMapSettings;
	}

	public TraceColourMap getTraceColourMap() {
		return traceColourMap;
	}

	public void setTraceColourMap(TraceColourMap traceColourMap) {
		this.traceColourMap = traceColourMap;
	}

	//==histogram==
	private HistogramData histogramData;
	private int histogramWidth;

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
	private static final Preferences preferences = Preferences.userRoot()
			.node("org.processmining.inductivevisualminer");
	public static final String playAnimationOnStartupKey = "playanimationonstartup";
	private boolean animationGlobalEnabled = preferences.getBoolean(playAnimationOnStartupKey, true);

	public void setAnimation(GraphVizTokens animationGraphVizTokens) {
		this.animationGraphVizTokens = animationGraphVizTokens;
	}

	public void setAnimationScaler(Scaler animationScaler) {
		this.animationScaler = animationScaler;
	}

	public Scaler getAnimationScaler() {
		return animationScaler;
	}

	public GraphVizTokens getAnimationGraphVizTokens() {
		return animationGraphVizTokens;
	}

	public boolean isAnimationGlobalEnabled() {
		return animationGlobalEnabled;
	}

	public void setAnimationGlobalEnabled(boolean animationEnabled) {
		this.animationGlobalEnabled = animationEnabled;
		preferences.putBoolean(playAnimationOnStartupKey, animationEnabled);
	}

	//==queue lengths
	private PerformanceWrapper performance;

	public void setPerformance(PerformanceWrapper performance) {
		this.performance = performance;
	}

	public PerformanceWrapper getPerformance() {
		return performance;
	}

	public boolean isPerformanceReady() {
		return isAlignmentReady() && performance != null;
	}

	//== data analysis
	private TraceAttributeAnalysis dataAnalysis;

	public void setTraceAttributesAnalysis(TraceAttributeAnalysis dataAnalysis) {
		this.dataAnalysis = dataAnalysis;
	}

	public TraceAttributeAnalysis getDataAnalysis() {
		return dataAnalysis;
	}

	//==cohort analysis
	private Cohorts cohortAnalysis;

	public void setCohortAnalysis(Cohorts result) {
		this.cohortAnalysis = result;
	}

	public Cohorts getCohortAnalysis() {
		return cohortAnalysis;
	}
}
