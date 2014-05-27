package org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import org.processmining.plugins.graphviz.dot.DotNode;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.AlignedLogVisualisation.NodeType;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.UnfoldedNode;

public class LocalDotNode extends DotNode {

	/**
	 * 
	 */
	private final AlignedLogVisualisation alignedLogVisualisation;
	public NodeType type;
	public final UnfoldedNode node;

	public LocalDotNode(AlignedLogVisualisation alignedLogVisualisation, NodeType type, String label, final UnfoldedNode unode) {
		super(label, "");
		this.alignedLogVisualisation = alignedLogVisualisation;
		this.alignedLogVisualisation.dot.addNode(this);
		if (this.alignedLogVisualisation.unfoldedNode2dotNodes.get(unode) == null) {
			this.alignedLogVisualisation.unfoldedNode2dotNodes.put(unode, new ArrayList<LocalDotNode>());
		}
		this.alignedLogVisualisation.unfoldedNode2dotNodes.get(unode).add(this);
		this.alignedLogVisualisation.dotNodes.add(this);
		this.node = unode;
		this.type = type;

		switch (type) {
			case activity :
				setOptions("shape=\"box\", style=\"rounded,filled\", fontsize=9");
				break;
			case logMoveActivity :
				setOptions("shape=\"box\", style=\"rounded,filled\", fontsize=9, fillcolor=\"red\"");
				break;
			case parallel :
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

		if (this.alignedLogVisualisation.parameters.isAddOnClick()) {
			addMouseListener(new MouseListener() {

				public void mouseReleased(MouseEvent arg0) {
				}

				public void mousePressed(MouseEvent arg0) {

				}

				public void mouseExited(MouseEvent arg0) {
				}

				public void mouseEntered(MouseEvent arg0) {
				}

				public void mouseClicked(MouseEvent arg0) {

				}
			});
		}
	}
}