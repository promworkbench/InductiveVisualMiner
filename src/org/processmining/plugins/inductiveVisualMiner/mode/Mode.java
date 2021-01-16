package org.processmining.plugins.inductiveVisualMiner.mode;

import java.awt.Color;

import org.processmining.plugins.graphviz.colourMaps.ColourMapFixed;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.data.AlignedLogVisualisationData;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMObject;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMObjectValues;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.sizeMaps.SizeMapFixed;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFiltered;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogInfo;
import org.processmining.plugins.inductiveVisualMiner.performance.PerformanceWrapper;
import org.processmining.plugins.inductiveVisualMiner.performance.QueueActivityLog;
import org.processmining.plugins.inductiveVisualMiner.visualisation.ProcessTreeVisualisationParameters;

import gnu.trove.map.TIntObjectMap;

/**
 * Class to set the visualisation parameters of the layout of the model.
 * Determines whether deviations, colours and what data are shown on the model.
 * There is a possibility to use any object from the state, however their
 * presence is not guaranteed if requested.
 * 
 * Modes will be automatically triggered if any requested object becomes
 * available (thus, ensure that inputsRequested() always returns the same
 * objects).
 * 
 * VisualisationParameters should work when the unfiltered aligned log is
 * available. The filtered log (and thus, the highlighting) must be taken care
 * of using getVisualisationData.
 * 
 * @author sander
 *
 */
public abstract class Mode {

	private final ProcessTreeVisualisationParameters parametersWithoutAlignments = new ProcessTreeVisualisationParameters();

	public Mode() {
		parametersWithoutAlignments.setColourModelEdges(null);
		parametersWithoutAlignments.setShowFrequenciesOnModelEdges(false);
		parametersWithoutAlignments.setShowFrequenciesOnMoveEdges(false);
		parametersWithoutAlignments.setShowFrequenciesOnNodes(false);
		parametersWithoutAlignments.setModelEdgesWidth(new SizeMapFixed(1));
		parametersWithoutAlignments.setShowLogMoves(false);
		parametersWithoutAlignments.setShowModelMoves(false);
		parametersWithoutAlignments.setColourNodes(new ColourMapFixed(Color.white));
	}

	/**
	 * Perform the computations necessary to visualise something on the model.
	 * Will be called asynchronously, and the result must not be cached.
	 * 
	 * @param model
	 * @param log
	 * @param logInfo
	 * @param queueActivityLogs
	 *            Gives access to activity instance executions and their
	 *            events/moves
	 * @param performance
	 * @return
	 */
	public abstract AlignedLogVisualisationData getVisualisationData(IvMModel model, IvMLogFiltered log,
			IvMLogInfo logInfo, TIntObjectMap<QueueActivityLog> queueActivityLogs, PerformanceWrapper performance);

	public ProcessTreeVisualisationParameters getParametersWithoutAlignments() {
		return parametersWithoutAlignments;
	}

	/**
	 * 
	 * @return The objects that would be handy for this mode. A mode must be
	 *         always available, so there is no guarantee that the objects will
	 *         be provided.
	 */
	public abstract IvMObject<?>[] inputsRequested();

	public abstract boolean isShowDeviations();

	public abstract boolean isUpdateWithTimeStep();

	/**
	 * Note that there is no guarantee that the requested objects will be
	 * provided.
	 * 
	 * @return
	 */
	public abstract ProcessTreeVisualisationParameters getVisualisationParametersWithAlignments(IvMObjectValues inputs);
}
