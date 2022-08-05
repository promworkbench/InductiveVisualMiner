package org.processmining.plugins.inductiveVisualMiner.causal.mvpc;

public class DiGraph implements Cloneable {

	private char[][] edges;

	public void add_nodes_from(int numberOfNodes) {
		assert edges == null;
		edges = new char[numberOfNodes][numberOfNodes];
	}

	public void add_edge(int i, int j, char c) {
		edges[i][j] = c;
	}

	public DiGraph clone() {
		DiGraph r = null;
		try {
			r = (DiGraph) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}

		r.edges = new char[edges.length][edges.length];
		for (int i = 0; i < edges.length; i++) {
			for (int j = 0; j < edges[0].length; j++) {
				r.edges[i][j] = edges[i][j];
			}
		}

		return r;
	}
}