package org.processmining.plugins.inductiveVisualMiner.causal.mvpc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.processmining.plugins.InductiveMiner.Triple;

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TObjectIntHashMap;

public class GeneralGraph implements Cloneable {

	public List<Node> nodes;
	public int num_vars;
	public TObjectIntMap<Node> node_map;
	public Endpoint[][] graph;
	public int[][] dpath;
	public List<Triple<Node, Node, Node>> ambiguous_triples;
	public List<Triple<Node, Node, Node>> underline_triples;
	public List<Triple<Node, Node, Node>> dotted_underline_triples;
	public boolean pattern;
	public boolean pag;
	public Map<Object, Object> attributes;

	public GeneralGraph(List<Node> nodes) {
		this.nodes = nodes;
		num_vars = nodes.size();

		node_map = new TObjectIntHashMap<>();

		for (int i = 0; i < num_vars; i++) {
			Node node = nodes.get(i);
			node_map.put(node, i);
		}

		graph = new Endpoint[num_vars][num_vars];
		for (Endpoint[] x : graph) { //update SL: initialise array
			Arrays.fill(x, Endpoint.NULL);
		}

		dpath = new int[num_vars][num_vars];

		reconstitute_dpath(new ArrayList<>());

		ambiguous_triples = new ArrayList<>();
		underline_triples = new ArrayList<>();
		dotted_underline_triples = new ArrayList<>();

		attributes = new THashMap<>();
		pattern = false;
		pag = false;
	}

	//	def reconstitute_dpath(self, edges: List[Edge]):
	//        for i in range(self.num_vars):
	//            self.adjust_dpath(i, i)
	//
	//        while len(edges) > 0:
	//            edge = edges.pop()
	//            node1 = edge.get_node1()
	//            node2 = edge.get_node2()
	//            i = self.node_map[node1]
	//            j = self.node_map[node2]
	//            self.adjust_dpath(i, j)
	public void reconstitute_dpath(List<Edge> edges) {
		for (int i = 0; i < num_vars; i++) {
			adjust_dpath(i, i);
		}

		while (!edges.isEmpty()) {
			Edge edge = edges.remove(0);
			Node node1 = edge.get_node1();
			Node node2 = edge.get_node2();
			int i = node_map.get(node1);
			int j = node_map.get(node2);
			adjust_dpath(i, j);
		}
	}

	//	def adjust_dpath(self, i: int, j: int):
	//        dpath = self.dpath
	//        dpath[j, i] = 1
	//
	//        for k in range(self.num_vars):
	//            if dpath[i, k] == 1:
	//                dpath[j, k] = 1
	//
	//            if dpath[k, j] == 1:
	//                dpath[k, i] = 1
	//
	//        self.dpath = dpath
	public void adjust_dpath(int i, int j) {
		dpath[j][i] = 1;

		for (int k = 0; k < num_vars; k++) {
			if (dpath[i][k] == 1) {
				dpath[j][k] = 1;
			}

			if (dpath[k][j] == 1) {
				dpath[k][i] = 1;
			}
		}
	}

	//	# Returns the edge connecting node1 and node2, provided a unique such edge exists.
	//    def get_edge(self, node1: Node, node2: Node) -> Edge | None:
	//        i = self.node_map[node1]
	//        j = self.node_map[node2]
	//
	//        end_1 = self.graph[i, j]
	//        end_2 = self.graph[j, i]
	//
	//        if end_1 == 0:
	//            return None
	//
	//        edge = Edge(node1, node2, Endpoint(end_1), Endpoint(end_2))
	//        return edge
	public Edge get_edge(Node node1, Node node2) {
		int i = node_map.get(node1);
		int j = node_map.get(node2);

		Endpoint end_1 = graph[i][j];
		Endpoint end_2 = graph[j][i];

		if (end_1.value() == 0) {
			return null;
		}

		Edge edge = new Edge(node1, node2, end_1, end_2);
		return edge;
	}

	//	# Removes the given edge from the graph.
	//    def remove_edge(self, edge: Edge):
	//        node1 = edge.get_node1()
	//        node2 = edge.get_node2()
	//
	//        i = self.node_map[node1]
	//        j = self.node_map[node2]
	//
	//        out_of = self.graph[j, i]
	//        in_to = self.graph[i, j]
	//
	//        end1 = edge.get_numerical_endpoint1()
	//        end2 = edge.get_numerical_endpoint2()
	//
	//        if out_of == Endpoint.TAIL_AND_ARROW.value and in_to == Endpoint.TAIL_AND_ARROW.value:
	//            if end1 == Endpoint.ARROW.value:
	//                self.graph[j, i] = -1
	//                self.graph[i, j] = -1
	//            else:
	//                if end1 == -1:
	//                    self.graph[i, j] = Endpoint.ARROW.value
	//                    self.graph[j, i] = Endpoint.ARROW.value
	//        else:
	//            if out_of == Endpoint.ARROW_AND_ARROW.value and in_to == Endpoint.TAIL_AND_ARROW.value:
	//                if end1 == Endpoint.ARROW.value:
	//                    self.graph[j, i] = 1
	//                    self.graph[i, j] = -1
	//                else:
	//                    if end1 == -1:
	//                        self.graph[j, i] = Endpoint.ARROW.value
	//                        self.graph[i, j] = Endpoint.ARROW.value
	//            else:
	//                if out_of == Endpoint.TAIL_AND_ARROW.value and in_to == Endpoint.ARROW_AND_ARROW.value:
	//                    if end1 == Endpoint.ARROW.value:
	//                        self.graph[j, i] = -1
	//                        self.graph[i, j] = 1
	//                    else:
	//                        if end1 == -1:
	//                            self.graph[j, i] = Endpoint.ARROW.value
	//                            self.graph[i, j] = Endpoint.ARROW.value
	//                else:
	//                    if end1 == in_to and end2 == out_of:
	//                        self.graph[j, i] = 0
	//                        self.graph[i, j] = 0
	//
	//        self.reconstitute_dpath(self.get_graph_edges())
	public void remove_edge(Edge edge) {
		Node node1 = edge.get_node1();
		Node node2 = edge.get_node2();

		int i = node_map.get(node1);
		int j = node_map.get(node2);

		Endpoint out_of = graph[j][i];
		Endpoint in_to = graph[i][j];

		int end1 = edge.getNumerical_endpoint1();
		int end2 = edge.getNumerical_endpoint2();

		if (out_of == Endpoint.TAIL_AND_ARROW && in_to == Endpoint.TAIL_AND_ARROW) {
			if (end1 == Endpoint.ARROW.value()) {
				graph[j][i] = Endpoint.get(-1);
				graph[i][j] = Endpoint.get(-1);
			} else {
				if (end1 == -1) {
					graph[i][j] = Endpoint.ARROW;
					graph[j][i] = Endpoint.ARROW;
				}
			}
		} else {
			if (out_of == Endpoint.ARROW_AND_ARROW && in_to == Endpoint.TAIL_AND_ARROW) {
				if (end1 == Endpoint.ARROW.value()) {
					graph[j][i] = Endpoint.get(1);
					graph[i][j] = Endpoint.get(-1);
				} else {
					if (end1 == -1) {
						graph[j][i] = Endpoint.ARROW;
						graph[i][j] = Endpoint.ARROW;
					}
				}
			} else {
				if (out_of == Endpoint.TAIL_AND_ARROW && in_to == Endpoint.ARROW_AND_ARROW) {
					if (end1 == Endpoint.ARROW.value()) {
						graph[j][i] = Endpoint.get(-1);
						graph[i][j] = Endpoint.get(1);
					} else {
						if (end1 == -1) {
							graph[j][i] = Endpoint.ARROW;
							graph[i][j] = Endpoint.ARROW;
						}
					}
				} else {
					if (end1 == in_to.value() && end2 == out_of.value()) {
						graph[j][i] = Endpoint.get(0);
						graph[i][j] = Endpoint.get(0);
					}
				}
			}
		}
		reconstitute_dpath(get_graph_edges());
	}

	//	def get_graph_edges(self) -> List[Edge]:
	//        edges: List[Edge] = []
	//        for i in range(self.num_vars):
	//            node = self.nodes[i]
	//            for j in range(i + 1, self.num_vars):
	//                node2 = self.nodes[j]
	//                if self.graph[j, i] == 1 or self.graph[j, i] == -1 or self.graph[j, i] == 2:
	//                    edges.append(self.get_edge(node, node2))
	//                else:
	//                    if self.graph[j, i] == Endpoint.TAIL_AND_ARROW.value \
	//                            and self.graph[i, j] == Endpoint.ARROW_AND_ARROW.value:
	//                        edges.append(Edge(node, node2, Endpoint.ARROW, Endpoint.TAIL))
	//                        edges.append(Edge(node, node2, Endpoint.ARROW, Endpoint.ARROW))
	//                    else:
	//                        if self.graph[j, i] == Endpoint.ARROW_AND_ARROW.value \
	//                                and self.graph[i, j] == Endpoint.TAIL_AND_ARROW.value:
	//                            edges.append(Edge(node, node2, Endpoint.TAIL, Endpoint.ARROW))
	//                            edges.append(Edge(node, node2, Endpoint.ARROW, Endpoint.ARROW))
	//                        else:
	//                            if self.graph[j, i] == Endpoint.TAIL_AND_ARROW.value \
	//                                    and self.graph[i, j] == Endpoint.TAIL_AND_ARROW.value:
	//                                edges.append(Edge(node, node2, Endpoint.TAIL, Endpoint.TAIL))
	//                                edges.append(Edge(node, node2, Endpoint.ARROW, Endpoint.ARROW))
	//
	//        return edges
	public List<Edge> get_graph_edges() {
		List<Edge> edges = new ArrayList<>();
		for (int i = 0; i < num_vars; i++) {
			Node node = nodes.get(i);
			for (int j = i + 1; j < num_vars; j++) {
				Node node2 = nodes.get(j);
				if (graph[j][i].value() == 1 || graph[j][i].value() == -1 || graph[j][i].value() == 2) {
					edges.add(get_edge(node, node2));
				} else {
					if (graph[j][i] == Endpoint.TAIL_AND_ARROW && graph[i][j] == Endpoint.ARROW_AND_ARROW) {
						edges.add(new Edge(node, node2, Endpoint.ARROW, Endpoint.TAIL));
						edges.add(new Edge(node, node2, Endpoint.ARROW, Endpoint.ARROW));
					} else {
						if (graph[j][i] == Endpoint.ARROW_AND_ARROW && graph[i][j] == Endpoint.TAIL_AND_ARROW) {
							edges.add(new Edge(node, node2, Endpoint.TAIL, Endpoint.ARROW));
							edges.add(new Edge(node, node2, Endpoint.ARROW, Endpoint.ARROW));
						} else {
							if (graph[j][i] == Endpoint.TAIL_AND_ARROW && graph[i][j] == Endpoint.TAIL_AND_ARROW) {
								edges.add(new Edge(node, node2, Endpoint.TAIL, Endpoint.TAIL));
								edges.add(new Edge(node, node2, Endpoint.ARROW, Endpoint.ARROW));
							}
						}
					}
				}
			}
		}
		return edges;
	}

	//	# Adds the specified edge to the graph, provided it is not already in the
	//    # graph.
	//    def add_edge(self, edge: Edge):
	//        node1 = edge.get_node1()
	//        node2 = edge.get_node2()
	//        endpoint1 = str(edge.get_endpoint1())
	//        endpoint2 = str(edge.get_endpoint2())
	//
	//        i = self.node_map[node1]
	//        j = self.node_map[node2]
	//
	//        e1 = self.graph[i, j]
	//        e2 = self.graph[j, i]
	//
	//        bidirected = e2 == 1 and e1 == 1
	//        existing_edge = not bidirected and (e2 != 0 or e1 != 0)
	//
	//        if endpoint1 == "TAIL":
	//            if existing_edge:
	//                return False
	//            if endpoint2 == "TAIL":
	//                if bidirected:
	//                    self.graph[j, i] = Endpoint.TAIL_AND_ARROW.value
	//                    self.graph[i, j] = Endpoint.TAIL_AND_ARROW.value
	//                else:
	//                    self.graph[j, i] = -1
	//                    self.graph[i, j] = -1
	//            else:
	//                if endpoint2 == "ARROW":
	//                    if bidirected:
	//                        self.graph[j, i] = Endpoint.ARROW_AND_ARROW.value
	//                        self.graph[i, j] = Endpoint.TAIL_AND_ARROW.value
	//                    else:
	//                        self.graph[j, i] = 1
	//                        self.graph[i, j] = -1
	//                    self.adjust_dpath(i, j)
	//                else:
	//                    if endpoint2 == "CIRCLE":
	//                        if bidirected:
	//                            return False
	//                        else:
	//                            self.graph[j, i] = 2
	//                            self.graph[i, j] = -1
	//                    else:
	//                        return False
	//        else:
	//            if endpoint1 == "ARROW":
	//                if endpoint2 == "ARROW":
	//                    if existing_edge:
	//
	//                        if e1 == 2 or e2 == 2:
	//                            return False
	//                        if self.graph[j, i] == Endpoint.ARROW.value:
	//                            self.graph[j, i] = Endpoint.ARROW_AND_ARROW.value
	//                        else:
	//                            self.graph[j, i] = Endpoint.TAIL_AND_ARROW.value
	//                        if self.graph[i, j] == Endpoint.ARROW.value:
	//                            self.graph[i, j] = Endpoint.ARROW_AND_ARROW.value
	//                        else:
	//                            self.graph[i, j] = Endpoint.TAIL_AND_ARROW.value
	//                    else:
	//                        self.graph[j, i] = Endpoint.ARROW.value
	//                        self.graph[i, j] = Endpoint.ARROW.value
	//                else:
	//                    return False
	//            else:
	//                if endpoint1 == "CIRCLE":
	//                    if existing_edge:
	//                        return False
	//                    if endpoint2 == "ARROW":
	//                        if bidirected:
	//                            return False
	//                        else:
	//                            self.graph[j, i] = 1
	//                            self.graph[i, j] = 2
	//                    else:
	//                        if endpoint2 == "CIRCLE":
	//                            if bidirected:
	//                                return False
	//                            else:
	//                                self.graph[j, i] = 2
	//                                self.graph[i, j] = 2
	//                        else:
	//                            return False
	//                else:
	//                    return False
	//
	//            return True
	public boolean add_edge(Edge edge) {
		Node node1 = edge.get_node1();
		Node node2 = edge.get_node2();
		Endpoint endpoint1 = edge.get_endpoint1();
		Endpoint endpoint2 = edge.get_endpoint2();

		int i = node_map.get(node1);
		int j = node_map.get(node2);

		Endpoint e1 = graph[i][j];
		Endpoint e2 = graph[j][i];

		boolean bidirected = e2.value() == 1 && e1.value() == 1;
		boolean existing_edge = !bidirected && (e2.value() != 0 || e1.value() != 0);

		if (endpoint1 == Endpoint.TAIL) {
			if (existing_edge) {
				return false;
			}
			if (endpoint2 == Endpoint.TAIL) {
				if (bidirected) {
					graph[j][i] = Endpoint.TAIL_AND_ARROW;
					graph[i][j] = Endpoint.TAIL_AND_ARROW;
				} else {
					graph[j][i] = Endpoint.get(-1);
					graph[i][j] = Endpoint.get(-1);
				}
			} else {
				if (endpoint2 == Endpoint.ARROW) {
					if (bidirected) {
						graph[j][i] = Endpoint.ARROW_AND_ARROW;
						graph[i][j] = Endpoint.TAIL_AND_ARROW;
					} else {
						graph[j][i] = Endpoint.get(1);
						graph[i][j] = Endpoint.get(-1);
					}
					adjust_dpath(i, j);
				} else {
					if (endpoint2 == Endpoint.CIRCLE) {
						if (bidirected) {
							return false;
						} else {
							graph[j][i] = Endpoint.get(2);
							graph[i][j] = Endpoint.get(-1);
						}
					} else {
						return false;
					}
				}
			}
		} else {
			if (endpoint1 == Endpoint.ARROW) {
				if (endpoint2 == Endpoint.ARROW) {
					if (existing_edge) {

						if (e1 == Endpoint.get(2) || e2 == Endpoint.get(2)) {
							return false;
						}
						if (graph[j][i] == Endpoint.ARROW) {
							graph[j][i] = Endpoint.ARROW_AND_ARROW;
						} else {
							graph[j][i] = Endpoint.TAIL_AND_ARROW;
						}
						if (graph[i][j] == Endpoint.ARROW) {
							graph[i][j] = Endpoint.ARROW_AND_ARROW;
						} else {
							graph[i][j] = Endpoint.TAIL_AND_ARROW;
						}
					} else {
						graph[j][i] = Endpoint.ARROW;
						graph[i][j] = Endpoint.ARROW;
					}
				} else {
					return false;
				}
			} else {
				if (endpoint1 == Endpoint.CIRCLE) {
					if (existing_edge) {
						return false;
					}
					if (endpoint2 == Endpoint.ARROW) {
						if (bidirected) {
							return false;
						} else {
							graph[j][i] = Endpoint.get(1);
							graph[i][j] = Endpoint.get(2);
						}
					} else {
						if (endpoint2 == Endpoint.CIRCLE) {
							if (bidirected) {
								return false;
							} else {
								graph[j][i] = Endpoint.get(2);
								graph[i][j] = Endpoint.get(2);
							}
						} else {
							return false;
						}
					}
				} else {
					return false;
				}
			}
			return true;
		}

		//SL: this function needs a return value; however, this is never used
		return true;
	}

	//    # Return true iff node1 is an ancestor of node2.
	//    def is_ancestor_of(self, node1: Node, node2: Node) -> bool:
	//        i = self.node_map[node1]
	//        j = self.node_map[node2]
	//        return self.dpath[j, i] == 1
	public boolean is_ancestor_of(Node node1, Node node2) {
		int i = node_map.get(node1);
		int j = node_map.get(node2);
		return dpath[j][i] == 1;
	}

	public GeneralGraph clone() {
		GeneralGraph r = null;
		try {
			r = (GeneralGraph) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}

		//SL design decision: do not clone nodes
		r.nodes = new ArrayList<>(nodes);
		r.num_vars = num_vars;
		r.node_map = new TObjectIntHashMap<>(node_map);

		r.graph = new Endpoint[graph.length][graph[0].length];
		for (int i = 0; i < graph.length; i++) {
			for (int j = 0; j < graph[i].length; j++) {
				r.graph[i][j] = graph[i][j];
			}
		}

		r.dpath = new int[dpath.length][dpath[0].length];
		for (int i = 0; i < dpath.length; i++) {
			for (int j = 0; j < dpath[i].length; j++) {
				r.dpath[i][j] = dpath[i][j];
			}
		}

		r.ambiguous_triples = new ArrayList<>(ambiguous_triples);
		r.underline_triples = new ArrayList<>(underline_triples);
		r.dotted_underline_triples = new ArrayList<>(dotted_underline_triples);
		r.pattern = pattern;
		r.pag = pag;
		r.attributes = new THashMap<>(attributes);

		return r;
	}
}