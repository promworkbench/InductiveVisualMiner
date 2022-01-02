package org.processmining.plugins.inductiveVisualMiner.configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;

import javax.swing.JOptionPane;

import org.processmining.cohortanalysis.cohort.Cohort;
import org.processmining.framework.plugin.ProMCanceller;
import org.processmining.plugins.InductiveMiner.Function;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerPanel;
import org.processmining.plugins.inductiveVisualMiner.alignment.AlignmentComputer;
import org.processmining.plugins.inductiveVisualMiner.alignment.AlignmentComputerImpl;
import org.processmining.plugins.inductiveVisualMiner.attributes.IvMVirtualAttributeFactory;
import org.processmining.plugins.inductiveVisualMiner.attributes.VirtualAttributeTraceDistinctEventAttribute;
import org.processmining.plugins.inductiveVisualMiner.attributes.VirtualAttributeTraceDuration;
import org.processmining.plugins.inductiveVisualMiner.attributes.VirtualAttributeTraceFitness;
import org.processmining.plugins.inductiveVisualMiner.attributes.VirtualAttributeTraceHasDeviations;
import org.processmining.plugins.inductiveVisualMiner.attributes.VirtualAttributeTraceLength;
import org.processmining.plugins.inductiveVisualMiner.attributes.VirtualAttributeTraceNumberOfCompleteEvents;
import org.processmining.plugins.inductiveVisualMiner.attributes.VirtualAttributeTraceNumberOfLogMoves;
import org.processmining.plugins.inductiveVisualMiner.attributes.VirtualAttributeTraceNumberOfModelMoves;
import org.processmining.plugins.inductiveVisualMiner.attributes.VirtualAttributeTraceSumEventAttribute;
import org.processmining.plugins.inductiveVisualMiner.chain.Cl01GatherAttributes;
import org.processmining.plugins.inductiveVisualMiner.chain.Cl02SortEvents;
import org.processmining.plugins.inductiveVisualMiner.chain.Cl03MakeLog;
import org.processmining.plugins.inductiveVisualMiner.chain.Cl04FilterLogOnActivities;
import org.processmining.plugins.inductiveVisualMiner.chain.Cl05Mine;
import org.processmining.plugins.inductiveVisualMiner.chain.Cl06LayoutModel;
import org.processmining.plugins.inductiveVisualMiner.chain.Cl07Align;
import org.processmining.plugins.inductiveVisualMiner.chain.Cl08UpdateIvMAttributes;
import org.processmining.plugins.inductiveVisualMiner.chain.Cl09LayoutAlignment;
import org.processmining.plugins.inductiveVisualMiner.chain.Cl10AnimationScaler;
import org.processmining.plugins.inductiveVisualMiner.chain.Cl11Animate;
import org.processmining.plugins.inductiveVisualMiner.chain.Cl12TraceColouring;
import org.processmining.plugins.inductiveVisualMiner.chain.Cl13FilterNodeSelection;
import org.processmining.plugins.inductiveVisualMiner.chain.Cl14Performance;
import org.processmining.plugins.inductiveVisualMiner.chain.Cl14PerformanceNegative;
import org.processmining.plugins.inductiveVisualMiner.chain.Cl15Histogram;
import org.processmining.plugins.inductiveVisualMiner.chain.Cl18DataAnalysisCohort;
import org.processmining.plugins.inductiveVisualMiner.chain.Cl19DataAnalysisCost;
import org.processmining.plugins.inductiveVisualMiner.chain.Cl20DataAnalysisAssociations;
import org.processmining.plugins.inductiveVisualMiner.chain.Cl21AdvancedAnalysesDelay;
import org.processmining.plugins.inductiveVisualMiner.chain.Cl22AdvancedAnalysisCausal;
import org.processmining.plugins.inductiveVisualMiner.chain.DataChain;
import org.processmining.plugins.inductiveVisualMiner.chain.DataChainImplNonBlocking;
import org.processmining.plugins.inductiveVisualMiner.chain.DataState;
import org.processmining.plugins.inductiveVisualMiner.cost.CostModelFactory;
import org.processmining.plugins.inductiveVisualMiner.cost.CostModelFactoryImplModelDeviationsLP;
import org.processmining.plugins.inductiveVisualMiner.cost.CostModelFactoryImplModelDeviationsServiceLP;
import org.processmining.plugins.inductiveVisualMiner.cost.CostModelFactoryImplModelDeviationsTimeLP;
import org.processmining.plugins.inductiveVisualMiner.cost.CostModelFactoryImplModelLP;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataAnalysisTab;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataRowBlock;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataRowBlockComputer;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.associations.AssociationsRowBlock;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.associations.AssociationsRowBlockProcess;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.associations.DataAnalysisTabAssociations;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.cohorts.CohortDataAnalysisTab;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.cohorts.CohortRowBlock;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.cost.CostDataTab;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.cost.CostRowBlock;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.eventattributes.EventDataRowBlock;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.eventattributes.EventDataRowBlockHistogram;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.eventattributes.EventDataRowBlockHistogramVirtual;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.eventattributes.EventDataRowBlockType;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.eventattributes.EventDataRowBlockTypeVirtual;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.eventattributes.EventDataRowBlockVirtual;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.eventattributes.EventDataTab;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.logattributes.DataRowBlockLogAttributes;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.logattributes.DataRowBlockLogAttributesHighlighted;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.logattributes.DataRowBlockLogEMSC;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.logattributes.DataTabLog;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.modeltime.DataAnalysisTabModelTime;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.modeltime.RowBlockModelHistogram;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.modeltime.RowBlockModelLogNormal;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.modeltime.RowBlockModelPerformance;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.modeltime.RowBlockModelWeibull;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.traceattributes.TraceDataRowBlock;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.traceattributes.TraceDataRowBlockHistogram;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.traceattributes.TraceDataRowBlockHistogramVirtual;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.traceattributes.TraceDataRowBlockType;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.traceattributes.TraceDataRowBlockTypeVirtual;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.traceattributes.TraceDataRowBlockVirtual;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.traceattributes.TraceDataTab;
import org.processmining.plugins.inductiveVisualMiner.export.ExporterAvi;
import org.processmining.plugins.inductiveVisualMiner.export.ExporterDataAnalyses;
import org.processmining.plugins.inductiveVisualMiner.export.ExporterModelStatistics;
import org.processmining.plugins.inductiveVisualMiner.export.ExporterTraceData;
import org.processmining.plugins.inductiveVisualMiner.export.IvMExporter;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.decoration.IvMDecoratorDefault;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.decoration.IvMDecoratorI;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.highlightingfilter.HighlightingFilter;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.highlightingfilter.filters.HighlightingFilterCohort;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.highlightingfilter.filters.HighlightingFilterCompleteEventTwice;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.highlightingfilter.filters.HighlightingFilterEvent;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.highlightingfilter.filters.HighlightingFilterEventTwice;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.highlightingfilter.filters.HighlightingFilterFollows;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.highlightingfilter.filters.HighlightingFilterLogMove;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.highlightingfilter.filters.HighlightingFilterTraceAttribute;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.highlightingfilter.filters.HighlightingFilterTraceEndsWithEvent;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.highlightingfilter.filters.HighlightingFilterTraceStartsWithEvent;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.highlightingfilter.filters.HighlightingFilterWithoutEvent;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.preminingfilters.PreMiningFilter;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.preminingfilters.filters.PreMiningFilterEvent;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.preminingfilters.filters.PreMiningFilterTrace;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.preminingfilters.filters.PreMiningFilterTraceWithEvent;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.preminingfilters.filters.PreMiningFilterTraceWithEventTwice;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.IvMFilterBuilder;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.IvMFilterBuilderFactory;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.filters.FilterIvMMoveAnd;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.filters.FilterIvMMoveAny;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.filters.FilterIvMMoveAttribute;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.filters.FilterIvMMoveOr;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.filters.FilterIvMTraceAnd;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.filters.FilterIvMTraceAny;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.filters.FilterIvMTraceAttribute;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.filters.FilterIvMTraceEndsWithEvent;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.filters.FilterIvMTraceFollows;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.filters.FilterIvMTraceOr;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.filters.FilterIvMTraceStartsWithEvent;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.filters.FilterIvMTraceWithEvent;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.filters.FilterIvMTraceWithEventTwice;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.filters.FilterIvMTraceWithoutEvent;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMMove;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;
import org.processmining.plugins.inductiveVisualMiner.mode.Mode;
import org.processmining.plugins.inductiveVisualMiner.mode.ModeCost;
import org.processmining.plugins.inductiveVisualMiner.mode.ModePaths;
import org.processmining.plugins.inductiveVisualMiner.mode.ModePathsDeviations;
import org.processmining.plugins.inductiveVisualMiner.mode.ModePathsQueueLengths;
import org.processmining.plugins.inductiveVisualMiner.mode.ModePathsService;
import org.processmining.plugins.inductiveVisualMiner.mode.ModePathsSojourn;
import org.processmining.plugins.inductiveVisualMiner.mode.ModePathsWaiting;
import org.processmining.plugins.inductiveVisualMiner.mode.ModeRelativePaths;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemActivity;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemLog;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemLogMove;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemModelMove;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemStartEnd;
import org.processmining.plugins.inductiveVisualMiner.popup.items.PopupItemActivityCost;
import org.processmining.plugins.inductiveVisualMiner.popup.items.PopupItemActivityName;
import org.processmining.plugins.inductiveVisualMiner.popup.items.PopupItemActivityOccurrences;
import org.processmining.plugins.inductiveVisualMiner.popup.items.PopupItemActivityOccurrencesPerTrace;
import org.processmining.plugins.inductiveVisualMiner.popup.items.PopupItemActivityPerformance;
import org.processmining.plugins.inductiveVisualMiner.popup.items.PopupItemActivitySpacer;
import org.processmining.plugins.inductiveVisualMiner.popup.items.PopupItemLogMoveActivities;
import org.processmining.plugins.inductiveVisualMiner.popup.items.PopupItemLogMoveSpacer;
import org.processmining.plugins.inductiveVisualMiner.popup.items.PopupItemLogMoveTitle;
import org.processmining.plugins.inductiveVisualMiner.popup.items.PopupItemLogName;
import org.processmining.plugins.inductiveVisualMiner.popup.items.PopupItemLogSpacer;
import org.processmining.plugins.inductiveVisualMiner.popup.items.PopupItemLogTitle;
import org.processmining.plugins.inductiveVisualMiner.popup.items.PopupItemModelMoveOccurrences;
import org.processmining.plugins.inductiveVisualMiner.popup.items.PopupItemStartEndName;
import org.processmining.plugins.inductiveVisualMiner.popup.items.PopupItemStartEndNumberOfTraces;
import org.processmining.plugins.inductiveVisualMiner.popup.items.PopupItemStartEndPerformance;
import org.processmining.plugins.inductiveVisualMiner.popup.items.PopupItemStartEndSpacer;
import org.processmining.plugins.inductiveVisualMiner.visualMinerWrapper.VisualMinerWrapper;
import org.processmining.plugins.inductiveVisualMiner.visualMinerWrapper.miners.AllOperatorsMiner;
import org.processmining.plugins.inductiveVisualMiner.visualMinerWrapper.miners.DfgMiner;
import org.processmining.plugins.inductiveVisualMiner.visualMinerWrapper.miners.LifeCycleMiner;
import org.processmining.plugins.inductiveVisualMiner.visualMinerWrapper.miners.Miner;
import org.processmining.plugins.inductiveminer2.attributes.AttributeImpl;
import org.processmining.plugins.inductiveminer2.attributes.AttributeVirtual;

import gnu.trove.map.hash.THashMap;

public class InductiveVisualMinerConfigurationDefault extends InductiveVisualMinerConfigurationAbstract {

	protected Cl02SortEvents<InductiveVisualMinerConfiguration> sortEvents;

	public InductiveVisualMinerConfigurationDefault(ProMCanceller canceller, Executor executor) {
		super(canceller, executor);
	}

	@Override
	protected List<PreMiningFilter> createPreMiningFilters() {
		return new ArrayList<>(Arrays.asList(new PreMiningFilter[] { //
				new PreMiningFilterEvent(), //
				new PreMiningFilterTrace(), //
				new PreMiningFilterTraceWithEvent(), //
				new PreMiningFilterTraceWithEventTwice(), //
				//new PreMiningFrequentTracesFilter()//
		}));
	}

	@Override
	protected List<HighlightingFilter> createHighlightingFilters() {
		return new ArrayList<>(Arrays.asList(new HighlightingFilter[] { //
				new HighlightingFilterTraceAttribute(), //
				new HighlightingFilterEvent(), //
				new HighlightingFilterWithoutEvent(), //
				new HighlightingFilterEventTwice(), // 
				new HighlightingFilterCompleteEventTwice(), //
				new HighlightingFilterFollows(), //
				new HighlightingFilterLogMove(), //
				new HighlightingFilterTraceStartsWithEvent(), //
				new HighlightingFilterTraceEndsWithEvent(), //
				new HighlightingFilterCohort() //
		}));
	}

	@Override
	public IvMFilterBuilderFactory getFilters() {
		return new IvMFilterBuilderFactory() {
			@SuppressWarnings("unchecked")
			public <X> List<IvMFilterBuilder<X, ?, ?>> get(Class<X> clazz) {
				if (clazz == IvMTrace.class) {
					List<IvMFilterBuilder<X, ?, ?>> filterBuilders = new ArrayList<>();
					filterBuilders.add((IvMFilterBuilder<X, ?, ?>) new FilterIvMTraceAny());
					filterBuilders.add((IvMFilterBuilder<X, ?, ?>) new FilterIvMTraceAttribute());
					filterBuilders.add((IvMFilterBuilder<X, ?, ?>) new FilterIvMTraceWithEvent());
					filterBuilders.add((IvMFilterBuilder<X, ?, ?>) new FilterIvMTraceStartsWithEvent());
					filterBuilders.add((IvMFilterBuilder<X, ?, ?>) new FilterIvMTraceEndsWithEvent());
					filterBuilders.add((IvMFilterBuilder<X, ?, ?>) new FilterIvMTraceWithEventTwice());
					filterBuilders.add((IvMFilterBuilder<X, ?, ?>) new FilterIvMTraceFollows());
					filterBuilders.add((IvMFilterBuilder<X, ?, ?>) new FilterIvMTraceWithoutEvent());
					filterBuilders.add((IvMFilterBuilder<X, ?, ?>) new FilterIvMTraceAnd());
					filterBuilders.add((IvMFilterBuilder<X, ?, ?>) new FilterIvMTraceOr());
					return filterBuilders;
				} else if (clazz == IvMMove.class) {
					List<IvMFilterBuilder<X, ?, ?>> filterBuilders = new ArrayList<>();
					filterBuilders.add((IvMFilterBuilder<X, ?, ?>) new FilterIvMMoveAny());
					filterBuilders.add((IvMFilterBuilder<X, ?, ?>) new FilterIvMMoveAttribute());
					filterBuilders.add((IvMFilterBuilder<X, ?, ?>) new FilterIvMMoveAnd());
					filterBuilders.add((IvMFilterBuilder<X, ?, ?>) new FilterIvMMoveOr());
					return filterBuilders;
				}
				return null;
			}
		};
	}

	@Override
	protected List<VisualMinerWrapper> createDiscoveryTechniques() {
		return new ArrayList<>(Arrays.asList(new VisualMinerWrapper[] { //
				new Miner(), // 
				new DfgMiner(), //
				new LifeCycleMiner(), //
				new AllOperatorsMiner(), //
		}));
	}

	@Override
	protected List<Mode> createModes() {
		return new ArrayList<>(Arrays.asList(new Mode[] { //
				new ModePaths(), //
				new ModePathsDeviations(), //
				new ModePathsQueueLengths(), //
				new ModePathsSojourn(), //
				new ModePathsWaiting(), //
				new ModePathsService(), //
				new ModeRelativePaths(), //
				new ModeCost() }));
	}

	@Override
	protected List<PopupItemActivity> createPopupItemsActivity() {
		return new ArrayList<>(Arrays.asList(new PopupItemActivity[] { //
				new PopupItemActivityName(), //
				new PopupItemActivitySpacer(), //
				new PopupItemActivityOccurrences(), //
				new PopupItemActivityOccurrencesPerTrace(), //
				new PopupItemActivitySpacer(), //
				new PopupItemActivityPerformance(), //
				new PopupItemActivitySpacer(), //
				new PopupItemActivityCost(), //
		}));
	}

	@Override
	protected List<PopupItemStartEnd> createPopupItemsStartEnd() {
		return new ArrayList<>(Arrays.asList(new PopupItemStartEnd[] { //
				new PopupItemStartEndName(), //
				new PopupItemStartEndSpacer(), //
				new PopupItemStartEndNumberOfTraces(), //
				new PopupItemStartEndSpacer(), //
				new PopupItemStartEndPerformance(), //
		}));
	}

	@Override
	protected List<PopupItemLogMove> createPopupItemsLogMove() {
		return new ArrayList<>(Arrays.asList(new PopupItemLogMove[] { //
				new PopupItemLogMoveTitle(), //
				new PopupItemLogMoveSpacer(), //
				new PopupItemLogMoveActivities(), //
		}));
	}

	@Override
	protected List<PopupItemModelMove> createPopupItemsModelMove() {
		return new ArrayList<>(Arrays.asList(new PopupItemModelMove[] { //
				new PopupItemModelMoveOccurrences(), //
		}));
	}

	@Override
	protected List<PopupItemLog> createPopupItemsLog() {
		return new ArrayList<>(Arrays.asList(new PopupItemLog[] { //
				new PopupItemLogTitle(), //
				new PopupItemLogSpacer(), //
				new PopupItemLogName() //
		}));
	}

	@Override
	protected List<DataAnalysisTab<?, InductiveVisualMinerConfiguration, InductiveVisualMinerPanel>> createDataAnalysisTables() {
		List<DataAnalysisTab<?, InductiveVisualMinerConfiguration, InductiveVisualMinerPanel>> result = new ArrayList<>();

		result.add(new DataTabLog<InductiveVisualMinerConfiguration, InductiveVisualMinerPanel>(//
				new Callable<List<DataRowBlock<Object, InductiveVisualMinerConfiguration, InductiveVisualMinerPanel>>>() {
					public List<DataRowBlock<Object, InductiveVisualMinerConfiguration, InductiveVisualMinerPanel>> call()
							throws Exception {
						return new ArrayList<>();
					}
				}, //
				new Callable<List<DataRowBlockComputer<Object, InductiveVisualMinerConfiguration, InductiveVisualMinerPanel>>>() {
					public List<DataRowBlockComputer<Object, InductiveVisualMinerConfiguration, InductiveVisualMinerPanel>> call()
							throws Exception {
						List<DataRowBlockComputer<Object, InductiveVisualMinerConfiguration, InductiveVisualMinerPanel>> r = new ArrayList<>();
						r.add(new DataRowBlockLogAttributes<InductiveVisualMinerConfiguration, InductiveVisualMinerPanel>());
						r.add(new DataRowBlockLogAttributesHighlighted<InductiveVisualMinerConfiguration, InductiveVisualMinerPanel>());
						r.add(new DataRowBlockLogEMSC<InductiveVisualMinerConfiguration, InductiveVisualMinerPanel>());
						return r;
					}
				}));

		result.add(new TraceDataTab<InductiveVisualMinerConfiguration, InductiveVisualMinerPanel>(
				new Callable<List<DataRowBlock<Object, InductiveVisualMinerConfiguration, InductiveVisualMinerPanel>>>() {
					public List<DataRowBlock<Object, InductiveVisualMinerConfiguration, InductiveVisualMinerPanel>> call()
							throws Exception {
						return new ArrayList<>();
					}
				}, //
				new Callable<List<DataRowBlockComputer<Object, InductiveVisualMinerConfiguration, InductiveVisualMinerPanel>>>() {
					public List<DataRowBlockComputer<Object, InductiveVisualMinerConfiguration, InductiveVisualMinerPanel>> call()
							throws Exception {
						List<DataRowBlockComputer<Object, InductiveVisualMinerConfiguration, InductiveVisualMinerPanel>> r = new ArrayList<>();
						r.add(new TraceDataRowBlock<InductiveVisualMinerConfiguration, InductiveVisualMinerPanel>());
						r.add(new TraceDataRowBlockVirtual<InductiveVisualMinerConfiguration, InductiveVisualMinerPanel>());
						r.add(new TraceDataRowBlockType<InductiveVisualMinerConfiguration, InductiveVisualMinerPanel>());
						r.add(new TraceDataRowBlockTypeVirtual<InductiveVisualMinerConfiguration, InductiveVisualMinerPanel>());
						r.add(new TraceDataRowBlockHistogram<InductiveVisualMinerConfiguration, InductiveVisualMinerPanel>());
						r.add(new TraceDataRowBlockHistogramVirtual<InductiveVisualMinerConfiguration, InductiveVisualMinerPanel>());
						return r;
					}
				}));

		result.add(new EventDataTab<InductiveVisualMinerConfiguration, InductiveVisualMinerPanel>(
				new Callable<List<DataRowBlock<Object, InductiveVisualMinerConfiguration, InductiveVisualMinerPanel>>>() {
					public List<DataRowBlock<Object, InductiveVisualMinerConfiguration, InductiveVisualMinerPanel>> call()
							throws Exception {
						return new ArrayList<>();
					}
				}, //
				new Callable<List<DataRowBlockComputer<Object, InductiveVisualMinerConfiguration, InductiveVisualMinerPanel>>>() {
					public List<DataRowBlockComputer<Object, InductiveVisualMinerConfiguration, InductiveVisualMinerPanel>> call()
							throws Exception {
						List<DataRowBlockComputer<Object, InductiveVisualMinerConfiguration, InductiveVisualMinerPanel>> r = new ArrayList<>();
						r.add(new EventDataRowBlock<InductiveVisualMinerConfiguration, InductiveVisualMinerPanel>());
						r.add(new EventDataRowBlockVirtual<InductiveVisualMinerConfiguration, InductiveVisualMinerPanel>());
						r.add(new EventDataRowBlockType<InductiveVisualMinerConfiguration, InductiveVisualMinerPanel>());
						r.add(new EventDataRowBlockTypeVirtual<InductiveVisualMinerConfiguration, InductiveVisualMinerPanel>());
						r.add(new EventDataRowBlockHistogram<InductiveVisualMinerConfiguration, InductiveVisualMinerPanel>());
						r.add(new EventDataRowBlockHistogramVirtual<InductiveVisualMinerConfiguration, InductiveVisualMinerPanel>());
						return r;
					}
				}));

		result.add(new DataAnalysisTabModelTime<InductiveVisualMinerConfiguration, InductiveVisualMinerPanel>(
				new Callable<List<DataRowBlock<Object, InductiveVisualMinerConfiguration, InductiveVisualMinerPanel>>>() {
					public List<DataRowBlock<Object, InductiveVisualMinerConfiguration, InductiveVisualMinerPanel>> call()
							throws Exception {
						List<DataRowBlock<Object, InductiveVisualMinerConfiguration, InductiveVisualMinerPanel>> r = new ArrayList<>();
						r.add(new RowBlockModelPerformance<InductiveVisualMinerConfiguration, InductiveVisualMinerPanel>());
						return r;
					}
				}, //
				new Callable<List<DataRowBlockComputer<Object, InductiveVisualMinerConfiguration, InductiveVisualMinerPanel>>>() {
					public List<DataRowBlockComputer<Object, InductiveVisualMinerConfiguration, InductiveVisualMinerPanel>> call()
							throws Exception {
						List<DataRowBlockComputer<Object, InductiveVisualMinerConfiguration, InductiveVisualMinerPanel>> r = new ArrayList<>();
						r.add(new RowBlockModelHistogram<InductiveVisualMinerConfiguration, InductiveVisualMinerPanel>());
						r.add(new RowBlockModelWeibull<InductiveVisualMinerConfiguration, InductiveVisualMinerPanel>());
						r.add(new RowBlockModelLogNormal<InductiveVisualMinerConfiguration, InductiveVisualMinerPanel>());
						return r;
					}
				}));

		result.add(new CohortDataAnalysisTab<InductiveVisualMinerConfiguration, InductiveVisualMinerPanel>(
				new Callable<List<DataRowBlock<Cohort, InductiveVisualMinerConfiguration, InductiveVisualMinerPanel>>>() {
					public List<DataRowBlock<Cohort, InductiveVisualMinerConfiguration, InductiveVisualMinerPanel>> call()
							throws Exception {
						List<DataRowBlock<Cohort, InductiveVisualMinerConfiguration, InductiveVisualMinerPanel>> r = new ArrayList<>();
						r.add(new CohortRowBlock<InductiveVisualMinerConfiguration, InductiveVisualMinerPanel>());
						return r;
					}
				}, //
				new Callable<List<DataRowBlockComputer<Cohort, InductiveVisualMinerConfiguration, InductiveVisualMinerPanel>>>() {
					public List<DataRowBlockComputer<Cohort, InductiveVisualMinerConfiguration, InductiveVisualMinerPanel>> call()
							throws Exception {
						return new ArrayList<>();
					}
				}));

		result.add(new CostDataTab<InductiveVisualMinerConfiguration, InductiveVisualMinerPanel>(
				new Callable<List<DataRowBlock<Object, InductiveVisualMinerConfiguration, InductiveVisualMinerPanel>>>() {
					public List<DataRowBlock<Object, InductiveVisualMinerConfiguration, InductiveVisualMinerPanel>> call()
							throws Exception {
						List<DataRowBlock<Object, InductiveVisualMinerConfiguration, InductiveVisualMinerPanel>> r = new ArrayList<>();
						r.add(new CostRowBlock<InductiveVisualMinerConfiguration, InductiveVisualMinerPanel>());
						return r;
					}
				}, //
				new Callable<List<DataRowBlockComputer<Object, InductiveVisualMinerConfiguration, InductiveVisualMinerPanel>>>() {
					public List<DataRowBlockComputer<Object, InductiveVisualMinerConfiguration, InductiveVisualMinerPanel>> call()
							throws Exception {
						return new ArrayList<>();
					}
				}));

		result.add(new DataAnalysisTabAssociations<>(
				new Callable<List<DataRowBlock<Object, InductiveVisualMinerConfiguration, InductiveVisualMinerPanel>>>() {
					public List<DataRowBlock<Object, InductiveVisualMinerConfiguration, InductiveVisualMinerPanel>> call()
							throws Exception {
						List<DataRowBlock<Object, InductiveVisualMinerConfiguration, InductiveVisualMinerPanel>> r = new ArrayList<>();
						r.add(new AssociationsRowBlockProcess<InductiveVisualMinerConfiguration, InductiveVisualMinerPanel>());
						return r;
					}
				}, //
				new Callable<List<DataRowBlockComputer<Object, InductiveVisualMinerConfiguration, InductiveVisualMinerPanel>>>() {
					public List<DataRowBlockComputer<Object, InductiveVisualMinerConfiguration, InductiveVisualMinerPanel>> call()
							throws Exception {
						List<DataRowBlockComputer<Object, InductiveVisualMinerConfiguration, InductiveVisualMinerPanel>> r = new ArrayList<>();
						r.add(new AssociationsRowBlock<InductiveVisualMinerConfiguration, InductiveVisualMinerPanel>());
						return r;
					}
				}));

		//		result.add(new DataAnalysisTabCausal<>(
		//				new Callable<List<DataRowBlock<Object, InductiveVisualMinerConfiguration, InductiveVisualMinerPanel>>>() {
		//					public List<DataRowBlock<Object, InductiveVisualMinerConfiguration, InductiveVisualMinerPanel>> call()
		//							throws Exception {
		//						List<DataRowBlock<Object, InductiveVisualMinerConfiguration, InductiveVisualMinerPanel>> r = new ArrayList<>();
		//						r.add(new RowBlockCausal<InductiveVisualMinerConfiguration, InductiveVisualMinerPanel>());
		//						return r;
		//
		//					}
		//				}, //
		//				new Callable<List<DataRowBlockComputer<Object, InductiveVisualMinerConfiguration, InductiveVisualMinerPanel>>>() {
		//					public List<DataRowBlockComputer<Object, InductiveVisualMinerConfiguration, InductiveVisualMinerPanel>> call()
		//							throws Exception {
		//						return new ArrayList<>();
		//					}
		//				}));

		return result;
	}

	@Override
	protected List<CostModelFactory> createCostModelFactories() {
		return new ArrayList<>(Arrays.asList(new CostModelFactory[] { //
				new CostModelFactoryImplModelLP(), //
				new CostModelFactoryImplModelDeviationsLP(), //
				new CostModelFactoryImplModelDeviationsServiceLP(), //
				new CostModelFactoryImplModelDeviationsTimeLP(), //
		}));
	}

	@Override
	protected IvMVirtualAttributeFactory createVirtualAttributes() {
		return new IvMVirtualAttributeFactory() {
			public Iterable<AttributeVirtual> createVirtualTraceAttributes(
					THashMap<String, AttributeImpl> traceAttributesReal,
					THashMap<String, AttributeImpl> eventAttributesReal) {
				return new ArrayList<>(Arrays.asList(new AttributeVirtual[] { //
						new VirtualAttributeTraceDuration(), //
						new VirtualAttributeTraceLength(), //
				}));
			}

			public Iterable<AttributeVirtual> createVirtualEventAttributes(
					THashMap<String, AttributeImpl> traceAttributesReal,
					THashMap<String, AttributeImpl> eventAttributesReal) {
				return new ArrayList<>(Arrays.asList(new AttributeVirtual[] { //
						//
				}));
			}

			public Iterable<AttributeVirtual> createVirtualIvMTraceAttributes(
					THashMap<String, AttributeImpl> traceAttributesReal,
					THashMap<String, AttributeImpl> eventAttributesReal) {
				ArrayList<AttributeVirtual> result = new ArrayList<>(Arrays.asList(new AttributeVirtual[] { //
						new VirtualAttributeTraceNumberOfCompleteEvents(), //
						new VirtualAttributeTraceHasDeviations(), //
						new VirtualAttributeTraceFitness(), //
						new VirtualAttributeTraceNumberOfModelMoves(), //
						new VirtualAttributeTraceNumberOfLogMoves(), //
				}));
				for (AttributeImpl eventAttribute : eventAttributesReal.values()) {
					result.add(new VirtualAttributeTraceDistinctEventAttribute(eventAttribute));
					if (eventAttribute.isNumeric()) {
						result.add(new VirtualAttributeTraceSumEventAttribute(eventAttribute));
					}
				}
				return result;
			}

			public Iterable<AttributeVirtual> createVirtualIvMEventAttributes(
					THashMap<String, AttributeImpl> traceAttributesReal,
					THashMap<String, AttributeImpl> eventAttributesReal) {
				return new ArrayList<>(Arrays.asList(new AttributeVirtual[] { //
						//
				}));
			}
		};
	}

	@Override
	protected List<IvMExporter> createExporters() {
		return new ArrayList<>(Arrays.asList(new IvMExporter[] { //
				new ExporterDataAnalyses(this), //
				new ExporterTraceData(), //
				new ExporterModelStatistics(this), //
				new ExporterAvi(this), //
		}));
	}

	@Override
	protected InductiveVisualMinerPanel createPanel(ProMCanceller canceller) {
		return new InductiveVisualMinerPanel(this, canceller);
	}

	@Override
	public DataChain<InductiveVisualMinerConfiguration> createChain(final InductiveVisualMinerPanel panel,
			final ProMCanceller canceller, final Executor executor, final List<PreMiningFilter> preMiningFilters,
			final List<HighlightingFilter> highlightingFilters) {
		//set up the state
		DataState state = new DataState();

		//set up the chain
		final DataChainImplNonBlocking<InductiveVisualMinerConfiguration, InductiveVisualMinerPanel> chain = new DataChainImplNonBlocking<InductiveVisualMinerConfiguration, InductiveVisualMinerPanel>(
				state, canceller, executor, this, panel);

		chain.register(new Cl01GatherAttributes());

		sortEvents = new Cl02SortEvents<>();
		chain.register(sortEvents);
		sortEvents.setOnIllogicalTimeStamps(new Function<Object, Boolean>() {
			public Boolean call(Object input) throws Exception {
				String[] options = new String[] { "Continue with neither animation nor performance", "Reorder events" };
				int n = JOptionPane.showOptionDialog(panel,
						"The event log contains illogical time stamps,\n i.e. some time stamps contradict the order of events.\n\nInductive visual Miner can reorder the events and discover a new model.\nWould you like to do that?", //message
						"Illogical Time Stamps", //title
						JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, //do not use a custom Icon
						options, //the titles of buttons
						options[0]); //default button title
				if (n == 1) {
					//the user requested to reorder the events
					return true;
				}
				return false;
			}
		});

		chain.register(new Cl03MakeLog<InductiveVisualMinerConfiguration>());
		chain.register(new Cl04FilterLogOnActivities());
		chain.register(new Cl05Mine<InductiveVisualMinerConfiguration>());
		chain.register(new Cl06LayoutModel<InductiveVisualMinerConfiguration>());
		chain.register(new Cl07Align());
		chain.register(new Cl08UpdateIvMAttributes());
		chain.register(new Cl09LayoutAlignment(this));
		chain.register(new Cl10AnimationScaler<InductiveVisualMinerConfiguration>());
		chain.register(new Cl11Animate<InductiveVisualMinerConfiguration>());
		chain.register(new Cl12TraceColouring<InductiveVisualMinerConfiguration>());
		chain.register(new Cl13FilterNodeSelection());
		chain.register(new Cl14Performance<InductiveVisualMinerConfiguration>());
		chain.register(new Cl14PerformanceNegative<InductiveVisualMinerConfiguration>());
		chain.register(new Cl15Histogram<InductiveVisualMinerConfiguration>());
		chain.register(new Cl18DataAnalysisCohort<InductiveVisualMinerConfiguration>());
		chain.register(new Cl19DataAnalysisCost<InductiveVisualMinerConfiguration>());
		chain.register(new Cl20DataAnalysisAssociations<InductiveVisualMinerConfiguration>());
		chain.register(new Cl21AdvancedAnalysesDelay<InductiveVisualMinerConfiguration>());
		chain.register(new Cl22AdvancedAnalysisCausal<InductiveVisualMinerConfiguration>());

		return chain;
	}

	protected AlignmentComputer createAlignmentComputer() {
		return new AlignmentComputerImpl();
	}

	protected IvMDecoratorI createDecorator() {
		return new IvMDecoratorDefault();
	}
}