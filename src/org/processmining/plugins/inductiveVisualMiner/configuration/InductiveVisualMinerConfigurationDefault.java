package org.processmining.plugins.inductiveVisualMiner.configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;

import javax.swing.JOptionPane;

import org.deckfour.xes.model.XLog;
import org.processmining.framework.plugin.ProMCanceller;
import org.processmining.plugins.InductiveMiner.Function;
import org.processmining.plugins.InductiveMiner.efficienttree.UnknownTreeNodeException;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerController;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerPanel;
import org.processmining.plugins.inductiveVisualMiner.Selection;
import org.processmining.plugins.inductiveVisualMiner.alignment.AlignmentComputer;
import org.processmining.plugins.inductiveVisualMiner.alignment.AlignmentComputerImpl;
import org.processmining.plugins.inductiveVisualMiner.attributes.IvMVirtualAttributeFactory;
import org.processmining.plugins.inductiveVisualMiner.attributes.VirtualAttributeTraceDuration;
import org.processmining.plugins.inductiveVisualMiner.attributes.VirtualAttributeTraceFitness;
import org.processmining.plugins.inductiveVisualMiner.attributes.VirtualAttributeTraceLength;
import org.processmining.plugins.inductiveVisualMiner.attributes.VirtualAttributeTraceNumberOfCompleteEvents;
import org.processmining.plugins.inductiveVisualMiner.attributes.VirtualAttributeTraceNumberOfLogMoves;
import org.processmining.plugins.inductiveVisualMiner.attributes.VirtualAttributeTraceNumberOfModelMoves;
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
import org.processmining.plugins.inductiveVisualMiner.chain.Cl15Histogram;
import org.processmining.plugins.inductiveVisualMiner.chain.Cl16DataAnalysisTrace;
import org.processmining.plugins.inductiveVisualMiner.chain.Cl17DataAnalysisEvent;
import org.processmining.plugins.inductiveVisualMiner.chain.Cl18DataAnalysisCohort;
import org.processmining.plugins.inductiveVisualMiner.chain.Cl19DataAnalysisLog;
import org.processmining.plugins.inductiveVisualMiner.chain.Cl20Done;
import org.processmining.plugins.inductiveVisualMiner.chain.Cl21DataAnalysisLog;
import org.processmining.plugins.inductiveVisualMiner.chain.DataChain;
import org.processmining.plugins.inductiveVisualMiner.chain.DataState;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataAnalysisTableFactory;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.cohorts.CohortAnalysisTableFactory;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.eventattributes.EventAttributeAnalysisTableFactory;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.logattributes.LogAttributeAnalysisTableFactory;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.traceattributes.TraceAttributeAnalysisTableFactory;
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
import org.processmining.plugins.inductiveVisualMiner.mode.Mode;
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
import org.processmining.plugins.inductiveVisualMiner.popup.items.PopupItemActivityName;
import org.processmining.plugins.inductiveVisualMiner.popup.items.PopupItemActivityOccurrences;
import org.processmining.plugins.inductiveVisualMiner.popup.items.PopupItemActivityOccurrencesPerTrace;
import org.processmining.plugins.inductiveVisualMiner.popup.items.PopupItemActivityPerformance;
import org.processmining.plugins.inductiveVisualMiner.popup.items.PopupItemActivitySpacer;
import org.processmining.plugins.inductiveVisualMiner.popup.items.PopupItemLogMoveActivities;
import org.processmining.plugins.inductiveVisualMiner.popup.items.PopupItemLogMoveSpacer;
import org.processmining.plugins.inductiveVisualMiner.popup.items.PopupItemLogMoveTitle;
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

	protected Cl01GatherAttributes gatherAttributes;
	protected Cl02SortEvents sortEvents;
	protected Cl03MakeLog makeLog;
	protected Cl04FilterLogOnActivities filterLogOnActivities;
	protected Cl05Mine mine;
	protected Cl06LayoutModel layoutModel;
	protected Cl07Align align;
	protected Cl08UpdateIvMAttributes ivmAttributes;
	protected Cl09LayoutAlignment layoutAlignment;
	protected Cl10AnimationScaler animationScaler;
	protected Cl11Animate animate;
	protected Cl12TraceColouring traceColouring;
	protected Cl13FilterNodeSelection filterNodeSelection;
	protected Cl14Performance performance;
	protected Cl15Histogram histogram;
	protected Cl16DataAnalysisTrace dataAnalysisTrace;
	protected Cl17DataAnalysisEvent dataAnalysisEvent;
	protected Cl18DataAnalysisCohort dataAnalysisCohort;
	protected Cl19DataAnalysisLog dataAnalysisLog;
	protected Cl20Done done;

	public InductiveVisualMinerConfigurationDefault(XLog log, ProMCanceller canceller, Executor executor) {
		super(log, canceller, executor);
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
				new ModeRelativePaths() }));
	}

	@Override
	protected List<PopupItemActivity> createPopupItemsActivity() {
		return new ArrayList<>(Arrays.asList(new PopupItemActivity[] { //
				new PopupItemActivityName(), //
				new PopupItemActivitySpacer(), //
				new PopupItemActivityOccurrences(), //
				new PopupItemActivityOccurrencesPerTrace(), //
				new PopupItemActivitySpacer(), //
				new PopupItemActivityPerformance(),//
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

		}));
	}

	@Override
	public List<DataAnalysisTableFactory> createDataAnalysisTables() {
		return new ArrayList<>(Arrays.asList(new DataAnalysisTableFactory[] { //
				new LogAttributeAnalysisTableFactory(), //
				new TraceAttributeAnalysisTableFactory(), //
				new EventAttributeAnalysisTableFactory(), //
				new CohortAnalysisTableFactory(), //
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
				return new ArrayList<>(Arrays.asList(new AttributeVirtual[] { //
						new VirtualAttributeTraceNumberOfCompleteEvents(), //
						new VirtualAttributeTraceFitness(), //
						new VirtualAttributeTraceNumberOfModelMoves(), //
						new VirtualAttributeTraceNumberOfLogMoves(), //
				}));
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
	protected DataState createState(XLog log) {
		return new DataState(log);
	}

	@Override
	protected InductiveVisualMinerPanel createPanel(ProMCanceller canceller) {
		return new InductiveVisualMinerPanel(this, canceller);
	}

	@Override
	public DataChain createChain(final DataState state, final InductiveVisualMinerPanel panel,
			final ProMCanceller canceller, final Executor executor, final List<PreMiningFilter> preMiningFilters,
			final List<HighlightingFilter> highlightingFilters) {
		//set up the chain
		final DataChain chain = new DataChain(state, canceller, executor, this, panel);

		chain.register(new Cl01GatherAttributes());

		sortEvents = new Cl02SortEvents();
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

		chain.register(new Cl21DataAnalysisLog());
		chain.register(new Cl03MakeLog());
		chain.register(new Cl04FilterLogOnActivities());

		mine = new Cl05Mine();
		layoutModel = new Cl06LayoutModel();
		align = new Cl07Align();
		ivmAttributes = new Cl08UpdateIvMAttributes();
		layoutAlignment = new Cl09LayoutAlignment();
		animationScaler = new Cl10AnimationScaler();
		animate = new Cl11Animate();
		traceColouring = new Cl12TraceColouring();
		filterNodeSelection = new Cl13FilterNodeSelection();
		performance = new Cl14Performance();
		histogram = new Cl15Histogram();
		dataAnalysisTrace = new Cl16DataAnalysisTrace();
		dataAnalysisEvent = new Cl17DataAnalysisEvent();
		dataAnalysisCohort = new Cl18DataAnalysisCohort();
		dataAnalysisLog = new Cl19DataAnalysisLog();
		done = new Cl20Done();

		//mine a model
		{
			mine.setOnComplete(new Runnable() {
				public void run() {
					panel.getSaveModelButton().setEnabled(true);
					panel.getEditModelView().setModel(state.getModel());
				}
			});
			mine.setOnInvalidate(new Runnable() {
				public void run() {
					panel.getSaveModelButton().setEnabled(false);
					panel.getEditModelView().setMessage("Mining tree...");
					state.setSelection(new Selection());
				}
			});
		}

		//layout
		{
			layoutModel.setOnComplete(new Runnable() {
				public void run() {
					panel.getGraph().changeDot(state.getDot(), state.getSVGDiagram(), true);
				}
			});
		}

		//align
		{
			align.setOnComplete(new Runnable() {
				public void run() {
					panel.getSaveLogButton().setEnabled(true);
					panel.getTraceView().set(state.getModel(), state.getIvMLog(), state.getSelection(),
							state.getTraceColourMap());

					//TODO: enable
					//PopupPopulator.updatePopup(panel, state);
				}
			});
			align.setOnInvalidate(new Runnable() {
				public void run() {
					panel.getSaveLogButton().setEnabled(false);

					//TODO: enable
					//PopupPopulator.updatePopup(panel, state);
				}
			});
		}

		//layout with alignment
		{
			layoutAlignment.setOnStart(new Runnable() {
				public void run() {
					//if the view does not show deviations, do not select any log moves
					if (!state.getMode().isShowDeviations()) {
						state.removeModelAndLogMovesSelection();
					}
				}
			});
			layoutAlignment.setOnComplete(new Runnable() {
				public void run() {
					panel.getGraph().changeDot(state.getDot(), state.getSVGDiagram(), true);

					InductiveVisualMinerController.makeElementsSelectable(state.getVisualisationInfo(), panel,
							state.getSelection());

					//tell the trace view the colours of activities
					panel.getTraceView().setEventColourMap(state.getTraceViewColourMap());
				}
			});
		}

		//ivm attributes
		{
			ivmAttributes.setOnComplete(new Runnable() {
				public void run() {
					//update highlighting filters
					state.getHighlightingFiltersController().setAttributesInfo(state.getIvMAttributesInfo());

					//update trace colour window 
					panel.getTraceColourMapView().setAttributes(state.getIvMAttributesInfo());
				}
			});
			ivmAttributes.setOnInvalidate(new Runnable() {
				public void run() {
					panel.getTraceColourMapView().invalidateAttributes();
				}
			});
		}

		//animate
		{
			animate.setOnStart(new Runnable() {
				public void run() {
					InductiveVisualMinerController.setAnimationStatus(panel, " ", false);
				}
			});

			animate.setOnComplete(new Runnable() {
				public void run() {
					if (state.getAnimationGraphVizTokens() != null) {
						//animation enabled; store the result
						panel.getGraph().setTokens(state.getAnimationGraphVizTokens());
						panel.getGraph().setAnimationExtremeTimes(state.getAnimationScaler().getMinInUserTime(),
								state.getAnimationScaler().getMaxInUserTime());
						panel.getGraph().setFilteredLog(state.getIvMLogFiltered());
						panel.getGraph().setAnimationEnabled(true);
					} else {
						//animation disabled
						System.out.println("animation disabled");
						InductiveVisualMinerController.setAnimationStatus(panel, "animation disabled", true);
						panel.getGraph().setAnimationEnabled(false);
					}
					panel.repaint();

					//record the width of the panel (necessary for histogram later)
					state.setHistogramWidth((int) panel.getGraph().getControlsProgressLine().getWidth());
				}
			});

			animate.setOnInvalidate(new Runnable() {
				public void run() {
					panel.getGraph().setAnimationEnabled(false);
					InductiveVisualMinerController.setAnimationStatus(panel, " ", false);
				}
			});
		}

		//colour traces
		{
			traceColouring.setOnComplete(new Runnable() {
				public void run() {
					//tell the animation and the trace view the trace colour map
					panel.getGraph().setTraceColourMap(state.getTraceColourMap());
					panel.getTraceView().setTraceColourMap(state.getTraceColourMap());
					panel.getTraceView().repaint();

					panel.repaint();
				}
			});
		}

		//filter node selection
		{

		}

		//mine performance
		{
			performance.setOnComplete(new Runnable() {
				public void run() {
					//TODO: enable
					//PopupPopulator.updatePopup(panel, state);
					try {
						InductiveVisualMinerController.updateHighlighting(panel, state);
					} catch (UnknownTreeNodeException e) {
						e.printStackTrace();
					}
					panel.getGraph().repaint();
				}
			});
			performance.setOnInvalidate(new Runnable() {
				public void run() {
					//TODO: enable
					//PopupPopulator.updatePopup(panel, state);
					panel.getGraph().repaint();
				}
			});
		}

		//compute histogram
		{
			histogram.setOnComplete(new Runnable() {
				public void run() {
					//pass the histogram data to the panel
					panel.getGraph().setHistogramData(state.getHistogramData());
					panel.getGraph().repaint();
				}
			});
		}

		//data analysis - trace
		{
			dataAnalysisTrace.setOnComplete(new Runnable() {
				public void run() {
					panel.getDataAnalysesView().setData(TraceAttributeAnalysisTableFactory.name, state);
				}
			});
			dataAnalysisTrace.setOnInvalidate(new Runnable() {
				public void run() {
					panel.getDataAnalysesView().invalidate(TraceAttributeAnalysisTableFactory.name);
				}
			});
		}

		//data analysis - event
		{
			dataAnalysisEvent.setOnComplete(new Runnable() {
				public void run() {
					panel.getDataAnalysesView().setData(EventAttributeAnalysisTableFactory.name, state);
				}
			});
			dataAnalysisEvent.setOnInvalidate(new Runnable() {
				public void run() {
					panel.getDataAnalysesView().invalidate(EventAttributeAnalysisTableFactory.name);
				}
			});
		}

		//data analysis - cohort analysis
		{
			dataAnalysisCohort.setOnComplete(new Runnable() {
				public void run() {
					panel.getDataAnalysesView().setData(CohortAnalysisTableFactory.name, state);
				}
			});
			dataAnalysisCohort.setOnInvalidate(new Runnable() {
				public void run() {
					panel.getDataAnalysesView().invalidate(CohortAnalysisTableFactory.name);
				}
			});
		}

		//data analysis - log
		{
			dataAnalysisLog.setOnComplete(new Runnable() {
				public void run() {
					panel.getDataAnalysesView().setData(LogAttributeAnalysisTableFactory.name, state);
				}
			});
			dataAnalysisLog.setOnInvalidate(new Runnable() {
				public void run() {
					/*
					 * The bulk of the work is performed in Cl02SortEvents.
					 * Therefore, we do not invalidate the result but rather
					 * update it.
					 */
					panel.getDataAnalysesView().setData(LogAttributeAnalysisTableFactory.name, state);
				}
			});
		}

		return chain;
	}

	protected AlignmentComputer createAlignmentComputer() {
		return new AlignmentComputerImpl();
	}

	protected IvMDecoratorI createDecorator() {
		return new IvMDecoratorDefault();
	}
}