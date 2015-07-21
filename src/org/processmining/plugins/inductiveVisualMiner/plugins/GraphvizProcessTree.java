package org.processmining.plugins.inductiveVisualMiner.plugins;

import javax.swing.JComponent;

import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.plugins.InductiveMiner.mining.interleaved.Interleaved;
import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.dot.Dot.GraphDirection;
import org.processmining.plugins.graphviz.dot.DotNode;
import org.processmining.plugins.graphviz.visualisation.DotPanel;
import org.processmining.processtree.Node;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.impl.AbstractBlock;
import org.processmining.processtree.impl.AbstractTask;
//import org.processmining.plugins.InductiveMiner.mining.operators.Interleaved;

@Plugin(name = "Graphviz process tree visualisation", returnLabels = { "Dot visualization" }, returnTypes = { JComponent.class }, parameterLabels = { "Process Tree" }, userAccessible = false)
@Visualizer
public class GraphvizProcessTree {

	@PluginVariant(requiredParameterLabels = { 0 })
	public JComponent visualize(PluginContext context, ProcessTree tree) throws NotYetImplementedException {
		return new DotPanel(convert(tree));
	}

	public static Dot convert(ProcessTree tree) throws NotYetImplementedException {

		Dot dot = new Dot();
		dot.setDirection(GraphDirection.topDown);
		dot.setOption("splines", "false");

		convertNode(dot, null, tree.getRoot());

		return dot;
	}

	public static class NotYetImplementedException extends Exception {
		private static final long serialVersionUID = 1007081393798261074L;
	}

	private static void convertNode(Dot dot, DotNode parent, Node node) throws NotYetImplementedException {
		if (node instanceof AbstractTask.Automatic) {
			convertTau(dot, parent, (AbstractTask.Automatic) node);
		} else if (node instanceof AbstractTask.Manual) {
			convertTask(dot, parent, (AbstractTask.Manual) node);
		} else if (node instanceof Interleaved) {
			convertInterleaved(dot, parent, (Interleaved) node);
		} else if (node instanceof AbstractBlock.And) {
			convertAnd(dot, parent, (AbstractBlock.And) node);
		} else if (node instanceof AbstractBlock.Seq) {
			convertSeq(dot, parent, (AbstractBlock.Seq) node);
		} else if (node instanceof AbstractBlock.Xor) {
			convertXor(dot, parent, (AbstractBlock.Xor) node);
		} else if (node instanceof AbstractBlock.XorLoop) {
			convertXorLoop(dot, parent, (AbstractBlock.XorLoop) node);
		} else if (node instanceof AbstractBlock.Or) {
			convertOr(dot, parent, (AbstractBlock.Or) node);
		} else if (node instanceof AbstractBlock.Def) {
			convertDeferredChoice(dot, parent, (AbstractBlock.Def) node);
		} else if (node instanceof AbstractBlock.DefLoop) {
			convertDeferredLoop(dot, parent, (AbstractBlock.DefLoop) node);
		} else {
			throw new NotYetImplementedException();
		}
	}

	private static void convertBinOperator(Dot dot, DotNode parent, AbstractBlock node, String label)
			throws NotYetImplementedException {
		DotNode dotNode = dot.addNode(label);
		addArc(dot, parent, dotNode);
		for (Node child : node.getChildren()) {
			convertNode(dot, dotNode, child);
		}
	}

	private static void convertTau(Dot dot, DotNode parent, AbstractTask.Automatic node) {
		DotNode dotNode = dot.addNode("");
		dotNode.setOption("shape", "point");
		addArc(dot, parent, dotNode);
	}

	private static void convertTask(Dot dot, DotNode parent, AbstractTask.Manual node) {
		DotNode dotNode = dot.addNode(node.getName());
		addArc(dot, parent, dotNode);
	}

	private static void convertInterleaved(Dot dot, DotNode parent, Interleaved node) throws NotYetImplementedException {
		convertBinOperator(dot, parent, node, "int");
	}

	private static void convertAnd(Dot dot, DotNode parent, AbstractBlock.And node) throws NotYetImplementedException {
		convertBinOperator(dot, parent, node, "and");
	}

	private static void convertSeq(Dot dot, DotNode parent, AbstractBlock.Seq node) throws NotYetImplementedException {
		convertBinOperator(dot, parent, node, "seq");
	}

	private static void convertXor(Dot dot, DotNode parent, AbstractBlock.Xor node) throws NotYetImplementedException {
		convertBinOperator(dot, parent, node, "xor");
	}

	private static void convertXorLoop(Dot dot, DotNode parent, AbstractBlock.XorLoop node)
			throws NotYetImplementedException {
		convertBinOperator(dot, parent, node, "xor loop");
	}

	private static void convertOr(Dot dot, DotNode parent, AbstractBlock.Or node) throws NotYetImplementedException {
		convertBinOperator(dot, parent, node, "or");
	}

	private static void convertDeferredChoice(Dot dot, DotNode parent, AbstractBlock.Def node)
			throws NotYetImplementedException {
		convertBinOperator(dot, parent, node, "def choice");
	}

	private static void convertDeferredLoop(Dot dot, DotNode parent, AbstractBlock.DefLoop node)
			throws NotYetImplementedException {
		convertBinOperator(dot, parent, node, "def loop");
	}

	private static void addArc(Dot dot, DotNode parent, DotNode child) {
		if (parent != null) {
			dot.addEdge(parent, child, "").setOption("arrowhead", "none");
		}
	}

}
