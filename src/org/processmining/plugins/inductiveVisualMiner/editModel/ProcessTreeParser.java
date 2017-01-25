package org.processmining.plugins.inductiveVisualMiner.editModel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.processmining.plugins.InductiveMiner.Triple;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree;
import org.processmining.plugins.InductiveMiner.efficienttree.UnknownTreeNodeException;
import org.processmining.plugins.inductiveVisualMiner.editModel.ProcessTreeNodiser.NodeType;
@Deprecated
public class ProcessTreeParser {
	public static Triple<EfficientTree, Integer, String> parse(String string, int spacesPerTab) throws IOException {
		ProcessTreeNodiser nodiser = new ProcessTreeNodiser(string, spacesPerTab);
		return parseNode(nodiser);
	}

	/**
	 * Parse the next node using nodiser.
	 * 
	 * @param nodiser
	 * @return A triple, in which the first item denotes the parsed tree. If
	 *         parsing failed, this is null, and the second element contains the
	 *         line number where parsing failed, and the third element contains
	 *         an error message.
	 * @throws IOException
	 */
	public static Triple<EfficientTree, Integer, String> parseNode(ProcessTreeNodiser nodiser) throws IOException {
		if (!nodiser.nextNode()) {
			// we ran out of nodes, this means that something went wrong or the
			// string is empty
			return null;
		}

		int indentation = nodiser.getLastIndentation();
		NodeType nodeType = nodiser.getLastNodeType();

		switch (nodeType) {
			case activity :
				return Triple.of(InlineTree.leaf(nodiser.getLastActivity()), -1, null);
			case tau :
				return Triple.of(InlineTree.tau(), -1, null);
			default :
				break;
		}

		List<EfficientTree> children = new ArrayList<>();

		// parse children
		{
			// first, parse the next token
			boolean hasNextChild = nodiser.nextNode();
			int childIndentation = nodiser.getLastIndentation();
			// leave the token to the child (push back the tokeniser)
			if (hasNextChild) {
				nodiser.pushBack();
			}

			while (hasNextChild && childIndentation > indentation) {
				Triple<EfficientTree, Integer, String> childResult = parseNode(nodiser);
				if (childResult.getA() == null) {
					return childResult;
				}
				children.add(childResult.getA());

				hasNextChild = nodiser.nextNode();
				childIndentation = nodiser.getLastIndentation();
				if (hasNextChild) {
					nodiser.pushBack();
				}
			}
		}

		//recurse
		{
			switch (nodeType) {
				case interleaving :
					if (children.size() < 1) {
						return Triple.of(null, nodiser.getLastLineNumber(),
								"An interleaved node must have at lease one child.");
					}
					return Triple.of(InlineTree.interleaved(children), -1, null);
				case loop :
					if (children.size() < 1) {
						return Triple
								.of(null, nodiser.getLastLineNumber(), "A loop node must have at least one child.");
					}
					// allow loops with less than 3 children
					while (children.size() < 3) {
						children.add(InlineTree.tau());
					}
					return Triple.of(InlineTree.loop(children), -1, null);
				case or :
					if (children.size() < 1) {
						return Triple.of(null, nodiser.getLastLineNumber(),
								"An inclusive choice node must have at lease one child.");
					}
					return Triple.of(InlineTree.or(children), -1, null);
				case concurrent :
					if (children.size() < 1) {
						return Triple.of(null, nodiser.getLastLineNumber(),
								"A concurrent node must have at lease one child.");
					}
					return Triple.of(InlineTree.concurrent(children), -1, null);
				case sequence :
					if (children.size() < 1) {
						return Triple.of(null, nodiser.getLastLineNumber(),
								"A sequence node must have at lease one child.");
					}
					return Triple.of(InlineTree.sequence(children), -1, null);
				case xor :
					if (children.size() < 1) {
						return Triple.of(null, nodiser.getLastLineNumber(),
								"An exclusive choice node must have at lease one child.");
					}
					return Triple.of(InlineTree.xor(children), -1, null);
				default :
					throw new UnknownTreeNodeException();
			}
		}
	}
}
