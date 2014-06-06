package org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation;

import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.dot.DotNode;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.UnfoldedNode;

public class LocalDotNode extends DotNode {

	public class Appearance {
		public String stroke;
		public String strokeWidth;
		public String strokeDashArray;
	}
	
	public enum NodeType {
		source, sink, activity, xor, parallelSplit, parallelJoin, logMoveActivity
	}
	
	private NodeType type;
	private final UnfoldedNode node;
	public final Appearance unselectedAppearance = new Appearance();

	public LocalDotNode(Dot dot, AlignedLogVisualisationInfo info, NodeType type, String label, final UnfoldedNode unode) {
		super(label, "");
		
		this.node = unode;
		this.setType(type);

		switch (type) {
			case activity :
				setOptions("shape=\"box\", style=\"rounded,filled\", fontsize=9");
				break;
			case logMoveActivity :
				setOptions("shape=\"box\", style=\"rounded,filled\", fontsize=9, fillcolor=\"red\"");
				break;
			case parallelSplit :
			case parallelJoin:
				setOptions("shape=\"diamond\", fixedsize=true, height=0.25, width=0.27");
				break;
			case sink :
				setOptions("width=0.2, shape=\"circle\", style=filled, fillcolor=\"red\"");
				break;
			case source :
				setOptions("width=0.2, shape=\"circle\", style=filled, fillcolor=\"green\"");
				break;
			case xor :
				setOptions("width=0.05, shape=\"circle\"");
				break;
		}
		
		dot.addNode(this);
		info.addNode(unode, this);
	}

	public UnfoldedNode getUnode() {
		return node;
	}

	public NodeType getType() {
		return type;
	}

	public void setType(NodeType type) {
		this.type = type;
	}
}