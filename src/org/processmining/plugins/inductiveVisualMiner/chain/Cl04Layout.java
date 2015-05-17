package org.processmining.plugins.inductiveVisualMiner.chain;

import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.InductiveMiner.Quadruple;
import org.processmining.plugins.InductiveMiner.Triple;
import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.dot.Dot.GraphDirection;
import org.processmining.plugins.graphviz.visualisation.DotPanel;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerPanel;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.AlignedLogVisualisation;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.AlignedLogVisualisationInfo;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.AlignedLogVisualisationParameters;
import org.processmining.plugins.inductiveVisualMiner.alignment.AlignedLogInfo;
import org.processmining.processtree.ProcessTree;

import com.kitfox.svg.SVGDiagram;

// perform layout
public class Cl04Layout
		extends
		ChainLink<Quadruple<ProcessTree, AlignedLogInfo, AlignedLogVisualisationParameters, GraphDirection>, Triple<Dot, SVGDiagram, AlignedLogVisualisationInfo>> {

	protected Quadruple<ProcessTree, AlignedLogInfo, AlignedLogVisualisationParameters, GraphDirection> generateInput(
			InductiveVisualMinerState state) {
		AlignedLogVisualisationParameters parameters = InductiveVisualMinerPanel.getViewParameters(state);
		return Quadruple.of(state.getTree(), state.getAlignedFilteredLogInfo(), parameters, state.getGraphDirection());
	}

	protected Triple<Dot, SVGDiagram, AlignedLogVisualisationInfo> executeLink(
			Quadruple<ProcessTree, AlignedLogInfo, AlignedLogVisualisationParameters, GraphDirection> input) {
		//compute dot
		AlignedLogVisualisation visualiser = new AlignedLogVisualisation();
		Pair<Dot, AlignedLogVisualisationInfo> p = visualiser.fancy(input.getA(), input.getB(), input.getC());

		//set the graph direction
		p.getA().setDirection(input.getD());

		//compute svg from dot
		SVGDiagram diagram = DotPanel.dot2svg(p.getA());

		return Triple.of(p.getA(), diagram, p.getB());
	}

	protected void processResult(Triple<Dot, SVGDiagram, AlignedLogVisualisationInfo> result,
			InductiveVisualMinerState state) {
		state.setLayout(result.getA(), result.getB(), result.getC());

	}
}
