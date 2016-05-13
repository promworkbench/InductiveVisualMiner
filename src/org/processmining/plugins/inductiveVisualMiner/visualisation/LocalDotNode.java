package org.processmining.plugins.inductiveVisualMiner.visualisation;

import gnu.trove.map.hash.THashMap;

import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.dot.DotNode;

public class LocalDotNode extends DotNode {

	public class Appearance {
		public String stroke;
		public String strokeWidth;
		public String strokeDashArray;
	}

	public enum NodeType {
		source, sink, activity, xor, concurrentSplit, concurrentJoin, interleavedSplit, interleavedJoin, orSplit, orJoin, logMoveActivity
	}

	private NodeType type;
	private final int node;
	public final Appearance unselectedAppearance = new Appearance();

	public LocalDotNode(Dot dot, ProcessTreeVisualisationInfo info, NodeType type, String label, final int unode) {
		super(label, new THashMap<String, String>());

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
			case concurrentSplit :
			case concurrentJoin :
			case orSplit :
			case orJoin :
			case interleavedSplit :
			case interleavedJoin :
				setOption("shape", "diamond");
				setOption("fixedsize", "true");
				setOption("height", "0.25");
				setOption("width", "0.27");
				break;
			case sink :
				setOption("width", "0.2");
				setOption("shape", "circle");
				setOption("style", "filled");
				setOption("fillcolor", "#E40000");
				break;
			case source :
				setOption("width", "0.2");
				setOption("shape", "circle");
				setOption("style", "filled");
				setOption("fillcolor", "#80ff00");
				break;
			case xor :
				setOption("width", "0.05");
				setOption("shape", "circle");
				break;
		}

		dot.addNode(this);
		info.addNode(unode, this);
	}

	public int getUnode() {
		return node;
	}

	public NodeType getType() {
		return type;
	}

	public void setType(NodeType type) {
		this.type = type;
	}
}