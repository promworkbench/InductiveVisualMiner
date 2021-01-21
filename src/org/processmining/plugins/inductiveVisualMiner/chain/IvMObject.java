package org.processmining.plugins.inductiveVisualMiner.chain;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.model.XLog;
import org.processmining.cohortanalysis.cohort.Cohorts;
import org.processmining.plugins.InductiveMiner.AttributeClassifiers.AttributeClassifier;
import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;
import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.visualisation.DotPanelUserSettings;
import org.processmining.plugins.inductiveVisualMiner.Selection;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.data.AlignedLogVisualisationData;
import org.processmining.plugins.inductiveVisualMiner.alignment.InductiveVisualMinerAlignment;
import org.processmining.plugins.inductiveVisualMiner.animation.GraphVizTokens;
import org.processmining.plugins.inductiveVisualMiner.animation.Scaler;
import org.processmining.plugins.inductiveVisualMiner.attributes.IvMAttributesInfo;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.eventattributes.EventAttributeAnalysis;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.logattributes.LogAttributeAnalysis;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.traceattributes.TraceAttributeAnalysis;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.IvMHighlightingFiltersController;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.IvMPreMiningFiltersController;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFilteredImpl;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogInfo;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogNotFiltered;
import org.processmining.plugins.inductiveVisualMiner.mode.Mode;
import org.processmining.plugins.inductiveVisualMiner.performance.PerformanceWrapper;
import org.processmining.plugins.inductiveVisualMiner.popup.HistogramData;
import org.processmining.plugins.inductiveVisualMiner.tracecolouring.TraceColourMap;
import org.processmining.plugins.inductiveVisualMiner.tracecolouring.TraceColourMapSettings;
import org.processmining.plugins.inductiveVisualMiner.visualMinerWrapper.VisualMinerWrapper;
import org.processmining.plugins.inductiveVisualMiner.visualisation.ProcessTreeVisualisationInfo;
import org.processmining.plugins.inductiveminer2.attributes.AttributesInfo;

import com.kitfox.svg.SVGDiagram;

public class IvMObject<C> {
	//inputs
	public static final IvMObject<XLog> input_log = c("input log", XLog.class);
	public static final IvMObject<Integer> histogram_width = c("histogram width", int.class);

	//computed objects
	public static final IvMObject<XLog> sorted_log = c("sorted log", XLog.class);
	public static final IvMObject<Boolean> log_timestamps_logical = c("log timestamps logical", boolean.class);
	public static final IvMObject<AttributesInfo> attributes_info = c("attributes info", AttributesInfo.class);
	public static final IvMObject<AttributeClassifier[]> classifiers = c("classifiers", AttributeClassifier[].class);
	public static final IvMObject<IvMModel> model = c("model", IvMModel.class);
	//	public static final IvMObject<TraceViewEventColourMap> trace_view_event_colour_map = c(
	//			"trace view event colour map", TraceViewEventColourMap.class);
	public static final IvMObject<TraceColourMap> trace_colour_map = c("trace colour map", TraceColourMap.class);
	public static final IvMObject<TraceColourMapSettings> trace_colour_map_settings = c("trace colour map settings",
			TraceColourMapSettings.class);
	public static final IvMObject<XLogInfo> xlog_info = c("xlog info", XLogInfo.class);
	public static final IvMObject<XLogInfo> xlog_info_performance = c("xlog info performance", XLogInfo.class);
	public static final IvMObject<IMLog> imlog = c("IM log", IMLog.class);
	public static final IvMObject<IMLogInfo> imlog_info = c("IM log info", IMLogInfo.class);
	public static final IvMObject<IMLog> imlog_activity_filtered = c("IM log activity filtered", IMLog.class);
	public static final IvMObject<IMLogInfo> imlog_info_activity_filtered = c("IM log activity filtered info",
			IMLogInfo.class);
	@SuppressWarnings("rawtypes")
	public static final IvMObject<Set> filtered_activities = c("filtered activities", Set.class);
	public static final IvMObject<AlignedLogVisualisationData> visualisation_data = c("visualisation data",
			AlignedLogVisualisationData.class);
	public static final IvMObject<IvMAttributesInfo> ivm_attributes_info = c("attributes info",
			IvMAttributesInfo.class);
	public static final IvMObject<HistogramData> histogram_data = c("histogram data", HistogramData.class);
	public static final IvMObject<AttributeClassifier[]> classifier_for_gui = c("classifier for gui",
			AttributeClassifier[].class);

	//popups
	@SuppressWarnings("rawtypes")
	public static final IvMObject<Map> popups = c("popups", Map.class);

	//alignments
	public static final IvMObject<IvMLogNotFiltered> aligned_log = c("aligned log", IvMLogNotFiltered.class);
	public static final IvMObject<IvMLogInfo> aligned_log_info = c("aligned log info", IvMLogInfo.class);
	public static final IvMObject<IvMLogFilteredImpl> aligned_log_filtered = c("aligned log filtered",
			IvMLogFilteredImpl.class);
	public static final IvMObject<IvMLogInfo> aligned_log_info_filtered = c("aligned log info filtered",
			IvMLogInfo.class);
	public static final IvMObject<InductiveVisualMinerAlignment> imported_alignment = c("imported alignment",
			InductiveVisualMinerAlignment.class);

	//performance
	public static final IvMObject<PerformanceWrapper> performance = c("performance", PerformanceWrapper.class);

	//data analyses
	public static final IvMObject<LogAttributeAnalysis> data_analysis_log = c("data analysis log",
			LogAttributeAnalysis.class);
	@SuppressWarnings("rawtypes")
	public static final IvMObject<List> data_analysis_log_virtual_attributes = c("data analysis log virtual attributes",
			List.class);
	public static final IvMObject<Cohorts> data_analysis_cohort = c("data analysis cohort", Cohorts.class);
	public static final IvMObject<EventAttributeAnalysis> data_analysis_event = c("data analysis event",
			EventAttributeAnalysis.class);
	public static final IvMObject<TraceAttributeAnalysis> data_analysis_trace = c("data analysis trace",
			TraceAttributeAnalysis.class);

	//graph objects
	public static final IvMObject<Dot> graph_dot = c("graph dot", Dot.class);
	public static final IvMObject<SVGDiagram> graph_svg = c("graph svg", SVGDiagram.class);
	public static final IvMObject<ProcessTreeVisualisationInfo> graph_visualisation_info = c("graph visualisation info",
			ProcessTreeVisualisationInfo.class);
	public static final IvMObject<Scaler> animation_scaler = c("animation scaler", Scaler.class);
	public static final IvMObject<GraphVizTokens> animation = c("animation", GraphVizTokens.class);

	//controllers & IvM-stuff
	public static final IvMObject<IvMHighlightingFiltersController> controller_highlighting_filters = c(
			"highlighting filters controller", IvMHighlightingFiltersController.class);
	public static final IvMObject<IvMPreMiningFiltersController> controller_premining_filters = c(
			"pre-mining filters controller", IvMPreMiningFiltersController.class);

	//user selections
	public static final IvMObject<Double> selected_activities_threshold = c("selected activities threshold",
			double.class);
	public static final IvMObject<Double> selected_noise_threshold = c("selected noise threshold", double.class);
	public static final IvMObject<Mode> selected_visualisation_mode = c("selected visualisation mode", Mode.class);
	public static final IvMObject<Selection> selected_model_selection = c("selected model selection", Selection.class);
	public static final IvMObject<DotPanelUserSettings> selected_graph_user_settings = c("selected graph user settings",
			DotPanelUserSettings.class);
	public static final IvMObject<Boolean> selected_animation_enabled = c("selected animation enabled", boolean.class);
	public static final IvMObject<VisualMinerWrapper> selected_miner = c("selected miner", VisualMinerWrapper.class);
	public static final IvMObject<AttributeClassifier[]> selected_classifier = c("selected classifier",
			AttributeClassifier[].class);
	public static final IvMObject<Boolean> selected_cohort_analysis_enabled = c("selected cohort analysis enabled",
			boolean.class);

	//skeleton
	protected static <C> IvMObject<C> c(String name, Class<C> clazz) {
		return new IvMObject<C>(name, clazz);
	}

	private final String name;
	private final Class<C> clazz;

	protected IvMObject(String name, Class<C> clazz) {
		this.name = name;
		this.clazz = clazz;
	}

	public String toString() {
		return name;
	}

	public String getName() {
		return name;
	}

	public Class<C> getClazz() {
		return clazz;
	}

	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((clazz == null) ? 0 : clazz.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		IvMObject<?> other = (IvMObject<?>) obj;
		if (clazz == null) {
			if (other.clazz != null) {
				return false;
			}
		} else if (!clazz.equals(other.clazz)) {
			return false;
		}
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		return true;
	}

}
