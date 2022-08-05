package org.processmining.plugins.inductiveVisualMiner.causal.mvpc;

import java.util.List;
import java.util.Set;

import org.processmining.plugins.InductiveMiner.Pair;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.THashSet;
import gnu.trove.set.hash.TIntHashSet;

public class SkeletonDiscovery {

	//	def skeleton_discovery(
	//		    data: ndarray, 
	//		    alpha: float, 
	//		    indep_test: CIT,
	//		    stable: bool = True,
	//		    background_knowledge: BackgroundKnowledge | None = None, 
	//		    verbose: bool = False,
	//		    show_progress: bool = True,
	//		    node_names: List[str] | None = None, 
	//		) -> CausalGraph:
	//		    """
	//		    Perform skeleton discovery
	//
	//		    Parameters
	//		    ----------
	//		    data : data set (numpy ndarray), shape (n_samples, n_features). The input data, where n_samples is the number of
	//		            samples and n_features is the number of features.
	//		    alpha: float, desired significance level of independence tests (p_value) in (0,1)
	//		    indep_test : class CIT, the independence test being used
	//		            [fisherz, chisq, gsq, mv_fisherz, kci]
	//		           - fisherz: Fisher's Z conditional independence test
	//		           - chisq: Chi-squared conditional independence test
	//		           - gsq: G-squared conditional independence test
	//		           - mv_fisherz: Missing-value Fishers'Z conditional independence test
	//		           - kci: Kernel-based conditional independence test
	//		    stable : run stabilized skeleton discovery if True (default = True)
	//		    background_knowledge : background knowledge
	//		    verbose : True iff verbose output should be printed.
	//		    show_progress : True iff the algorithm progress should be show in console.
	//		    node_names: Shape [n_features]. The name for each feature (each feature is represented as a Node in the graph, so it's also the node name)
	//
	//		    Returns
	//		    -------
	//		    cg : a CausalGraph object. Where cg.G.graph[j,i]=0 and cg.G.graph[i,j]=1 indicates  i -> j ,
	//		                    cg.G.graph[i,j] = cg.G.graph[j,i] = -1 indicates i -- j,
	//		                    cg.G.graph[i,j] = cg.G.graph[j,i] = 1 indicates i <-> j.
	//
	//		    """
	//
	//		    assert type(data) == np.ndarray
	//		    assert 0 < alpha < 1
	//
	//		    no_of_var = data.shape[1]
	//		    cg = CausalGraph(no_of_var, node_names)
	//		    cg.set_ind_test(indep_test)
	//
	//		    depth = -1
	//		    pbar = tqdm(total=no_of_var) if show_progress else None
	//		    while cg.max_degree() - 1 > depth:
	//		        depth += 1
	//		        edge_removal = []
	//		        if show_progress:
	//		            pbar.reset()
	//		        for x in range(no_of_var):
	//		            if show_progress:
	//		                pbar.update()
	//		            if show_progress:
	//		                pbar.set_description(f'Depth={depth}, working on node {x}')
	//		            Neigh_x = cg.neighbors(x)
	//		            if len(Neigh_x) < depth - 1:
	//		                continue
	//		            for y in Neigh_x:
	//		                knowledge_ban_edge = False
	//		                sepsets = set()
	//		                if background_knowledge is not None and (
	//		                        background_knowledge.is_forbidden(cg.G.nodes[x], cg.G.nodes[y])
	//		                        and background_knowledge.is_forbidden(cg.G.nodes[y], cg.G.nodes[x])):
	//		                    knowledge_ban_edge = True
	//		                if knowledge_ban_edge:
	//		                    if not stable:
	//		                        edge1 = cg.G.get_edge(cg.G.nodes[x], cg.G.nodes[y])
	//		                        if edge1 is not None:
	//		                            cg.G.remove_edge(edge1)
	//		                        edge2 = cg.G.get_edge(cg.G.nodes[y], cg.G.nodes[x])
	//		                        if edge2 is not None:
	//		                            cg.G.remove_edge(edge2)
	//		                        append_value(cg.sepset, x, y, ())
	//		                        append_value(cg.sepset, y, x, ())
	//		                        break
	//		                    else:
	//		                        edge_removal.append((x, y))  # after all conditioning sets at
	//		                        edge_removal.append((y, x))  # depth l have been considered
	//
	//		                Neigh_x_noy = np.delete(Neigh_x, np.where(Neigh_x == y))
	//		                for S in combinations(Neigh_x_noy, depth):
	//		                    p = cg.ci_test(x, y, S)
	//		                    if p > alpha:
	//		                        if verbose:
	//		                            print('%d ind %d | %s with p-value %f\n' % (x, y, S, p))
	//		                        if not stable:
	//		                            edge1 = cg.G.get_edge(cg.G.nodes[x], cg.G.nodes[y])
	//		                            if edge1 is not None:
	//		                                cg.G.remove_edge(edge1)
	//		                            edge2 = cg.G.get_edge(cg.G.nodes[y], cg.G.nodes[x])
	//		                            if edge2 is not None:
	//		                                cg.G.remove_edge(edge2)
	//		                            append_value(cg.sepset, x, y, S)
	//		                            append_value(cg.sepset, y, x, S)
	//		                            break
	//		                        else:
	//		                            edge_removal.append((x, y))  # after all conditioning sets at
	//		                            edge_removal.append((y, x))  # depth l have been considered
	//		                            for s in S:
	//		                                sepsets.add(s)
	//		                    else:
	//		                        if verbose:
	//		                            print('%d dep %d | %s with p-value %f\n' % (x, y, S, p))
	//		                append_value(cg.sepset, x, y, tuple(sepsets))
	//		                append_value(cg.sepset, y, x, tuple(sepsets))
	//
	//		        if show_progress:
	//		            pbar.refresh()
	//
	//		        for (x, y) in list(set(edge_removal)):
	//		            edge1 = cg.G.get_edge(cg.G.nodes[x], cg.G.nodes[y])
	//		            if edge1 is not None:
	//		                cg.G.remove_edge(edge1)
	//
	//		    if show_progress:
	//		        pbar.close()
	//
	//		    return cg
	public static CausalGraph skeleton_discovery(double[][] data, double alpha, CIT indep_test, boolean stable,
			BackgroundKnowledge background_knowledge, List<String> node_names) {
		boolean verbose = false;
		boolean show_progress = false;

		assert 0 < alpha && alpha < 1;

		int no_of_var = JavaHelperClasses.getNumberOfColumns(data);
		CausalGraph cg = new CausalGraph(no_of_var, node_names);
		cg.set_ind_test(indep_test);

		int depth = -1;

		while (cg.max_degree() - 1 > depth) {
			depth += 1;
			Set<Pair<Integer, Integer>> edge_removal = new THashSet<>();

			for (int x = 0; x < no_of_var; x++) {
				TIntList Neigh_x = cg.neighbors(x);
				if (Neigh_x.size() < depth - 1) {
					continue;
				}
				for (TIntIterator it = Neigh_x.iterator(); it.hasNext();) {
					int y = it.next();
					boolean knowledge_ban_edge = false;
					TIntSet sepsets = new TIntHashSet();
					if (background_knowledge != null
							&& (background_knowledge.is_forbidden(cg.G.nodes.get(x), cg.G.nodes.get(y))
									&& background_knowledge.is_forbidden(cg.G.nodes.get(y), cg.G.nodes.get(x)))) {
						knowledge_ban_edge = true;
					}
					if (knowledge_ban_edge) {
						if (!stable) {
							Edge edge1 = cg.G.get_edge(cg.G.nodes.get(x), cg.G.nodes.get(y));
							if (edge1 != null) {
								cg.G.remove_edge(edge1);
							}
							Edge edge2 = cg.G.get_edge(cg.G.nodes.get(y), cg.G.nodes.get(x));
							if (edge2 != null) {
								cg.G.remove_edge(edge2);
							}
							Helper.append_value(cg.sepset, x, y, new TIntArrayList());
							Helper.append_value(cg.sepset, y, x, new TIntArrayList());
							break;
						} else {
							edge_removal.add(Pair.of(x, y)); //  # after all conditioning sets at
							edge_removal.add(Pair.of(y, x)); //  # depth l have been considered
						}
					}

					TIntList Neigh_x_noy = new TIntArrayList(Neigh_x);
					for (TIntIterator itn = Neigh_x_noy.iterator(); itn.hasNext();) {
						if (itn.next() == y) {
							itn.remove();
						}
					}

					for (TIntList S : JavaHelperClasses.combinations(Neigh_x_noy, depth)) {
						double p = cg.ci_test(x, y, S);
						if (p > alpha) {

							if (!stable) {
								Edge edge1 = cg.G.get_edge(cg.G.nodes.get(x), cg.G.nodes.get(y));
								if (edge1 != null) {
									cg.G.remove_edge(edge1);
								}
								Edge edge2 = cg.G.get_edge(cg.G.nodes.get(y), cg.G.nodes.get(x));
								if (edge2 != null) {
									cg.G.remove_edge(edge2);
								}
								Helper.append_value(cg.sepset, x, y, S);
								Helper.append_value(cg.sepset, y, x, S);
								break;
							} else {
								edge_removal.add(Pair.of(x, y)); //  # after all conditioning sets at
								edge_removal.add(Pair.of(y, x)); //  # depth l have been considered
								for (TIntIterator its = S.iterator(); its.hasNext();) {
									int s = its.next();
									sepsets.add(s);
								}
							}
						} else {

						}

					}
					Helper.append_value(cg.sepset, x, y, new TIntArrayList(sepsets));
					Helper.append_value(cg.sepset, y, x, new TIntArrayList(sepsets));
				}
			}

			for (Pair<Integer, Integer> xy : edge_removal) {
				Edge edge1 = cg.G.get_edge(cg.G.nodes.get(xy.getA()), cg.G.nodes.get(xy.getB()));
				if (edge1 != null) {
					cg.G.remove_edge(edge1);
				}
			}
		}

		return cg;
	}
}