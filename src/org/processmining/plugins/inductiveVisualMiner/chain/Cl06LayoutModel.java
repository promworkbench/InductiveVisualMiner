package org.processmining.plugins.inductiveVisualMiner.chain;

import org.processmining.plugins.InductiveMiner.Quadruple;
import org.processmining.plugins.InductiveMiner.Triple;
import org.processmining.plugins.InductiveMiner.efficienttree.UnknownTreeNodeException;
import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.visualisation.DotPanel;
import org.processmining.plugins.graphviz.visualisation.DotPanelUserSettings;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.data.AlignedLogVisualisationData;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.data.AlignedLogVisualisationDataImplEmpty;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.plugins.inductiveVisualMiner.traceview.TraceViewEventColourMap;
import org.processmining.plugins.inductiveVisualMiner.visualisation.DfmVisualisation;
import org.processmining.plugins.inductiveVisualMiner.visualisation.ProcessTreeVisualisation;
import org.processmining.plugins.inductiveVisualMiner.visualisation.ProcessTreeVisualisationInfo;
import org.processmining.plugins.inductiveVisualMiner.visualisation.ProcessTreeVisualisationParameters;

import com.kitfox.svg.SVGDiagram;

public class Cl06LayoutModel extends
		IvMChainLink<Triple<IvMModel, ProcessTreeVisualisationParameters, DotPanelUserSettings>, Quadruple<Dot, SVGDiagram, ProcessTreeVisualisationInfo, TraceViewEventColourMap>> {

	protected Triple<IvMModel, ProcessTreeVisualisationParameters, DotPanelUserSettings> generateInput(
			InductiveVisualMinerState state) {
		return Triple.of(state.getModel(), state.getMode().getVisualisationParameters(state),
				state.getGraphUserSettings());
	}

	protected Quadruple<Dot, SVGDiagram, ProcessTreeVisualisationInfo, TraceViewEventColourMap> executeLink(
			Triple<IvMModel, ProcessTreeVisualisationParameters, DotPanelUserSettings> input, IvMCanceller canceller)
			throws UnknownTreeNodeException {
		//compute dot
		AlignedLogVisualisationData data = new AlignedLogVisualisationDataImplEmpty();

		IvMModel model = input.getA();
		Triple<Dot, ProcessTreeVisualisationInfo, TraceViewEventColourMap> p;
		if (model.isTree()) {
			ProcessTreeVisualisation visualiser = new ProcessTreeVisualisation();
			p = visualiser.fancy(model, data, input.getB());
		} else {
			DfmVisualisation visualiser = new DfmVisualisation();
			p = visualiser.fancy(model, data, input.getB());
		}

		//set the graph direction
		input.getC().applyToDot(p.getA());

		//compute svg from dot
		SVGDiagram diagram = DotPanel.dot2svg(p.getA());

		return Quadruple.of(p.getA(), diagram, p.getB(), p.getC());
	}

	protected void processResult(
			Quadruple<Dot, SVGDiagram, ProcessTreeVisualisationInfo, TraceViewEventColourMap> result,
			InductiveVisualMinerState state) {
		state.setLayout(result.getA(), result.getB(), result.getC(), result.getD());
	}

	protected void invalidateResult(InductiveVisualMinerState state) {
		state.setLayout(null, null, null, null);
	}

	public String getName() {
		return "layout model";
	}

	public String getStatusBusyMessage() {
		return "Layouting model..";
	}
}
