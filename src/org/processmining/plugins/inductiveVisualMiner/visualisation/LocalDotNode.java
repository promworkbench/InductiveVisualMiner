package org.processmining.plugins.inductiveVisualMiner.visualisation;

import java.util.HashMap;

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
		source, sink, activity, xor, parallelSplit, parallelJoin, interleavedSplit, interleavedJoin, logMoveActivity
	}
	
	private NodeType type;
	private final UnfoldedNode node;
	public final Appearance unselectedAppearance = new Appearance();

	public LocalDotNode(Dot dot, ProcessTreeVisualisationInfo info, NodeType type, String label, final UnfoldedNode unode) {
		super(label, new HashMap<String, String>());
		
		this.node = unode;
		this.setType(type);

		switch (type) {
			case activity :
				setOption("shape", "box");
				setOption("style", "rounded,filled");
				setOption("fontsize", "12");
				break;
			case logMoveActivity :
				setOption("shape", "box");
				setOption("style", "rounded,filled");
				setOption("fontsize", "9");
				setOption("fillcolor", "red");
				break;
			case parallelSplit :
			case parallelJoin:
			case interleavedSplit :
			case interleavedJoin:
				setOption("shape", "diamond");
				setOption("fixedsize", "true");
				setOption("height", "0.25");
				setOption("width", "0.27");
				break;
			case sink :
				setOption("width", "0.2");
				setOption("shape", "circle");
				setOption("style", "filled");
				setOption("fillcolor", "red");
				break;
			case source :
				setOption("width", "0.2");
				setOption("shape", "circle");
				setOption("style", "filled");
				setOption("fillcolor", "green");
				break;
			case xor :
				setOption("width", "0.05");
				setOption("shape", "circle");
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