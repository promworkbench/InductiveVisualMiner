package org.processmining.plugins.inductiveVisualMiner.chain;

import org.processmining.framework.plugin.ProMCanceller;
import org.processmining.plugins.InductiveMiner.Quadruple;
import org.processmining.plugins.InductiveMiner.Triple;
import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.dot.Dot.GraphDirection;
import org.processmining.plugins.graphviz.visualisation.DotPanel;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;
import org.processmining.plugins.inductiveVisualMiner.TraceView.TraceViewColourMap;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.data.AlignedLogVisualisationData;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.data.AlignedLogVisualisationDataImplFrequencies;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogInfo;
import org.processmining.plugins.inductiveVisualMiner.visualisation.ProcessTreeVisualisation;
import org.processmining.plugins.inductiveVisualMiner.visualisation.ProcessTreeVisualisationInfo;
import org.processmining.plugins.inductiveVisualMiner.visualisation.ProcessTreeVisualisationParameters;
import org.processmining.processtree.ProcessTree;

import com.kitfox.svg.SVGDiagram;

// perform layout
public class Cl06LayoutWithAlignment
		extends
		ChainLink<Quadruple<ProcessTree, IvMLogInfo, ProcessTreeVisualisationParameters, GraphDirection>, Quadruple<Dot, SVGDiagram, ProcessTreeVisualisationInfo, TraceViewColourMap>> {

	public Cl06LayoutWithAlignment(ProMCanceller globalCanceller) {
		super(globalCanceller);
	}

	protected Quadruple<ProcessTree, IvMLogInfo, ProcessTreeVisualisationParameters, GraphDirection> generateInput(
			InductiveVisualMinerState state) {
		return Quadruple.of(state.getTree(), state.getIvMLogInfoFiltered(), state.getMode()
				.getVisualisationParameters(state), state.getGraphDirection());
	}

	protected Quadruple<Dot, SVGDiagram, ProcessTreeVisualisationInfo, TraceViewColourMap> executeLink(
			Quadruple<ProcessTree, IvMLogInfo, ProcessTreeVisualisationParameters, GraphDirection> input) {
		//compute dot
		ProcessTreeVisualisation visualiser = new ProcessTreeVisualisation();
		AlignedLogVisualisationData data = new AlignedLogVisualisationDataImplFrequencies(input.getA(), input.getB());
		Triple<Dot, ProcessTreeVisualisationInfo, TraceViewColourMap> p = visualiser.fancy(input.getA(), data,
				input.getC());

		//set the graph direction
		p.getA().setDirection(input.getD());

		//compute svg from dot
		SVGDiagram diagram = DotPanel.dot2svg(p.getA());

		return Quadruple.of(p.getA(), diagram, p.getB(), p.getC());
	}

	protected void processResult(Quadruple<Dot, SVGDiagram, ProcessTreeVisualisationInfo, TraceViewColourMap> result,
			InductiveVisualMinerState state) {
		state.setLayout(result.getA(), result.getB(), result.getC(), result.getD());
	}
}
