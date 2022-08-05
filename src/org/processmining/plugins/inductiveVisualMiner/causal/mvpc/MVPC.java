package org.processmining.plugins.inductiveVisualMiner.causal.mvpc;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.processmining.plugins.InductiveMiner.Pair;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;

public class MVPC {

	//	def pc(
	//		    data: ndarray, 
	//		    alpha=0.05, 
	//		    indep_test=fisherz, 
	//		    stable: bool = True, 
	//		    uc_rule: int = 0, 
	//		    uc_priority: int = 2,
	//		    mvpc: bool = False, 
	//		    correction_name: str = 'MV_Crtn_Fisher_Z',
	//		    background_knowledge: BackgroundKnowledge | None = None, 
	//		    verbose: bool = False, 
	//		    show_progress: bool = True,
	//		    node_names: List[str] | None = None, 
	//		):
	//		    if data.shape[0] < data.shape[1]:
	//		        warnings.warn("The number of features is much larger than the sample size!")
	//
	//		    if mvpc:  # missing value PC
	//		        if indep_test == fisherz:
	//		            indep_test = mv_fisherz
	//		        return mvpc_alg(data=data, node_names=node_names, alpha=alpha, indep_test=indep_test, correction_name=correction_name, stable=stable,
	//		                        uc_rule=uc_rule, uc_priority=uc_priority, background_knowledge=background_knowledge,
	//		                        verbose=verbose,
	//		                        show_progress=show_progress)
	//		    else:
	//		        return pc_alg(data=data, node_names=node_names, alpha=alpha, indep_test=indep_test, stable=stable, uc_rule=uc_rule,
	//		                      uc_priority=uc_priority, background_knowledge=background_knowledge, verbose=verbose,
	//		                      show_progress=show_progress)
	public static CausalGraph pc(double[][] data, double alpha, IndependenceTest indep_test, boolean stable,
			int uc_rule, Priority uc_priority, boolean mvpc, Correction correction_name,
			BackgroundKnowledge background_knowledge, boolean verbose, boolean show_progress, List<String> node_names) {
		if (JavaHelperClasses.getNumberOfRows(data) < JavaHelperClasses.getNumberOfColumns(data)) {
			System.err.println("Warning: the number of features is much larger than the sample size!");
		}

		if (mvpc) { //  # missing value PC
			if (indep_test == IndependenceTest.fisherz) {
				indep_test = IndependenceTest.mv_fisherz;
			}
			return mvpc_alg(data, node_names, alpha, indep_test, correction_name, stable, uc_rule, uc_priority,
					background_knowledge, verbose, show_progress);
		} else {
			throw new RuntimeException("not implemented");
			//			return pc_alg(data, node_names, alpha, indep_test, stable, uc_rule, uc_priority, background_knowledge,
			//					verbose, show_progress);
		}
	}

	//	 * def mvpc_alg(
	//    data: ndarray,
	//    node_names: List[str] | None,
	//    alpha: float,
	//    indep_test: str,
	//    correction_name: str,
	//    stable: bool,
	//    uc_rule: int,
	//    uc_priority: int,
	//    background_knowledge: BackgroundKnowledge | None = None,
	//    verbose: bool = False,
	//    show_progress: bool = True,
	//) -> CausalGraph:
	//    """
	//    Perform missing value Peter-Clark (PC) algorithm for causal discovery
	//
	//    Parameters
	//    ----------
	//    data : data set (numpy ndarray), shape (n_samples, n_features). The input data, where n_samples is the number of samples and n_features is the number of features.
	//    node_names: Shape [n_features]. The name for each feature (each feature is represented as a Node in the graph, so it's also the node name)
	//    alpha :  float, desired significance level of independence tests (p_value) in (0,1)
	//    indep_test : str, name of the test-wise deletion independence test being used
	//            ["mv_fisherz", "mv_g_sq"]
	//            - mv_fisherz: Fisher's Z conditional independence test
	//            - mv_g_sq: G-squared conditional independence test (TODO: under development)
	//    correction_name : correction_name: name of the missingness correction
	//            [MV_Crtn_Fisher_Z, MV_Crtn_G_sq, MV_DRW_Fisher_Z, MV_DRW_G_sq]
	//            - "MV_Crtn_Fisher_Z": Permutation based correction method
	//            - "MV_Crtn_G_sq": G-squared conditional independence test (TODO: under development)
	//            - "MV_DRW_Fisher_Z": density ratio weighting based correction method (TODO: under development)
	//            - "MV_DRW_G_sq": G-squared conditional independence test (TODO: under development)
	//    stable : run stabilized skeleton discovery if True (default = True)
	//    uc_rule : how unshielded colliders are oriented
	//           0: run uc_sepset
	//           1: run maxP
	//           2: run definiteMaxP
	//    uc_priority : rule of resolving conflicts between unshielded colliders
	//           -1: whatever is default in uc_rule
	//           0: overwrite
	//           1: orient bi-directed
	//           2. prioritize existing colliders
	//           3. prioritize stronger colliders
	//           4. prioritize stronger* colliers
	//    background_knowledge: background knowledge
	//    verbose : True iff verbose output should be printed.
	//    show_progress : True iff the algorithm progress should be show in console.
	//
	//    Returns
	//    -------
	//    cg : a CausalGraph object, where cg.G.graph[j,i]=1 and cg.G.graph[i,j]=-1 indicates  i --> j ,
	//                    cg.G.graph[i,j] = cg.G.graph[j,i] = -1 indicates i --- j,
	//                    cg.G.graph[i,j] = cg.G.graph[j,i] = 1 indicates i <-> j.
	//
	//    """
	//	 * start = time.time()
	//    indep_test = CIT(data, indep_test)
	//    ## Step 1: detect the direct causes of missingness indicators
	//    prt_m = get_parent_missingness_pairs(data, alpha, indep_test, stable)
	//    # print('Finish detecting the parents of missingness indicators.  ')
	//
	//    ## Step 2:
	//    ## a) Run PC algorithm with the 1st step skeleton;
	//    cg_pre = SkeletonDiscovery.skeleton_discovery(data, alpha, indep_test, stable,
	//                                                  background_knowledge=background_knowledge,
	//                                                  verbose=verbose, show_progress=show_progress, node_names=node_names)
	//    if background_knowledge is not None:
	//        orient_by_background_knowledge(cg_pre, background_knowledge)
	//
	//    cg_pre.to_nx_skeleton()
	//    # print('Finish skeleton search with test-wise deletion.')
	//
	//    ## b) Correction of the extra edges
	//    cg_corr = skeleton_correction(data, alpha, correction_name, cg_pre, prt_m, stable)
	//    # print('Finish missingness correction.')
	//
	//    if background_knowledge is not None:
	//        orient_by_background_knowledge(cg_corr, background_knowledge)
	//
	//    ## Step 3: Orient the edges
	//    if uc_rule == 0:
	//        if uc_priority != -1:
	//            cg_2 = UCSepset.uc_sepset(cg_corr, uc_priority, background_knowledge=background_knowledge)
	//        else:
	//            cg_2 = UCSepset.uc_sepset(cg_corr, background_knowledge=background_knowledge)
	//        cg = Meek.meek(cg_2, background_knowledge=background_knowledge)
	//
	//    elif uc_rule == 1:
	//        if uc_priority != -1:
	//            cg_2 = UCSepset.maxp(cg_corr, uc_priority, background_knowledge=background_knowledge)
	//        else:
	//            cg_2 = UCSepset.maxp(cg_corr, background_knowledge=background_knowledge)
	//        cg = Meek.meek(cg_2, background_knowledge=background_knowledge)
	//
	//    elif uc_rule == 2:
	//        if uc_priority != -1:
	//            cg_2 = UCSepset.definite_maxp(cg_corr, alpha, uc_priority, background_knowledge=background_knowledge)
	//        else:
	//            cg_2 = UCSepset.definite_maxp(cg_corr, alpha, background_knowledge=background_knowledge)
	//        cg_before = Meek.definite_meek(cg_2, background_knowledge=background_knowledge)
	//        cg = Meek.meek(cg_before, background_knowledge=background_knowledge)
	//    else:
	//        raise ValueError("uc_rule should be in [0, 1, 2]")
	//    end = time.time()
	//
	//    cg.PC_elapsed = end - start
	//
	//    return cg

	/**
	 * @param data
	 * @param alpha
	 * @param uc_rule
	 * @param uc_priority
	 */
	public static CausalGraph mvpc_alg(double[][] data, List<String> node_names, double alpha,
			IndependenceTest indep_test_, Correction correction_name, boolean stable, int uc_rule, Priority uc_priority,
			BackgroundKnowledge background_knowledge, boolean verbose, boolean show_progress) {

		CIT indep_test = new CIT(data, indep_test_);

		//## Step 1: detect the direct causes of missingness indicators
		Map<String, List<TIntList>> prt_m = get_parent_missingness_pairs(data, alpha, indep_test, stable);
		//# print('Finish detecting the parents of missingness indicators.  ')

		//## Step 2:
		//## a) Run PC algorithm with the 1st step skeleton;
		CausalGraph cg_pre = SkeletonDiscovery.skeleton_discovery(data, alpha, indep_test, stable, background_knowledge,
				node_names);

		cg_pre.to_nx_skeleton();
		//# print('Finish skeleton search with test-wise deletion.')

		//## b) Correction of the extra edges
		CausalGraph cg_corr = skeleton_correction(data, alpha, correction_name, cg_pre, prt_m, stable);
		//# print('Finish missingness correction.')

		//if background_knowledge is not None:
		//    orient_by_background_knowledge(cg_corr, background_knowledge)

		//## Step 3: Orient the edges
		CausalGraph cg;
		if (uc_rule == 0) {
			CausalGraph cg_2;
			if (uc_priority != null) {
				cg_2 = UCSepset.uc_sepset(cg_corr, uc_priority, background_knowledge);
			} else {
				cg_2 = UCSepset.uc_sepset(cg_corr, background_knowledge);
			}
			cg = Meek.meek(cg_2, background_knowledge);

		} else if (uc_rule == 1) {
			CausalGraph cg_2;
			if (uc_priority != null) {
				cg_2 = UCSepset.maxp(cg_corr, uc_priority, background_knowledge);
			} else {
				cg_2 = UCSepset.maxp(cg_corr, background_knowledge);
			}
			cg = Meek.meek(cg_2, background_knowledge);

		} else if (uc_rule == 2) {
			CausalGraph cg_2;
			if (uc_priority != null) {
				cg_2 = UCSepset.definite_maxp(cg_corr, alpha, uc_priority, background_knowledge);
			} else {
				cg_2 = UCSepset.definite_maxp(cg_corr, alpha, background_knowledge);
			}
			CausalGraph cg_before = Meek.definite_meek(cg_2, background_knowledge);
			cg = Meek.meek(cg_before, background_knowledge);
		} else {
			throw new RuntimeException("uc_rule should be in [0, 1, 2]");
		}

		return cg;
	}

	//	#######################################################################################################################
	//	## *********** Functions for Step 1 ***********
	//	def get_parent_missingness_pairs(data: ndarray, alpha: float, indep_test, stable: bool = True) -> Dict[str, list]:
	//	    """
	//	    Detect the parents of missingness indicators
	//	    If a missingness indicator has no parent, it will not be included in the result
	//	    :param data: data set (numpy ndarray)
	//	    :param alpha: desired significance level in (0, 1) (float)
	//	    :param indep_test: name of the test-wise deletion independence test being used
	//	        - "MV_Fisher_Z": Fisher's Z conditional independence test
	//	        - "MV_G_sq": G-squared conditional independence test (TODO: under development)
	//	    :param stable: run stabilized skeleton discovery if True (default = True)
	//	    :return:
	//	    cg: a CausalGraph object
	//	    """
	//	    parent_missingness_pairs = {'prt': [], 'm': []}
	//
	//	    ## Get the index of missingness indicators
	//	    missingness_index = get_missingness_index(data)
	//
	//	    ## Get the index of parents of missingness indicators
	//	    # If the missingness indicator has no parent, then it will not be collected in prt_m
	//	    for missingness_i in missingness_index:
	//	        parent_of_missingness_i = detect_parent(missingness_i, data, alpha, indep_test, stable)
	//	        if not isempty(parent_of_missingness_i):
	//	            parent_missingness_pairs['prt'].append(parent_of_missingness_i)
	//	            parent_missingness_pairs['m'].append(missingness_i)
	//	    return parent_missingness_pairs

	public static Map<String, List<TIntList>> get_parent_missingness_pairs(double[][] data, double alpha,
			CIT indep_test, boolean stable) {
		//	    Detect the parents of missingness indicators
		//	    If a missingness indicator has no parent, it will not be included in the result
		//	    :param data: data set (numpy ndarray)
		//	    :param alpha: desired significance level in (0, 1) (float)
		//	    :param indep_test: name of the test-wise deletion independence test being used
		//	        - "MV_Fisher_Z": Fisher's Z conditional independence test
		//	        - "MV_G_sq": G-squared conditional independence test (TODO: under development)
		//	    :param stable: run stabilized skeleton discovery if True (default = True)
		//	    :return:
		//	    cg: a CausalGraph object
		Map<String, List<TIntList>> parent_missingness_pairs = new THashMap<>();
		parent_missingness_pairs.put("prt", new ArrayList<>());
		parent_missingness_pairs.put("m", new ArrayList<>());

		//## Get the index of missingness indicators
		TIntList missingness_index = get_missingness_index(data);

		//## Get the index of parents of missingness indicators
		//# If the missingness indicator has no parent, then it will not be collected in prt_m
		for (TIntIterator it = missingness_index.iterator(); it.hasNext();) {
			int missingness_i = it.next();
			TIntList parent_of_missingness_i = detect_parent(missingness_i, data, alpha, indep_test, stable);
			if (!parent_of_missingness_i.isEmpty()) {
				parent_missingness_pairs.get("prt").add(parent_of_missingness_i);

				TIntArrayList l = new TIntArrayList();
				l.add(missingness_i);
				parent_missingness_pairs.get("m").add(l);
			}
		}
		return parent_missingness_pairs;
	}

	//	def detect_parent(r: int, data_: ndarray, alpha: float, indep_test, stable: bool = True) -> ndarray:
	//	    """Detect the parents of a missingness indicator
	//	    :param r: the missingness indicator
	//	    :param data_: data set (numpy ndarray)
	//	    :param alpha: desired significance level in (0, 1) (float)
	//	    :param indep_test: name of the test-wise deletion independence test being used
	//	        - "MV_Fisher_Z": Fisher's Z conditional independence test
	//	        - "MV_G_sq": G-squared conditional independence test (TODO: under development)
	//	    :param stable: run stabilized skeleton discovery if True (default = True)
	//	    : return:
	//	    prt: parent of the missingness indicator, r
	//	    """
	//	    ## TODO: in the test-wise deletion CI test, if test between a binary and a continuous variable,
	//	    #  there can be the case where the binary variable only take one value after deletion.
	//	    #  It is because the assumption is violated.
	//
	//	    ## *********** Adaptation 0 ***********
	//	    # For avoid changing the original data
	//	    data = data_.copy()
	//	    ## *********** End ***********
	//
	//	    assert type(data) == np.ndarray
	//	    assert 0 < alpha < 1
	//
	//	    ## *********** Adaptation 1 ***********
	//	    # data
	//	    ## Replace the variable r with its missingness indicator
	//	    ## If r is not a missingness indicator, return [].
	//	    data[:, r] = np.isnan(data[:, r]).astype(float)  # True is missing; false is not missing
	//	    if sum(data[:, r]) == 0 or sum(data[:, r]) == len(data[:, r]):
	//	        return np.empty(0)
	//	    ## *********** End ***********
	//
	//	    no_of_var = data.shape[1]
	//	    cg = CausalGraph(no_of_var)
	//	    cg.set_ind_test(CIT(data, indep_test.method))
	//
	//	    node_ids = range(no_of_var)
	//	    pair_of_variables = list(permutations(node_ids, 2))
	//
	//	    depth = -1
	//	    while cg.max_degree() - 1 > depth:
	//	        depth += 1
	//	        edge_removal = []
	//	        for (x, y) in pair_of_variables:
	//
	//	            ## *********** Adaptation 2 ***********
	//	            # the skeleton search
	//	            ## Only test which variable is the neighbor of r
	//	            if x != r:
	//	                continue
	//	            ## *********** End ***********
	//
	//	            Neigh_x = cg.neighbors(x)
	//	            if y not in Neigh_x:
	//	                continue
	//	            else:
	//	                Neigh_x = np.delete(Neigh_x, np.where(Neigh_x == y))
	//
	//	            if len(Neigh_x) >= depth:
	//	                for S in combinations(Neigh_x, depth):
	//	                    p = cg.ci_test(x, y, S)
	//	                    if p > alpha:
	//	                        if not stable:  # Unstable: Remove x---y right away
	//	                            edge1 = cg.G.get_edge(cg.G.nodes[x], cg.G.nodes[y])
	//	                            if edge1 is not None:
	//	                                cg.G.remove_edge(edge1)
	//	                            edge2 = cg.G.get_edge(cg.G.nodes[y], cg.G.nodes[x])
	//	                            if edge2 is not None:
	//	                                cg.G.remove_edge(edge2)
	//	                        else:  # Stable: x---y will be removed only
	//	                            edge_removal.append((x, y))  # after all conditioning sets at
	//	                            edge_removal.append((y, x))  # depth l have been considered
	//	                            Helper.append_value(cg.sepset, x, y, S)
	//	                            Helper.append_value(cg.sepset, y, x, S)
	//	                        break
	//
	//	        for (x, y) in list(set(edge_removal)):
	//	            edge1 = cg.G.get_edge(cg.G.nodes[x], cg.G.nodes[y])
	//	            if edge1 is not None:
	//	                cg.G.remove_edge(edge1)
	//
	//	    ## *********** Adaptation 3 ***********
	//	    ## extract the parent of r from the graph
	//	    cg.to_nx_skeleton()
	//	    cg_skel_adj = nx.to_numpy_array(cg.nx_skel).astype(int)
	//	    prt = get_parent(r, cg_skel_adj)
	//	    ## *********** End ***********
	//
	//	    return prt

	private static TIntList detect_parent(int r, double[][] data_, double alpha, CIT indep_test, boolean stable) {
		//	    ## *********** Adaptation 0 ***********
		//		    # For avoid changing the original data
		double[][] data = JavaHelperClasses.copy(data_);
		//		    ## *********** End ***********

		assert 0 < alpha && alpha < 1;

		//		    ## *********** Adaptation 1 ***********
		//		    # data
		//		    ## Replace the variable r with its missingness indicator
		//		    ## If r is not a missingness indicator, return [].
		if (JavaHelperClasses.isAllNan(data, r) || !JavaHelperClasses.isAnyNan(data, r)) {
			return new TIntArrayList();
		}
		//		    ## *********** End ***********

		int no_of_var = JavaHelperClasses.getNumberOfColumns(data);
		CausalGraph cg = new CausalGraph(no_of_var, null);
		cg.set_ind_test(new CIT(data, indep_test.method));

		int depth = -1;
		while (cg.max_degree() - 1 > depth) {
			depth += 1;
			Set<Pair<Integer, Integer>> edge_removal = new THashSet<>();
			{
				int x = r;
				for (int y = 0; y < no_of_var; y++) {
					if (x != y) {

						TIntList Neigh_x = cg.neighbors(x);
						if (!Neigh_x.contains(y)) {
							continue;
						} else {
							JavaHelperClasses.removeAll(Neigh_x, y);
						}

						if (Neigh_x.size() >= depth) {
							for (TIntList S : JavaHelperClasses.combinations(Neigh_x, depth)) {
								double p = cg.ci_test(x, y, S);
								if (p > alpha) {
									if (!stable) { //  # Unstable: Remove x---y right away
										Edge edge1 = cg.G.get_edge(cg.G.nodes.get(x), cg.G.nodes.get(y));
										if (edge1 != null) {
											cg.G.remove_edge(edge1);
										}
										Edge edge2 = cg.G.get_edge(cg.G.nodes.get(y), cg.G.nodes.get(x));
										if (edge2 != null) {
											cg.G.remove_edge(edge2);
										}
									} else { // # Stable: x---y will be removed only
										edge_removal.add(Pair.of(x, y)); // # after all conditioning sets at
										edge_removal.add(Pair.of(y, x)); //# depth l have been considered
										Helper.append_value(cg.sepset, x, y, S);
										Helper.append_value(cg.sepset, y, x, S);
									}
									break;
								}
							}
						}
					}
				}
			}

			for (Iterator<Pair<Integer, Integer>> it = edge_removal.iterator(); it.hasNext();) {
				Pair<Integer, Integer> p = it.next();
				int x = p.getA();
				int y = p.getB();

				Edge edge1 = cg.G.get_edge(cg.G.nodes.get(x), cg.G.nodes.get(y));
				if (edge1 != null) {
					cg.G.remove_edge(edge1);
				}
			}
		}
		//		    ## *********** Adaptation 3 ***********
		//		    ## extract the parent of r from the graph
		cg.to_nx_skeleton();
		int[][] cg_skel_adj = cg.nx_skel.to_numpy_array();
		TIntList prt = get_parent(r, cg_skel_adj);
		//		    ## *********** End ***********

		return prt;
	}

	//	def get_missingness_index(data: ndarray) -> List[int]:
	//	    """Detect the parents of missingness indicators
	//	    :param data: data set (numpy ndarray)
	//	    :return:
	//	    missingness_index: list, the index of missingness indicators
	//	    """
	//
	//	    missingness_index = []
	//	    _, ncol = np.shape(data)
	//	    for i in range(ncol):
	//	        if np.isnan(data[:, i]).any():
	//	            missingness_index.append(i)
	//	    return missingness_index
	public static TIntList get_missingness_index(double[][] data) {
		//	    """Detect the parents of missingness indicators
		//	    :param data: data set (numpy ndarray)
		//	    :return:
		//	    missingness_index: list, the index of missingness indicators
		//	    """

		TIntList missingness_index = new TIntArrayList();
		int ncol = JavaHelperClasses.getNumberOfColumns(data);
		for (int i = 0; i < ncol; i++) {
			if (JavaHelperClasses.isAnyNan(data, i)) {
				missingness_index.add(i);
			}
		}
		return missingness_index;
	}

	//	def skeleton_correction(data: ndarray, alpha: float, test_with_correction_name: str, init_cg: CausalGraph, prt_m: dict,
	//            stable: bool = True) -> CausalGraph:
	//"""Perform skeleton discovery
	//:param data: data set (numpy ndarray)
	//:param alpha: desired significance level in (0, 1) (float)
	//:param test_with_correction_name: name of the independence test being used
	//- "MV_Crtn_Fisher_Z": Fisher's Z conditional independence test
	//- "MV_Crtn_G_sq": G-squared conditional independence test
	//:param stable: run stabilized skeleton discovery if True (default = True)
	//:return:
	//cg: a CausalGraph object
	//"""
	//
	//assert type(data) == np.ndarray
	//assert 0 < alpha < 1
	//assert test_with_correction_name in ["MV_Crtn_Fisher_Z", "MV_Crtn_G_sq"]
	//
	//## *********** Adaption 1 ***********
	//no_of_var = data.shape[1]
	//
	//## Initialize the graph with the result of test-wise deletion skeletion search
	//cg = init_cg
	//
	//if test_with_correction_name in ["MV_Crtn_Fisher_Z", "MV_Crtn_G_sq"]:
	//cg.set_ind_test(CIT(data, "mc_fisherz"))
	//# No need of the correlation matrix if using test-wise deletion test
	//cg.prt_m = prt_m
	//## *********** Adaption 1 ***********
	//
	//node_ids = range(no_of_var)
	//pair_of_variables = list(permutations(node_ids, 2))
	//
	//depth = -1
	//while cg.max_degree() - 1 > depth:
	//depth += 1
	//edge_removal = []
	//for (x, y) in pair_of_variables:
	//Neigh_x = cg.neighbors(x)
	//if y not in Neigh_x:
	//    continue
	//else:
	//    Neigh_x = np.delete(Neigh_x, np.where(Neigh_x == y))
	//
	//if len(Neigh_x) >= depth:
	//    for S in combinations(Neigh_x, depth):
	//        p = cg.ci_test(x, y, S)
	//        if p > alpha:
	//            if not stable:  # Unstable: Remove x---y right away
	//                edge1 = cg.G.get_edge(cg.G.nodes[x], cg.G.nodes[y])
	//                if edge1 is not None:
	//                    cg.G.remove_edge(edge1)
	//                edge2 = cg.G.get_edge(cg.G.nodes[y], cg.G.nodes[x])
	//                if edge2 is not None:
	//                    cg.G.remove_edge(edge2)
	//            else:  # Stable: x---y will be removed only
	//                edge_removal.append((x, y))  # after all conditioning sets at
	//                edge_removal.append((y, x))  # depth l have been considered
	//                Helper.append_value(cg.sepset, x, y, S)
	//                Helper.append_value(cg.sepset, y, x, S)
	//            break
	//
	//for (x, y) in list(set(edge_removal)):
	//edge1 = cg.G.get_edge(cg.G.nodes[x], cg.G.nodes[y])
	//if edge1 is not None:
	//    cg.G.remove_edge(edge1)
	//
	//return cg
	public static CausalGraph skeleton_correction(double[][] data, double alpha, Correction test_with_correction_name,
			CausalGraph init_cg, Map<String, List<TIntList>> prt_m) {
		boolean stable = true;
		return skeleton_correction(data, alpha, test_with_correction_name, init_cg, prt_m, stable);
	}

	public static CausalGraph skeleton_correction(double[][] data, double alpha, Correction test_with_correction_name,
			CausalGraph init_cg, Map<String, List<TIntList>> prt_m, boolean stable) {
		assert 0 < alpha && alpha < 1;

		//## *********** Adaption 1 ***********
		int no_of_var = JavaHelperClasses.getNumberOfColumns(data);

		//## Initialize the graph with the result of test-wise deletion skeletion search
		CausalGraph cg = init_cg;

		if (test_with_correction_name == Correction.MV_Crtn_Fisher_Z
				|| test_with_correction_name == Correction.MV_Crtn_G_sq) {
			cg.set_ind_test(new CIT(data, IndependenceTest.mc_fisherz));
		}
		//# No need of the correlation matrix if using test-wise deletion test
		cg.prt_m = prt_m;
		//## *********** Adaption 1 ***********

		Iterable<Pair<Integer, Integer>> pair_of_variables = JavaHelperClasses.permutationsPairs(no_of_var);

		int depth = -1;
		while (cg.max_degree() - 1 > depth) {
			depth += 1;
			Set<Pair<Integer, Integer>> edge_removal = new THashSet<>();
			for (Pair<Integer, Integer> t : pair_of_variables) {
				int x = t.getA();
				int y = t.getB();

				TIntList Neigh_x = cg.neighbors(x);
				if (!Neigh_x.contains(y)) {
					continue;
				} else {
					JavaHelperClasses.removeAll(Neigh_x, y);
				}

				if (Neigh_x.size() >= depth) {
					for (TIntList S : JavaHelperClasses.combinations(Neigh_x, depth)) {
						double p = cg.ci_test(x, y, S);
						if (p > alpha) {
							if (!stable) { // # Unstable: Remove x---y right away
								Edge edge1 = cg.G.get_edge(cg.G.nodes.get(x), cg.G.nodes.get(y));
								if (edge1 != null) {
									cg.G.remove_edge(edge1);
								}
								Edge edge2 = cg.G.get_edge(cg.G.nodes.get(y), cg.G.nodes.get(x));
								if (edge2 != null) {
									cg.G.remove_edge(edge2);
								}
							} else { // # Stable: x---y will be removed only
								edge_removal.add(Pair.of(x, y)); // # after all conditioning sets at
								edge_removal.add(Pair.of(y, x)); // # depth l have been considered
								Helper.append_value(cg.sepset, x, y, S);
								Helper.append_value(cg.sepset, y, x, S);
							}
							break;
						}
					}
				}
			}

			for (Pair<Integer, Integer> u : edge_removal) {
				int x = u.getA();
				int y = u.getB();
				Edge edge1 = cg.G.get_edge(cg.G.nodes.get(x), cg.G.nodes.get(y));
				if (edge1 != null) {
					cg.G.remove_edge(edge1);
				}
			}
		}
		return cg;
	}

	//	def get_parent(r: int, cg_skel_adj: ndarray) -> ndarray:
	//	    """Get the neighbors of missingness indicators which are the parents
	//	    :param r: the missingness indicator index
	//	    :param cg_skel_adj: adjacancy matrix of a causal skeleton
	//	    :return:
	//	    prt: list, parents of the missingness indicator r
	//	    """
	//	    num_var = len(cg_skel_adj[0, :])
	//	    indx = np.array([i for i in range(num_var)])
	//	    prt = indx[cg_skel_adj[r, :] == 1]
	//	    return prt
	public static TIntList get_parent(int r, int[][] cg_skel_adj) {
		TIntList prt = new TIntArrayList();
		for (int i = 0; i < cg_skel_adj[r].length; i++) {
			if (cg_skel_adj[r][i] == 1) {
				prt.add(i);
			}
		}
		return prt;
	}
}