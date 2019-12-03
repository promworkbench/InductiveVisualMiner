package org.processmining.plugins.inductiveVisualMiner.configuration;

import java.util.concurrent.Executor;

import javax.swing.JOptionPane;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.ProMCanceller;
import org.processmining.plugins.InductiveMiner.Function;
import org.processmining.plugins.InductiveMiner.efficienttree.UnknownTreeNodeException;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerController;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerPanel;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;
import org.processmining.plugins.inductiveVisualMiner.Selection;
import org.processmining.plugins.inductiveVisualMiner.chain.Chain;
import org.processmining.plugins.inductiveVisualMiner.chain.Cl01GatherAttributes;
import org.processmining.plugins.inductiveVisualMiner.chain.Cl02SortEvents;
import org.processmining.plugins.inductiveVisualMiner.chain.Cl03MakeLog;
import org.processmining.plugins.inductiveVisualMiner.chain.Cl04FilterLogOnActivities;
import org.processmining.plugins.inductiveVisualMiner.chain.Cl05Mine;
import org.processmining.plugins.inductiveVisualMiner.chain.Cl06LayoutModel;
import org.processmining.plugins.inductiveVisualMiner.chain.Cl07Align;
import org.processmining.plugins.inductiveVisualMiner.chain.Cl08LayoutAlignment;
import org.processmining.plugins.inductiveVisualMiner.chain.Cl09AnimationScaler;
import org.processmining.plugins.inductiveVisualMiner.chain.Cl10Animate;
import org.processmining.plugins.inductiveVisualMiner.chain.Cl11TraceColouring;
import org.processmining.plugins.inductiveVisualMiner.chain.Cl12FilterNodeSelection;
import org.processmining.plugins.inductiveVisualMiner.chain.Cl13Performance;
import org.processmining.plugins.inductiveVisualMiner.chain.Cl14Histogram;
import org.processmining.plugins.inductiveVisualMiner.chain.Cl15DataAnalysis;
import org.processmining.plugins.inductiveVisualMiner.chain.Cl16Done;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.IvMFiltersController;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.highlightingfilter.HighlightingFiltersView;
import org.processmining.plugins.inductiveVisualMiner.mode.Mode;
import org.processmining.plugins.inductiveVisualMiner.mode.ModePaths;
import org.processmining.plugins.inductiveVisualMiner.mode.ModePathsDeviations;
import org.processmining.plugins.inductiveVisualMiner.mode.ModePathsQueueLengths;
import org.processmining.plugins.inductiveVisualMiner.mode.ModePathsService;
import org.processmining.plugins.inductiveVisualMiner.mode.ModePathsSojourn;
import org.processmining.plugins.inductiveVisualMiner.mode.ModePathsWaiting;
import org.processmining.plugins.inductiveVisualMiner.mode.ModeRelativePaths;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupPopulator;
import org.processmining.plugins.inductiveVisualMiner.tracecolouring.TraceColourMapSettings;
import org.processmining.plugins.inductiveVisualMiner.visualMinerWrapper.VisualMinerWrapper;
import org.processmining.plugins.inductiveVisualMiner.visualMinerWrapper.miners.AllOperatorsMiner;
import org.processmining.plugins.inductiveVisualMiner.visualMinerWrapper.miners.DfgMiner;
import org.processmining.plugins.inductiveVisualMiner.visualMinerWrapper.miners.LifeCycleMiner;
import org.processmining.plugins.inductiveVisualMiner.visualMinerWrapper.miners.Miner;

/**
 * IvM configuration that contains the chainlink. To extend, please subclass and
 * override the getChain() method.
 * 
 * @author sander
 *
 */
public abstract class InductiveVisualMinerConfigurationAbstract implements InductiveVisualMinerConfiguration {

	protected final Cl01GatherAttributes gatherAttributes = new Cl01GatherAttributes();
	protected final Cl02SortEvents sortEvents = new Cl02SortEvents();
	protected final Cl03MakeLog makeLog = new Cl03MakeLog();
	protected final Cl04FilterLogOnActivities filterLogOnActivities = new Cl04FilterLogOnActivities();
	protected final Cl05Mine mine = new Cl05Mine();
	protected final Cl06LayoutModel layoutModel = new Cl06LayoutModel();
	protected final Cl07Align align = new Cl07Align();
	protected final Cl08LayoutAlignment layoutAlignment = new Cl08LayoutAlignment();
	protected final Cl09AnimationScaler animationScaler = new Cl09AnimationScaler();
	protected final Cl10Animate animate = new Cl10Animate();
	protected final Cl11TraceColouring traceColouring = new Cl11TraceColouring();
	protected final Cl12FilterNodeSelection filterNodeSelection = new Cl12FilterNodeSelection();
	protected final Cl13Performance performance = new Cl13Performance();
	protected final Cl14Histogram histogram = new Cl14Histogram();
	protected final Cl15DataAnalysis dataAnalysis = new Cl15DataAnalysis();
	protected final Cl16Done done = new Cl16Done();

	public final VisualMinerWrapper[] discoveryTechniques = new VisualMinerWrapper[] { new Miner(), new DfgMiner(),
			new LifeCycleMiner(), new AllOperatorsMiner() };

	public final Mode[] modes = new Mode[] { new ModePaths(), new ModePathsDeviations(), new ModePathsQueueLengths(),
			new ModePathsSojourn(), new ModePathsWaiting(), new ModePathsService(), new ModeRelativePaths() };

	@Override
	public VisualMinerWrapper[] getDiscoveryTechniques() {
		return discoveryTechniques;
	}

	@Override
	public Mode[] getModes() {
		return modes;
	}

	@Override
	public Chain getChain(final PluginContext context, final InductiveVisualMinerState state,
			final InductiveVisualMinerPanel panel, final ProMCanceller canceller, final Executor executor,
			final Runnable onChange) {
		//set up the chain
		final Chain chain = new Chain(state, canceller, executor, onChange);

		//gather attributes
		{
			gatherAttributes.setOnComplete(new Runnable() {
				public void run() {
					panel.getClassifiers().setEnabled(true);

					//update the classifier combobox
					panel.getClassifiers().replaceClassifiers(state.getClassifiers(), state.getInitialClassifier());

					//initialise the filters
					{
						Runnable onUpdatePreMining = new Runnable() {
							public void run() {
								chain.execute(Cl04FilterLogOnActivities.class);
							}
						};
						Runnable onUpdateHighlighting = new Runnable() {
							public void run() {
								chain.execute(Cl12FilterNodeSelection.class);
							}
						};
						Function<TraceColourMapSettings, Object> onUpdateTraceColourMap = new Function<TraceColourMapSettings, Object>() {
							public Object call(TraceColourMapSettings input) throws Exception {
								state.setTraceColourMapSettings(input);
								chain.execute(Cl11TraceColouring.class);
								return null;
							}
						};
						state.setFiltersController(new IvMFiltersController(context, panel, state, onUpdatePreMining,
								onUpdateHighlighting));
						panel.getTraceColourMapView().initialise(state.getAttributesInfo(), onUpdateTraceColourMap);
					}

					//initialise the data analysis view
					{
						panel.getDataAnalysisView().initialiseAttributes(state.getAttributesInfo());
					}
				}
			});
			gatherAttributes.setOnInvalidate(new Runnable() {
				public void run() {
					panel.getClassifiers().setEnabled(false);
				}
			});

		}

		//reorder events
		{
			sortEvents.setOnStart(new Runnable() {

				public void run() {
					panel.getGraph().setAnimationEnabled(false);
				}
			});
			sortEvents.setOnIllogicalTimeStamps(new Function<Object, Boolean>() {
				public Boolean call(Object input) throws Exception {
					String[] options = new String[] { "Continue with neither animation nor performance",
							"Reorder events" };
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

			chain.addConnection(gatherAttributes, sortEvents);
		}

		//make log
		{
			makeLog.setOnComplete(new Runnable() {
				public void run() {
					panel.getTraceView().set(state.getLog(), state.getTraceColourMap());

					state.getFiltersController().updateFiltersWithIMLog(panel, state.getLog(), state.getSortedXLog(),
							context.getExecutor());
				}
			});

			chain.addConnection(sortEvents, makeLog);
		}

		//filter on activities
		{
			chain.addConnection(makeLog, filterLogOnActivities);
		}

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

			chain.addConnection(filterLogOnActivities, mine);
		}

		//layout
		{
			layoutModel.setOnComplete(new Runnable() {
				public void run() {
					panel.getGraph().changeDot(state.getDot(), state.getSVGDiagram(), true);
				}
			});

			chain.addConnection(mine, layoutModel);
		}

		//align
		{
			align.setOnComplete(new Runnable() {
				public void run() {
					panel.getSaveLogButton().setEnabled(true);
					panel.getTraceView().set(state.getModel(), state.getIvMLog(), state.getSelection(),
							state.getTraceColourMap());

					state.getFiltersController().updateFiltersWithIvMLog(panel, state.getIvMLog(),
							context.getExecutor());
				}
			});
			align.setOnInvalidate(new Runnable() {
				public void run() {
					panel.getSaveLogButton().setEnabled(false);
				}
			});

			chain.addConnection(mine, align);
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

			chain.addConnection(layoutModel, layoutAlignment);
			chain.addConnection(align, layoutAlignment);
		}

		//animation scaler
		{
			chain.addConnection(align, animationScaler);
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

			chain.addConnection(animationScaler, animate);
			chain.addConnection(layoutAlignment, animate);
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

			chain.addConnection(align, traceColouring);
		}

		//filter node selection
		{
			filterNodeSelection.setOnComplete(new Runnable() {
				public void run() {
					HighlightingFiltersView.updateSelectionDescription(panel, state.getSelection(),
							state.getFiltersController(), state.getModel());

					//tell trace view the colour map and the selection
					panel.getTraceView().set(state.getModel(), state.getIvMLogFiltered(), state.getSelection(),
							state.getTraceColourMap());

					try {
						InductiveVisualMinerController.updateHighlighting(panel, state);
						PopupPopulator.updatePopup(panel, state);
					} catch (UnknownTreeNodeException e) {
						e.printStackTrace();
					}

					//tell the animation the filtered log
					panel.getGraph().setFilteredLog(state.getIvMLogFiltered());

					panel.repaint();
				}
			});
			filterNodeSelection.setOnInvalidate(new Runnable() {
				public void run() {
					//tell the animation the filtered log
					panel.getGraph().setFilteredLog(null);

					try {
						PopupPopulator.updatePopup(panel, state);
					} catch (UnknownTreeNodeException e) {
						e.printStackTrace();
					}
					panel.getGraph().repaint();
				}
			});

			chain.addConnection(layoutAlignment, filterNodeSelection);
		}

		//mine performance
		{
			performance.setOnComplete(new Runnable() {
				public void run() {
					try {
						InductiveVisualMinerController.updateHighlighting(panel, state);
						PopupPopulator.updatePopup(panel, state);
					} catch (UnknownTreeNodeException e) {
						e.printStackTrace();
					}
					panel.getGraph().repaint();
				}
			});
			performance.setOnInvalidate(new Runnable() {
				public void run() {
					try {
						PopupPopulator.updatePopup(panel, state);
					} catch (UnknownTreeNodeException e) {
						e.printStackTrace();
					}
					panel.getGraph().repaint();
				}
			});

			chain.addConnection(animationScaler, performance);
			chain.addConnection(filterNodeSelection, performance);
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

			chain.addConnection(animationScaler, histogram);
			chain.addConnection(filterNodeSelection, histogram);
		}

		//15 data analysis
		{
			dataAnalysis.setOnComplete(new Runnable() {
				public void run() {
					panel.getDataAnalysisView().setDataAnalysis(state.getDataAnalysis());
				}
			});
			dataAnalysis.setOnInvalidate(new Runnable() {
				public void run() {
					panel.getDataAnalysisView().invalidateContent();
				}
			});

			chain.addConnection(filterNodeSelection, dataAnalysis);
		}

		//done
		{
			chain.addConnection(histogram, done);
			chain.addConnection(performance, done);
			chain.addConnection(traceColouring, done);
			chain.addConnection(animate, done);
			chain.addConnection(dataAnalysis, done);
		}

		return chain;
	}

}
