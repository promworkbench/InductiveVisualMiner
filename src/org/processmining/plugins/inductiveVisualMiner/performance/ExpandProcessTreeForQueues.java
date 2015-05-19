package org.processmining.plugins.inductiveVisualMiner.performance;

import gnu.trove.map.hash.THashMap;

import java.util.Map;

import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.processtree.Block;
import org.processmining.processtree.Block.And;
import org.processmining.processtree.Block.Def;
import org.processmining.processtree.Block.DefLoop;
import org.processmining.processtree.Block.Seq;
import org.processmining.processtree.Block.Xor;
import org.processmining.processtree.Block.XorLoop;
import org.processmining.processtree.Node;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.Task.Automatic;
import org.processmining.processtree.Task.Manual;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.UnfoldedNode;
import org.processmining.processtree.impl.ProcessTreeImpl;

public class ExpandProcessTreeForQueues {
	public static final String enqueue = "enqueue";
	public static final String start = "start";
	public static final String complete = "complete";

	/**
	 * Expands a collapsed process tree, i.e. transforms all leaves a into
	 * seq(xor(tau, a+enqueue), xor(tau, a+start), a+complete).
	 * 
	 * @param tree
	 * @return
	 */
	public static Pair<ProcessTree, Map<UnfoldedNode, UnfoldedNode>> expand(ProcessTree tree) {
		ProcessTree newTree = new ProcessTreeImpl();
		Map<UnfoldedNode, UnfoldedNode> mapping = new THashMap<>();
		newTree.setRoot(expand(new UnfoldedNode(tree.getRoot()), newTree, mapping, null));
		return Pair.of(newTree, mapping);
	}

	private static Node expand(UnfoldedNode unode, ProcessTree newTree, Map<UnfoldedNode, UnfoldedNode> mapping,
			UnfoldedNode newParent) {
		if (unode.getNode() instanceof Block) {
			//copy blocks
			Block newNode;
			if (unode.getNode() instanceof Seq) {
				newNode = new org.processmining.processtree.impl.AbstractBlock.Seq(unode.getNode().getName());
			} else if (unode.getNode() instanceof And) {
				newNode = new org.processmining.processtree.impl.AbstractBlock.And(unode.getNode().getName());
			} else if (unode.getNode() instanceof XorLoop) {
				newNode = new org.processmining.processtree.impl.AbstractBlock.XorLoop(unode.getNode().getName());
			} else if (unode.getNode() instanceof DefLoop) {
				newNode = new org.processmining.processtree.impl.AbstractBlock.DefLoop(unode.getNode().getName());
			} else if (unode.getNode() instanceof Xor) {
				newNode = new org.processmining.processtree.impl.AbstractBlock.Xor(unode.getNode().getName());
			} else if (unode.getNode() instanceof Def) {
				newNode = new org.processmining.processtree.impl.AbstractBlock.Def(unode.getNode().getName());
			} else {
				throw new RuntimeException("construct not implemented");
			}
			newNode.setProcessTree(newTree);
			newTree.addNode(newNode);

			UnfoldedNode childParent;
			if (newParent == null) {
				childParent = new UnfoldedNode(newNode);
			} else {
				childParent = newParent.unfoldChild(newNode);
			}

			//process children
			for (Node child : unode.getBlock().getChildren()) {
				newNode.addChild(expand(unode.unfoldChild(child), newTree, mapping, childParent));
			}

			mapping.put(childParent, unode);

			return newNode;
		} else if (unode.getNode() instanceof Automatic) {
			//copy tau
			Node newNode = new org.processmining.processtree.impl.AbstractTask.Automatic("tau");
			newNode.setProcessTree(newTree);
			newTree.addNode(newNode);

			UnfoldedNode childParent;
			if (newParent == null) {
				childParent = new UnfoldedNode(newNode);
			} else {
				childParent = newParent.unfoldChild(newNode);
			}
			mapping.put(childParent, unode);

			return newNode;
		} else if (unode.getNode() instanceof Manual) {
			//transform activity into seq(xor(start, tau), complete)

			Seq seq = new org.processmining.processtree.impl.AbstractBlock.Seq("performance seq");
			seq.setProcessTree(newTree);
			newTree.addNode(seq);
			UnfoldedNode childParent;
			if (newParent == null) {
				childParent = new UnfoldedNode(seq);
			} else {
				childParent = newParent.unfoldChild(seq);
			}
			mapping.put(childParent, unode);

			{
				Xor xor = new org.processmining.processtree.impl.AbstractBlock.Xor("performance xor enqueue");
				xor.setProcessTree(newTree);
				newTree.addNode(xor);
				seq.addChild(xor);
				mapping.put(childParent.unfoldChild(xor), unode);

				Automatic tau = new org.processmining.processtree.impl.AbstractTask.Automatic("tau");
				tau.setProcessTree(newTree);
				newTree.addNode(tau);
				xor.addChild(tau);
				mapping.put(childParent.unfoldChild(xor).unfoldChild(tau), unode);

				Manual aStart = new org.processmining.processtree.impl.AbstractTask.Manual(unode.getNode().getName()
						+ "+" + enqueue);
				aStart.setProcessTree(newTree);
				newTree.addNode(aStart);
				xor.addChild(aStart);
				mapping.put(childParent.unfoldChild(xor).unfoldChild(aStart), unode);
			}
			
			{
				Xor xor = new org.processmining.processtree.impl.AbstractBlock.Xor("performance xor start");
				xor.setProcessTree(newTree);
				newTree.addNode(xor);
				seq.addChild(xor);
				mapping.put(childParent.unfoldChild(xor), unode);

				Automatic tau = new org.processmining.processtree.impl.AbstractTask.Automatic("tau");
				tau.setProcessTree(newTree);
				newTree.addNode(tau);
				xor.addChild(tau);
				mapping.put(childParent.unfoldChild(xor).unfoldChild(tau), unode);

				Manual aStart = new org.processmining.processtree.impl.AbstractTask.Manual(unode.getNode().getName()
						+ "+" + start);
				aStart.setProcessTree(newTree);
				newTree.addNode(aStart);
				xor.addChild(aStart);
				mapping.put(childParent.unfoldChild(xor).unfoldChild(aStart), unode);
			}

			Manual aComplete = new org.processmining.processtree.impl.AbstractTask.Manual(unode.getNode().getName()
					+ "+" + complete);
			aComplete.setProcessTree(newTree);
			newTree.addNode(aComplete);
			seq.addChild(aComplete);
			mapping.put(childParent.unfoldChild(aComplete), unode);

			return seq;
		} else {
			throw new RuntimeException("construct not implemented");
		}
	}
}
