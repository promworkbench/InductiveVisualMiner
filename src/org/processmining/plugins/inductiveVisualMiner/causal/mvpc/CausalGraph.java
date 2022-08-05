package org.processmining.plugins.inductiveVisualMiner.causal.mvpc;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.InductiveMiner.Quadruple;
import org.processmining.plugins.InductiveMiner.Triple;

import gnu.trove.TIntCollection;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.hash.THashSet;

public class CausalGraph implements Cloneable {

	public GeneralGraph G;
	public CIT test;
	public List<TIntList>[][] sepset;
	public TIntList definite_UC;
	public TIntList definite_non_UC;
	public int PC_elapsed;
	public TIntList redundant_nodes;
	public TIntObjectMap<String> labels;
	public DiGraph nx_graph;
	public Graph nx_skel;
	public Map<String, List<TIntList>> prt_m;

	//	if node_names is None:
	//        node_names = [("X%d" % (i + 1)) for i in range(no_of_var)]
	//    assert len(node_names) == no_of_var, "number of node_names must match number of variables"
	//    assert len(node_names) == len(set(node_names)), "node_names must be unique"
	//    nodes: List[Node] = []
	//    for name in node_names:
	//        node = GraphNode(name)
	//        nodes.append(node)
	//    self.G: GeneralGraph = GeneralGraph(nodes)
	//    for i in range(no_of_var):
	//        for j in range(i + 1, no_of_var):
	//            self.G.add_edge(Edge(nodes[i], nodes[j], Endpoint.TAIL, Endpoint.TAIL))
	//    self.test: CIT | None = None
	//    self.sepset = np.empty((no_of_var, no_of_var), object)  # store the collection of sepsets
	//    self.definite_UC = []  # store the list of definite unshielded colliders
	//    self.definite_non_UC = []  # store the list of definite unshielded non-colliders
	//    self.PC_elapsed = -1  # store the elapsed time of running PC
	//    self.redundant_nodes = []  # store the list of redundant nodes (for subgraphs)
	//    self.nx_graph = nx.DiGraph()  # store the directed graph
	//    self.nx_skel = nx.Graph()  # store the undirected graph
	//    self.labels = {}
	//    self.prt_m = {}  # store the parents of missingness indicators
	@SuppressWarnings("unchecked")
	public CausalGraph(int no_of_var, List<String> node_names) {
		if (node_names == null) {
			node_names = new ArrayList<>();
			for (int i = 0; i < no_of_var; i++) {
				node_names.add("N" + i);
			}
		}
		assert node_names.size() == no_of_var : "number of node_names must match number of variables";
		assert node_names.size() == (new THashSet<String>(node_names)).size() : "node_names must be unique";
		List<Node> nodes = new ArrayList<>();
		for (String name : node_names) {
			GraphNode node = new GraphNode(name);
			nodes.add(node);
		}
		G = new GeneralGraph(nodes);
		for (int i = 0; i < no_of_var; i++) {
			for (int j = i + 1; j < no_of_var; j++) {
				G.add_edge(new Edge(nodes.get(i), nodes.get(j), Endpoint.TAIL, Endpoint.TAIL));
			}
		}
		test = null;
		sepset = new List[no_of_var][no_of_var]; //# store the collection of sepsets
		definite_UC = new TIntArrayList(); //# store the list of definite unshielded colliders
		definite_non_UC = new TIntArrayList(); //# store the list of definite unshielded non-colliders
		PC_elapsed = -1; //# store the elapsed time of running PC
		redundant_nodes = new TIntArrayList(); //# store the list of redundant nodes (for subgraphs)
		nx_graph = new DiGraph(); //# store the directed graph
		nx_skel = new Graph(); //# store the undirected graph
		labels = new TIntObjectHashMap<>(no_of_var, 0.5f, -1);
		prt_m = new THashMap<>(); //# store the parents of missingness indicators
	}

	//    def max_degree(self) -> int:
	//        """Return the maximum number of edges connected to a node in adjmat"""
	//        return max(np.sum(self.G.graph != 0, axis=1))
	public int max_degree() {
		int max = Integer.MIN_VALUE;
		for (int i = 0; i < G.graph.length; i++) {
			int rowCount = 0;

			for (int j = 0; j < G.graph[i].length; j++) {
				if (G.graph[i][j] != Endpoint.get(0)) {
					rowCount++;
				}
			}

			max = Math.max(max, rowCount);
		}
		return max;
	}

	//	def set_ind_test(self, indep_test):
	//        """Set the conditional independence test that will be used"""
	//        self.test = indep_test
	public void set_ind_test(CIT indep_test) {
		test = indep_test;
	}

	//	def neighbors(self, i: int):
	//        """Find the neighbors of node i in adjmat"""
	//        return np.where(self.G.graph[i, :] != 0)[0]
	public TIntList neighbors(int i) {
		TIntList result = new TIntArrayList();
		for (int j = 0; j < G.graph[i].length; j++) {
			if (G.graph[i][j] != Endpoint.get(0)) {
				result.add(j);
			}
		}
		return result;
	}

	//    def ci_test(self, i: int, j: int, S) -> float:
	//        """Define the conditional independence test"""
	//        # assert i != j and not i in S and not j in S
	//        if self.test.method == 'mc_fisherz': return self.test(i, j, S, self.nx_skel, self.prt_m)
	//        return self.test(i, j, S)
	public double ci_test(int i, int j, TIntCollection S) {
		//"""Define the conditional independence test"""
		//# assert i != j and not i in S and not j in S
		if (test.method == IndependenceTest.mc_fisherz) {
			return test.call(i, j, S, nx_skel, prt_m);
		}
		return test.call(i, j, S);
	}

	//    def to_nx_skeleton(self):
	//        """Convert adjmat into its skeleton (a networkx.Graph object) named nx_skel"""
	//        nodes = range(len(self.G.graph))
	//        self.nx_skel.add_nodes_from(nodes)
	//        adj = [(i, j) for (i, j) in self.find_adj() if i < j]
	//        for (i, j) in adj:
	//            self.nx_skel.add_edge(i, j, color='g')  # Green edge: undirected edge
	public void to_nx_skeleton() {
		nx_skel.add_nodes_from(G.graph.length);

		List<Pair<Integer, Integer>> adj = find_adj();
		{
			Iterator<Pair<Integer, Integer>> it = adj.iterator();
			while (it.hasNext()) {
				Pair<Integer, Integer> p = it.next();
				if (!(p.getA() < p.getB())) {
					it.remove();
				}
			}
		}

		for (Pair<Integer, Integer> p : adj) {
			int i = p.getA();
			int j = p.getB();
			nx_skel.add_edge(i, j, 'g'); // # Green edge: undirected edge
		}

	}

	//	def find_adj(self):
	//        """Return the list of adjacencies i --- j in adjmat as (i, j) [with symmetry]"""
	//        return list(self.find_tails() + self.find_arrow_heads())
	public List<Pair<Integer, Integer>> find_adj() {
		List<Pair<Integer, Integer>> result = new ArrayList<>();
		result.addAll(find_tails());
		result.addAll(find_arrow_heads());
		return result;
	}

	//    def find_arrow_heads(self) -> List[Tuple[int, int]]:
	//        """Return the list of i o-> j in adjmat as (i, j)"""
	//        L = np.where(self.G.graph == 1)
	//        return list(zip(L[1], L[0]))
	public List<Pair<Integer, Integer>> find_arrow_heads() {
		List<Pair<Integer, Integer>> result = new ArrayList<>();
		for (int i = 0; i < G.graph.length; i++) {
			for (int j = 0; j < G.graph[0].length; j++) {
				if (G.graph[i][j] == Endpoint.ARROW) {
					result.add(Pair.of(j, i)); //TODO: ask author whether this is correct
				}
			}
		}
		return result;
	}

	//    def find_tails(self) -> List[Tuple[int, int]]:
	//        """Return the list of i --o j in adjmat as (j, i)"""
	//        L = np.where(self.G.graph == -1)
	//        return list(zip(L[1], L[0]))
	public List<Pair<Integer, Integer>> find_tails() {
		List<Pair<Integer, Integer>> result = new ArrayList<>();
		for (int i = 0; i < G.graph.length; i++) {
			for (int j = 0; j < G.graph[0].length; j++) {
				if (G.graph[i][j] == Endpoint.TAIL) {
					result.add(Pair.of(j, i));
				}
			}
		}
		return result;
	}

	//    def is_fully_directed(self, i, j) -> bool:
	//        """Return True if i --> j holds in adjmat and False otherwise"""
	//        return self.G.graph[i, j] == -1 and self.G.graph[j, i] == 1
	public boolean is_fully_directed(int i, int j) {
		return G.graph[i][j] == Endpoint.TAIL && G.graph[j][i] == Endpoint.ARROW;
	}

	//    def find_unshielded_triples(self) -> List[Tuple[int, int, int]]:
	//        """Return the list of unshielded triples i o-o j o-o k in adjmat as (i, j, k)"""
	//        return [(pair[0][0], pair[0][1], pair[1][1]) for pair in permutations(self.find_adj(), 2)
	//                if pair[0][1] == pair[1][0] and pair[0][0] != pair[1][1] and self.G.graph[pair[0][0], pair[1][1]] == 0]
	public Iterable<Triple<Integer, Integer, Integer>> find_unshielded_triples() {
		List<Triple<Integer, Integer, Integer>> result = new ArrayList<>();
		for (int[][] pair : JavaHelperClasses.permutationPairs(find_adj())) {
			if (pair[0][1] == pair[1][0] && pair[0][0] != pair[1][1]
					&& G.graph[pair[0][0]][pair[1][1]] == Endpoint.get(0)) {
				result.add(Triple.of(pair[0][0], pair[0][1], pair[1][1]));
			}
		}
		return result;
	}

	public CausalGraph deepcopy() {
		return clone();
	}

	@SuppressWarnings("unchecked")
	protected CausalGraph clone() {
		CausalGraph r = null;
		try {
			r = (CausalGraph) super.clone();
		} catch (CloneNotSupportedException e1) {
			e1.printStackTrace();
		}
		r.G = G.clone();
		r.test = test.clone();
		r.sepset = new List[sepset.length][sepset[0].length];
		for (int i = 0; i < sepset.length; i++) {
			for (int j = 0; j < sepset[0].length; j++) {
				r.sepset[i][j] = new ArrayList<>();
				for (TIntList list : sepset[i][j]) {
					r.sepset[i][j].add(new TIntArrayList(list));
				}
			}
		}
		r.PC_elapsed = PC_elapsed;
		r.labels = new TIntObjectHashMap<>(labels);
		r.nx_graph = nx_graph.clone();
		r.nx_skel = nx_skel.clone();
		r.redundant_nodes = new TIntArrayList(redundant_nodes);

		r.definite_UC = new TIntArrayList(definite_UC);
		r.definite_non_UC = new TIntArrayList(definite_non_UC);

		r.prt_m = new THashMap<>();
		for (Entry<String, List<TIntList>> e : prt_m.entrySet()) {
			ArrayList<TIntList> v = new ArrayList<>();
			for (TIntList l : e.getValue()) {
				v.add(new TIntArrayList(l));
			}
			r.prt_m.put(e.getKey(), v);
		}

		return r;
	}

	//    def find_cond_sets(self, i: int, j: int) -> List[Tuple[int]]:
	//        """return the list of conditioning sets of the neighbors of i or j in adjmat"""
	//        neigh_x = self.neighbors(i)
	//        neigh_y = self.neighbors(j)
	//        pow_neigh_x = powerset(neigh_x)
	//        pow_neigh_y = powerset(neigh_y)
	//        return list_union(pow_neigh_x, pow_neigh_y)
	public List<TIntList> find_cond_sets(int i, int j) {
		TIntList neigh_x = neighbors(i);
		TIntList neigh_y = neighbors(j);
		List<TIntList> pow_neigh_x = Helper.powerset(neigh_x);
		List<TIntList> pow_neigh_y = Helper.powerset(neigh_y);
		return Helper.list_union(pow_neigh_x, pow_neigh_y);
	}

	//    def find_cond_sets_with_mid(self, i: int, j: int, k: int) -> List[Tuple[int]]:
	//        """return the list of conditioning sets of the neighbors of i or j in adjmat which contains k"""
	//        return [S for S in self.find_cond_sets(i, j) if k in S]
	public List<TIntList> find_cond_sets_with_mid(int i, int j, int k) {
		List<TIntList> result = new ArrayList<>();

		for (TIntList S : find_cond_sets(i, j)) {
			if (S.contains(k)) {
				result.add(S);
			}
		}

		return result;
	}

	//    def find_cond_sets_without_mid(self, i: int, j: int, k: int) -> List[Tuple[int]]:
	//        """return the list of conditioning sets of the neighbors of i or j which in adjmat does not contain k"""
	//        return [S for S in self.find_cond_sets(i, j) if k not in S]
	public List<TIntList> find_cond_sets_without_mid(int i, int j, int k) {
		List<TIntList> result = new ArrayList<>();

		for (TIntList S : find_cond_sets(i, j)) {
			if (!S.contains(k)) {
				result.add(S);
			}
		}

		return result;
	}

	//	def find_triangles(self) -> List[Tuple[int, int, int]]:
	//        """Return the list of triangles i o-o j o-o k o-o i in adjmat as (i, j, k) [with symmetry]"""
	//        Adj = self.find_adj()
	//        return [(pair[0][0], pair[0][1], pair[1][1]) for pair in permutations(Adj, 2)
	//                if pair[0][1] == pair[1][0] and pair[0][0] != pair[1][1] and (pair[0][0], pair[1][1]) in Adj]
	public List<Triple<Integer, Integer, Integer>> find_triangles() {
		List<Pair<Integer, Integer>> Adj = find_adj();

		List<Triple<Integer, Integer, Integer>> result = new ArrayList<>();
		for (int[][] pair : JavaHelperClasses.permutationPairs(Adj)) {
			if (pair[0][1] == pair[1][0] && pair[0][0] != pair[1][1] && Adj.contains(Pair.of(pair[0][0], pair[1][1]))) {
				result.add(Triple.of(pair[0][0], pair[0][1], pair[1][1]));
			}
		}

		return result;
	}

	//	"""Return the list of non-ambiguous kites i o-o j o-o l o-o k o-o i o-o l in adjmat \
	//    (where j and k are non-adjacent) as (i, j, k, l) [with asymmetry j < k]"""
	//    return [(pair[0][0], pair[0][1], pair[1][1], pair[0][2]) for pair in permutations(self.find_triangles(), 2)
	//            if pair[0][0] == pair[1][0] and pair[0][2] == pair[1][2]
	//            and pair[0][1] < pair[1][1] and self.G.graph[pair[0][1], pair[1][1]] == 0]
	public List<Quadruple<Integer, Integer, Integer, Integer>> find_kites() {
		List<Triple<Integer, Integer, Integer>> triangles = find_triangles();

		List<Quadruple<Integer, Integer, Integer, Integer>> result = new ArrayList<>();

		for (int[][] pair : JavaHelperClasses.permutationTriples(triangles)) {
			if (pair[0][0] == pair[1][0] && pair[0][2] == pair[1][2] && pair[0][1] < pair[1][1]
					&& G.graph[pair[0][1]][pair[1][1]] == Endpoint.get(0)) {
				result.add(Quadruple.of(pair[0][0], pair[0][1], pair[1][1], pair[0][2]));
			}
		}

		return result;
	}

	//    def is_undirected(self, i, j) -> bool:
	//        """Return True if i --- j holds in adjmat and False otherwise"""
	//        return self.G.graph[i, j] == -1 and self.G.graph[j, i] == -1
	public boolean is_undirected(int i, int j) {
		return G.graph[i][j] == Endpoint.get(-1) && G.graph[j][i] == Endpoint.get(-1);
	}

	//    def to_nx_graph(self):
	//        """Convert adjmat into a networkx.Digraph object named nx_graph"""
	//        nodes = range(len(self.G.graph))
	//        self.labels = {i: self.G.nodes[i].get_name() for i in nodes}
	//        self.nx_graph.add_nodes_from(nodes)
	//        undirected = self.find_undirected()
	//        directed = self.find_fully_directed()
	//        bidirected = self.find_bi_directed()
	//        for (i, j) in undirected:
	//            self.nx_graph.add_edge(i, j, color='g')  # Green edge: undirected edge
	//        for (i, j) in directed:
	//            self.nx_graph.add_edge(i, j, color='b')  # Blue edge: directed edge
	//        for (i, j) in bidirected:
	//            self.nx_graph.add_edge(i, j, color='r')  # Red edge: bidirected edge
	public void to_nx_graph() {
		labels.clear();
		for (int node = 0; node < G.graph.length; node++) {
			labels.put(node, G.nodes.get(node).get_name());
		}

		nx_graph.add_nodes_from(G.graph.length);
		List<Pair<Integer, Integer>> undirected = find_undirected();
		List<Pair<Integer, Integer>> directed = find_fully_directed();
		List<Pair<Integer, Integer>> bidirected = find_bi_directed();
		for (Pair<Integer, Integer> p : undirected) {
			int i = p.getA();
			int j = p.getB();
			nx_graph.add_edge(i, j, 'g'); // # Green edge: undirected edge
		}
		for (Pair<Integer, Integer> p : directed) {
			int i = p.getA();
			int j = p.getB();
			nx_graph.add_edge(i, j, 'b'); // # Blue edge: directed edge
		}
		for (Pair<Integer, Integer> p : bidirected) {
			int i = p.getA();
			int j = p.getB();
			nx_graph.add_edge(i, j, 'r'); // # Red edge: bidirected edge
		}
	}

	//    def find_undirected(self) -> List[Tuple[int, int]]:
	//        """Return the list of undirected edge i --- j in adjmat as (i, j) [with symmetry]"""
	//        return [(edge[0], edge[1]) for edge in self.find_tails() if self.G.graph[edge[0], edge[1]] == -1]
	public List<Pair<Integer, Integer>> find_undirected() {
		List<Pair<Integer, Integer>> result = new ArrayList<>();
		for (Pair<Integer, Integer> p : find_tails()) {
			if (G.graph[p.getA()][p.getB()] == Endpoint.get(-1)) {
				result.add(p);
			}
		}
		return result;
	}

	//	def find_fully_directed(self) -> List[Tuple[int, int]]:
	//        """Return the list of directed edges i --> j in adjmat as (i, j)"""
	//        return [(edge[0], edge[1]) for edge in self.find_arrow_heads() if self.G.graph[edge[0], edge[1]] == -1]
	public List<Pair<Integer, Integer>> find_fully_directed() {
		List<Pair<Integer, Integer>> result = new ArrayList<>();
		for (Pair<Integer, Integer> p : find_arrow_heads()) {
			if (G.graph[p.getA()][p.getB()] == Endpoint.get(-1)) {
				result.add(p);
			}
		}
		return result;
	}

	//	def find_bi_directed(self) -> List[Tuple[int, int]]:
	//        """Return the list of bidirected edges i <-> j in adjmat as (i, j) [with symmetry]"""
	//        return [(edge[1], edge[0]) for edge in self.find_arrow_heads() if (
	//                self.G.graph[edge[1], edge[0]] == Endpoint.ARROW.value and self.G.graph[
	//            edge[0], edge[1]] == Endpoint.ARROW.value)]
	public List<Pair<Integer, Integer>> find_bi_directed() {
		List<Pair<Integer, Integer>> result = new ArrayList<>();
		for (Pair<Integer, Integer> p : find_arrow_heads()) {
			if (G.graph[p.getB()][p.getA()] == Endpoint.ARROW && G.graph[p.getA()][p.getB()] == Endpoint.ARROW) {
				result.add(Pair.of(p.getB(), p.getA()));
			}
		}
		return result;

	}
}