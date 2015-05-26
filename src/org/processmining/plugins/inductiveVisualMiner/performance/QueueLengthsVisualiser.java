package org.processmining.plugins.inductiveVisualMiner.performance;

import org.processmining.plugins.graphviz.visualisation.DotPanel;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerPanel;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.AlignedLogVisualisationInfo;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.LocalDotNode;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.TreeUtils;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.Task.Manual;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.UnfoldedNode;

import com.kitfox.svg.Group;
import com.kitfox.svg.SVGDiagram;
import com.kitfox.svg.SVGException;
import com.kitfox.svg.Text;

public class QueueLengthsVisualiser {

	/**
	 * Draw the queues in the visualisation.
	 * 
	 * @param panel
	 * @param lengths
	 * @throws SVGException
	 */
	public static void drawQueues(SVGDiagram svg, ProcessTree tree, AlignedLogVisualisationInfo info,
			QueueLengths lengths, long time) throws SVGException {
		for (UnfoldedNode unode : TreeUtils.unfoldAllNodes(new UnfoldedNode(tree.getRoot()))) {
			if (unode.getNode() instanceof Manual) {
				drawQueue(svg, info, unode, lengths.getQueueLength(unode, time));
			}
		}
	}

	private static void drawQueue(SVGDiagram svg, AlignedLogVisualisationInfo info, UnfoldedNode unode, double length)
			throws SVGException {
		LocalDotNode dotNode = info.getActivityNode(unode);

		Group group = DotPanel.getSVGElementOf(svg, dotNode);
		Text titleCount = (Text) group.getChild(group.getChildren(null).size() - 1);

		titleCount.getContent().clear();
		titleCount.getContent().add(length + "");
		titleCount.rebuild();
	}

	/**
	 * Remove all queues from view.
	 * 
	 * @param panel
	 */
	public static void resetQueues(InductiveVisualMinerPanel panel) {

	}
}
