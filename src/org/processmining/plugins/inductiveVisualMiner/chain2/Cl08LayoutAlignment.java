package org.processmining.plugins.inductiveVisualMiner.chain2;

import org.processmining.plugins.InductiveMiner.Quadruple;
import org.processmining.plugins.InductiveMiner.Triple;
import org.processmining.plugins.InductiveMiner.efficienttree.UnknownTreeNodeException;
import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.visualisation.DotPanel;
import org.processmining.plugins.graphviz.visualisation.DotPanelUserSettings;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.data.AlignedLogVisualisationData;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.data.AlignedLogVisualisationDataImplFrequencies;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMCanceller;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMEfficientTree;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogInfo;
import org.processmining.plugins.inductiveVisualMiner.traceview.TraceViewEventColourMap;
import org.processmining.plugins.inductiveVisualMiner.visualisation.ProcessTreeVisualisation;
import org.processmining.plugins.inductiveVisualMiner.visualisation.ProcessTreeVisualisationInfo;
import org.processmining.plugins.inductiveVisualMiner.visualisation.ProcessTreeVisualisationParameters;

import com.kitfox.svg.SVGDiagram;

public class Cl08LayoutAlignment extends
		ChainLink2<Quadruple<IvMEfficientTree, IvMLogInfo, ProcessTreeVisualisationParameters, DotPanelUserSettings>, Quadruple<Dot, SVGDiagram, ProcessTreeVisualisationInfo, TraceViewEventColourMap>> {

	protected Quadruple<IvMEfficientTree, IvMLogInfo, ProcessTreeVisualisationParameters, DotPanelUserSettings> generateInput(
			InductiveVisualMinerState state) {
		return Quadruple.of(state.getTree(), state.getIvMLogInfoFiltered(),
				state.getMode().getVisualisationParameters(state), state.getGraphUserSettings());
	}

	protected Quadruple<Dot, SVGDiagram, ProcessTreeVisualisationInfo, TraceViewEventColourMap> executeLink(
			Quadruple<IvMEfficientTree, IvMLogInfo, ProcessTreeVisualisationParameters, DotPanelUserSettings> input,
			IvMCanceller canceller) throws UnknownTreeNodeException {
		IvMEfficientTree tree = input.getA();

		//compute dot
		ProcessTreeVisualisation visualiser = new ProcessTreeVisualisation();
		AlignedLogVisualisationData data = new AlignedLogVisualisationDataImplFrequencies(tree, input.getB());
		Triple<Dot, ProcessTreeVisualisationInfo, TraceViewEventColourMap> p = visualiser.fancy(tree, data,
				input.getC());

		//keep the user settings of the dot panel
		input.getD().applyToDot(p.getA());

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
}
