package org.processmining.plugins.inductiveVisualMiner.helperClasses;

import java.io.File;

import org.processmining.plugins.InductiveMiner.dfgOnly.Dfg;
import org.processmining.plugins.InductiveMiner.dfgOnly.DfgMinerState;
import org.processmining.plugins.InductiveMiner.dfgOnly.dfgFallThrough.DfgFallThrough;
import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.dot.Dot2Image;
import org.processmining.plugins.graphviz.dot.Dot2Image.Type;
import org.processmining.processtree.Node;
import org.processmining.processtree.ProcessTree;

public class DfgFallThroughSaveDfg implements DfgFallThrough {
	
	private final File directory;
	private int number = 0;

	public DfgFallThroughSaveDfg(File directory) {
		this.directory = directory;
	}
	
	public Node fallThrough(Dfg dfg, ProcessTree tree, DfgMinerState minerState) {
		System.out.println("save");
		Dot dot = GraphvizDirectlyFollowsGraph.dfg2Dot(dfg, false);
		Dot2Image.dot2image(dot, new File(directory, "dfg " + number + ".svg"), Type.svg);
		number++;
		
		return null;
	}
	
}
