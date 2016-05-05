package org.processmining.plugins.inductiveVisualMiner.editModel;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree;
import org.processmining.plugins.InductiveMiner.efficienttree.UnknownTreeNodeException;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMEfficientTree;

import com.google.common.collect.FluentIterable;

public class TreeLanguage {
	
	public static String toString(IvMEfficientTree tree) {
		return tree2string(tree, 0, tree.getRoot());
	}
	
	public static String tree2string(EfficientTree tree, int indent, int node) {
		StringBuilder result = new StringBuilder();
		result.append(StringUtils.repeat("\t", indent));
		if (tree.isActivity(node)) {
			String name = StringEscapeUtils.escapeCsv(tree.getActivityName(node));
			if (name.startsWith("tau") || name.startsWith("sequence") || name.startsWith("parallel")
					|| name.startsWith("xor") || name.startsWith("interleaved") || name.startsWith("loop")) {
				result.append("\"" + name + "\"");
			} else {
				result.append(name);
			}
		} else if (tree.isTau(node)) {
			result.append("tau");
		} else if (tree.isOperator(node)) {
			if (tree.isSequence(node)) {
				result.append("sequence\n");
			} else if (tree.isConcurrent(node)) {
				result.append("concurrent\n");
			} else if (tree.isXor(node)) {
				result.append("xor\n");
			} else if (tree.isLoop(node)) {
				result.append("loop\n");
			} else if (tree.isOr(node)) {
				result.append("or\n");
			} else if (tree.isInterleaved(node)) {
				result.append("interleaved\n");
			} else {
				throw new UnknownTreeNodeException();
			}

			// filter the list of children: remove superfluous taus from loops
			List<Integer> children = new ArrayList<>(FluentIterable.from(tree.getChildren(node)).toList());
			if (tree.isLoop(node)) {
				if (tree.isTau(children.get(2))) {
					children.remove(2);
					if (tree.isTau(children.get(1))) {
						children.remove(1);
					}
				}
			}

			int i = 0;
			for (int child : children) {
				result.append(tree2string(tree, indent + 1, child));
				i++;
				if (i < children.size()) {
					result.append("\n");
				}
			}
		}
		return result.toString();
	}
}