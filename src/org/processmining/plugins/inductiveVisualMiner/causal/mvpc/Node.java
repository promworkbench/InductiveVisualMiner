package org.processmining.plugins.inductiveVisualMiner.causal.mvpc;

import java.util.Map;

import gnu.trove.map.hash.THashMap;

public class Node implements Cloneable {

	private String name;
	private NodeType node_type;
	private int center_x;
	private int center_y;
	private Map<String, String> attributes;

	public Node(String name) {
		this.name = name;
		this.node_type = NodeType.MEASURED;
		this.center_x = -1;
		this.center_y = -1;
		this.attributes = new THashMap<>();
	}

	public String get_name() {
		return name;
	}

	public Node clone() {
		Node r = null;
		try {
			r = (Node) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}

		r.name = name;
		r.node_type = node_type;
		r.center_x = center_x;
		r.center_y = center_y;
		r.attributes = new THashMap<>(attributes);

		return r;
	}
}