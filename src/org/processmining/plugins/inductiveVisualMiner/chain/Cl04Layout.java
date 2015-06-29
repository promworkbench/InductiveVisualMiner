package org.processmining.plugins.inductiveVisualMiner.chain;

import org.processmining.plugins.InductiveMiner.Quadruple;
import org.processmining.plugins.InductiveMiner.Triple;
import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.dot.Dot.GraphDirection;
import org.processmining.plugins.graphviz.visualisation.DotPanel;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;
import org.processmining.plugins.inductiveVisualMiner.TraceView.TraceViewColourMap;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.AlignedLogVisualisation;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.AlignedLogVisualisationData;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.AlignedLogVisualisationDataImplEmpty;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.AlignedLogVisualisationInfo;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.AlignedLogVisualisationParameters;
import org.processmining.plugins.inductiveVisualMiner.alignment.AlignedLogInfo;
import org.processmining.processtree.ProcessTree;

import com.kitfox.svg.SVGDiagram;

// perform layout
public class Cl04Layout
		extends
		ChainLink<Quadruple<ProcessTree, AlignedLogInfo, AlignedLogVisualisationParameters, GraphDirection>, Quadruple<Dot, SVGDiagram, AlignedLogVisualisationInfo, TraceViewColourMap>> {

	protected Quadruple<ProcessTree, AlignedLogInfo, AlignedLogVisualisationParameters, GraphDirection> generateInput(
			InductiveVisualMinerState state) {
		return Quadruple.of(state.getTree(), state.getAlignedFilteredLogInfo(), state.getColourMode()
				.getVisualisationParameters(state), state.getGraphDirection());
	}

	protected Quadruple<Dot, SVGDiagram, AlignedLogVisualisationInfo, TraceViewColourMap> executeLink(
			Quadruple<ProcessTree, AlignedLogInfo, AlignedLogVisualisationParameters, GraphDirection> input) {
		//compute dot
		AlignedLogVisualisation visualiser = new AlignedLogVisualisation();
		AlignedLogVisualisationData data = new AlignedLogVisualisationDataImplEmpty();
		Triple<Dot, AlignedLogVisualisationInfo, TraceViewColourMap> p = visualiser.fancy(input.getA(), data, input.getC());

		//set the graph direction
		p.getA().setDirection(input.getD());

		//compute svg from dot
		SVGDiagram diagram = DotPanel.dot2svg(p.getA());

		return Quadruple.of(p.getA(), diagram, p.getB(), p.getC());
	}

	protected void processResult(Quadruple<Dot, SVGDiagram, AlignedLogVisualisationInfo, TraceViewColourMap> result,
			InductiveVisualMinerState state) {
		state.setLayout(result.getA(), result.getB(), result.getC(), result.getD());

	}
}
