package org.processmining.plugins.inductiveVisualMiner.causal.mvpc;

public class Graph implements Cloneable {

	int[][] edges;

	public void add_nodes_from(int numberOfNodes) {
		assert edges == null;
		edges = new int[numberOfNodes][numberOfNodes];
	}

	public void add_edge(int i, int j, char c) {
		edges[i][j] = 1;
	}

	public Graph clone() {
		Graph r = null;
		try {
			r = (Graph) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}

		r.edges = new int[edges.length][edges.length];
		for (int i = 0; i < edges.length; i++) {
			for (int j = 0; j < edges[0].length; j++) {
				r.edges[i][j] = edges[i][j];
			}
		}

		return r;
	}

	public int[][] to_numpy_array() {
		return edges;
	}
}
