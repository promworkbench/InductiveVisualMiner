package org.processmining.plugins.inductiveVisualMiner.mode;

import java.awt.Color;
import java.util.Arrays;
import java.util.Set;

import org.processmining.plugins.graphviz.colourMaps.ColourMapFixed;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.data.AlignedLogVisualisationData;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMObject;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMObjectValues;
import org.processmining.plugins.inductiveVisualMiner.configuration.InductiveVisualMinerConfiguration;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.sizeMaps.SizeMapFixed;
import org.processmining.plugins.inductiveVisualMiner.visualisation.ProcessTreeVisualisationParameters;

import gnu.trove.set.hash.THashSet;

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

	private IvMObject<?>[] visualisationDataOptionalObjects;

	private IvMObject<?>[] optionalObjects;

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

	public ProcessTreeVisualisationParameters getParametersWithoutAlignments() {
		return parametersWithoutAlignments;
	}

	/**
	 * 
	 * @return The objects that would be handy for this mode. A mode must be
	 *         always available, so there is no guarantee that the objects will
	 *         be provided.
	 */
	public final IvMObject<?>[] getOptionalObjects() {
		if (optionalObjects == null) {
			optionalObjects = createOptionalObjects();
		}
		return optionalObjects;
	}

	/**
	 * 
	 * @return The objects that would be handy for this mode. A mode must be
	 *         always available, so there is no guarantee that the objects will
	 *         be provided.
	 */
	protected abstract IvMObject<?>[] createOptionalObjects();

	public abstract boolean isShowDeviations();

	/**
	 * Note that there is no guarantee that the requested objects will be
	 * provided: a mode has to always work. However, alignments can be assumed
	 * to have finished. That is, IvMObject.model and
	 * IvMObject.aligned_log_info_filtered will be available (though not the
	 * filtered variants).
	 * 
	 * @return
	 */
	public abstract ProcessTreeVisualisationParameters getVisualisationParametersWithAlignments(IvMObjectValues inputs);

	public static IvMObject<?>[] gatherInputsRequested(InductiveVisualMinerConfiguration configuration) {
		Set<IvMObject<?>> result = new THashSet<>();

		for (Mode mode : configuration.getModes()) {
			result.addAll(Arrays.asList(mode.getOptionalObjects()));
		}

		IvMObject<?>[] arr = new IvMObject<?>[result.size()];
		return result.toArray(arr);
	}

	/**
	 * Perform the computations necessary to visualise something on the model.
	 * Will be called asynchronously, and the result must not be cached. There
	 * is no guarantee that the requested inputs will be available: the
	 * visualisation data has to always work. However, IvMObject.model and
	 * IvMObject.aligned_log_info_filtered will be available but must still be
	 * requested.
	 * 
	 * 
	 * @param inputs
	 * @return
	 */
	public abstract AlignedLogVisualisationData getVisualisationData(IvMObjectValues inputs);

	/**
	 * @return Whether the visualisation data should be updated every time the
	 *         animation takes a time step.
	 */
	public abstract boolean isVisualisationDataUpdateWithTimeStep();

	/**
	 * 
	 * @return The objects that would be handy for this visualisation data.
	 *         Visualisation data must be always available, so there is no
	 *         guarantee that the objects will be provided. However,
	 *         IvMObject.model and IvMObject.aligned_log_info_filtered will be
	 *         available but must still be requested.
	 */
	protected abstract IvMObject<?>[] createVisualisationDataOptionalObjects();

	/**
	 * 
	 * @return The objects that would be handy for this visualisation data.
	 *         Visualisation data must be always available, so there is no
	 *         guarantee that the objects will be provided. However,
	 *         IvMObject.model and IvMObject.aligned_log_info_filtered will be
	 *         available but must still be requested.
	 */
	public final IvMObject<?>[] getVisualisationDataOptionalObjects() {
		if (visualisationDataOptionalObjects == null) {
			visualisationDataOptionalObjects = createVisualisationDataOptionalObjects();
		}
		return visualisationDataOptionalObjects;
	}

}
