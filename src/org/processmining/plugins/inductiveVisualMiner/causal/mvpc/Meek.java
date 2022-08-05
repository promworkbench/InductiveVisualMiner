package org.processmining.plugins.inductiveVisualMiner.causal.mvpc;

import org.processmining.plugins.InductiveMiner.Quadruple;
import org.processmining.plugins.InductiveMiner.Triple;

public class Meek {
	//	def meek(cg: CausalGraph, background_knowledge: BackgroundKnowledge | None = None) -> CausalGraph:
	//	    """
	//	    Run Meek rules
	//
	//	    Parameters
	//	    ----------
	//	    cg : a CausalGraph object. Where cg.G.graph[j,i]=1 and cg.G.graph[i,j]=-1 indicates  i --> j ,
	//	                    cg.G.graph[i,j] = cg.G.graph[j,i] = -1 indicates i --- j,
	//	                    cg.G.graph[i,j] = cg.G.graph[j,i] = 1 indicates i <-> j.
	//	    background_knowledge : artificial background background_knowledge
	//
	//	    Returns
	//	    -------
	//	    cg_new : a CausalGraph object. Where cg_new.G.graph[j,i]=1 and cg_new.G.graph[i,j]=-1 indicates  i --> j ,
	//	                    cg_new.G.graph[i,j] = cg_new.G.graph[j,i] = -1 indicates i --- j,
	//	                    cg_new.G.graph[i,j] = cg_new.G.graph[j,i] = 1 indicates i <-> j.
	//	    """
	//
	//	    cg_new = deepcopy(cg)
	//
	//	    UT = cg_new.find_unshielded_triples()
	//	    Tri = cg_new.find_triangles()
	//	    Kite = cg_new.find_kites()
	//
	//	    loop = True
	//
	//	    while loop:
	//	        loop = False
	//	        for (i, j, k) in UT:
	//	            if cg_new.is_fully_directed(i, j) and cg_new.is_undirected(j, k):
	//	                if (background_knowledge is not None) and \
	//	                        (background_knowledge.is_forbidden(cg_new.G.nodes[j], cg_new.G.nodes[k]) or
	//	                         background_knowledge.is_required(cg_new.G.nodes[k], cg_new.G.nodes[j])):
	//	                    pass
	//	                else:
	//	                    edge1 = cg_new.G.get_edge(cg_new.G.nodes[j], cg_new.G.nodes[k])
	//	                    if edge1 is not None:
	//	                        if cg_new.G.is_ancestor_of(cg_new.G.nodes[k], cg_new.G.nodes[j]):
	//	                            continue
	//	                        else:
	//	                            cg_new.G.remove_edge(edge1)
	//	                    else:
	//	                        continue
	//	                    cg_new.G.add_edge(Edge(cg_new.G.nodes[j], cg_new.G.nodes[k], Endpoint.TAIL, Endpoint.ARROW))
	//	                    loop = True
	//
	//	        for (i, j, k) in Tri:
	//	            if cg_new.is_fully_directed(i, j) and cg_new.is_fully_directed(j, k) and cg_new.is_undirected(i, k):
	//	                if (background_knowledge is not None) and \
	//	                        (background_knowledge.is_forbidden(cg_new.G.nodes[i], cg_new.G.nodes[k]) or
	//	                         background_knowledge.is_required(cg_new.G.nodes[k], cg_new.G.nodes[i])):
	//	                    pass
	//	                else:
	//	                    edge1 = cg_new.G.get_edge(cg_new.G.nodes[i], cg_new.G.nodes[k])
	//	                    if edge1 is not None:
	//	                        if cg_new.G.is_ancestor_of(cg_new.G.nodes[k], cg_new.G.nodes[i]):
	//	                            continue
	//	                        else:
	//	                            cg_new.G.remove_edge(edge1)
	//	                    else:
	//	                        continue
	//	                    cg_new.G.add_edge(Edge(cg_new.G.nodes[i], cg_new.G.nodes[k], Endpoint.TAIL, Endpoint.ARROW))
	//	                    loop = True
	//
	//	        for (i, j, k, l) in Kite:
	//	            if cg_new.is_undirected(i, j) and cg_new.is_undirected(i, k) and cg_new.is_fully_directed(j, l) \
	//	                    and cg_new.is_fully_directed(k, l) and cg_new.is_undirected(i, l):
	//	                if (background_knowledge is not None) and \
	//	                        (background_knowledge.is_forbidden(cg_new.G.nodes[i], cg_new.G.nodes[l]) or
	//	                         background_knowledge.is_required(cg_new.G.nodes[l], cg_new.G.nodes[i])):
	//	                    pass
	//	                else:
	//	                    edge1 = cg_new.G.get_edge(cg_new.G.nodes[i], cg_new.G.nodes[l])
	//	                    if edge1 is not None:
	//	                        if cg_new.G.is_ancestor_of(cg_new.G.nodes[l], cg_new.G.nodes[i]):
	//	                            continue
	//	                        else:
	//	                            cg_new.G.remove_edge(edge1)
	//	                    else:
	//	                        continue
	//	                    cg_new.G.add_edge(Edge(cg_new.G.nodes[i], cg_new.G.nodes[l], Endpoint.TAIL, Endpoint.ARROW))
	//	                    loop = True
	//
	//	    return cg_new

	public static CausalGraph meek(CausalGraph cg, BackgroundKnowledge background_knowledge) {

		CausalGraph cg_new = cg.deepcopy();

		Iterable<Triple<Integer, Integer, Integer>> UT = cg_new.find_unshielded_triples();
		Iterable<Triple<Integer, Integer, Integer>> Tri = cg_new.find_triangles();
		Iterable<Quadruple<Integer, Integer, Integer, Integer>> Kite = cg_new.find_kites();

		boolean loop = true;

		while (loop) {
			loop = false;
			for (Triple<Integer, Integer, Integer> t : UT) {
				int i = t.getA();
				int j = t.getB();
				int k = t.getC();

				if (cg_new.is_fully_directed(i, j) && cg_new.is_undirected(j, k)) {
					if ((background_knowledge != null) && (background_knowledge.is_forbidden(cg_new.G.nodes.get(j),
							cg_new.G.nodes.get(k))
							|| background_knowledge.is_required(cg_new.G.nodes.get(k), cg_new.G.nodes.get(j)))) {

					} else {
						Edge edge1 = cg_new.G.get_edge(cg_new.G.nodes.get(j), cg_new.G.nodes.get(k));
						if (edge1 != null) {
							if (cg_new.G.is_ancestor_of(cg_new.G.nodes.get(k), cg_new.G.nodes.get(j))) {
								continue;
							} else {
								cg_new.G.remove_edge(edge1);
							}
						} else {
							continue;
						}
						cg_new.G.add_edge(
								new Edge(cg_new.G.nodes.get(j), cg_new.G.nodes.get(k), Endpoint.TAIL, Endpoint.ARROW));
						loop = true;
					}
				}
			}

			for (Triple<Integer, Integer, Integer> t : Tri) {
				int i = t.getA();
				int j = t.getB();
				int k = t.getC();

				if (cg_new.is_fully_directed(i, j) && cg_new.is_fully_directed(j, k) && cg_new.is_undirected(i, k)) {
					if ((background_knowledge != null) && (background_knowledge.is_forbidden(cg_new.G.nodes.get(i),
							cg_new.G.nodes.get(k))
							|| background_knowledge.is_required(cg_new.G.nodes.get(k), cg_new.G.nodes.get(i)))) {

					} else {
						Edge edge1 = cg_new.G.get_edge(cg_new.G.nodes.get(i), cg_new.G.nodes.get(k));
						if (edge1 != null) {
							if (cg_new.G.is_ancestor_of(cg_new.G.nodes.get(k), cg_new.G.nodes.get(i))) {
								continue;
							} else {
								cg_new.G.remove_edge(edge1);
							}
						} else {
							continue;
						}
						cg_new.G.add_edge(
								new Edge(cg_new.G.nodes.get(i), cg_new.G.nodes.get(k), Endpoint.TAIL, Endpoint.ARROW));
						loop = true;
					}
				}
			}

			for (Quadruple<Integer, Integer, Integer, Integer> q : Kite) {
				int i = q.getA();
				int j = q.getB();
				int k = q.getC();
				int l = q.getD();

				if (cg_new.is_undirected(i, j) && cg_new.is_undirected(i, k) && cg_new.is_fully_directed(j, l)
						&& cg_new.is_fully_directed(k, l) && cg_new.is_undirected(i, l)) {
					if ((background_knowledge != null) && (background_knowledge.is_forbidden(cg_new.G.nodes.get(i),
							cg_new.G.nodes.get(l))
							|| background_knowledge.is_required(cg_new.G.nodes.get(l), cg_new.G.nodes.get(i)))) {

					} else {
						Edge edge1 = cg_new.G.get_edge(cg_new.G.nodes.get(i), cg_new.G.nodes.get(l));
						if (edge1 != null) {
							if (cg_new.G.is_ancestor_of(cg_new.G.nodes.get(l), cg_new.G.nodes.get(i))) {
								continue;
							} else {
								cg_new.G.remove_edge(edge1);
							}
						} else {
							continue;
						}
						cg_new.G.add_edge(
								new Edge(cg_new.G.nodes.get(i), cg_new.G.nodes.get(l), Endpoint.TAIL, Endpoint.ARROW));
						loop = true;
					}
				}
			}
		}

		return cg_new;
	}

	//def definite_meek(cg: CausalGraph, background_knowledge: BackgroundKnowledge | None = None) -> CausalGraph:
	//    """
	//    Run Meek rules over the definite unshielded triples
	//
	//    Parameters
	//    ----------
	//    cg : a CausalGraph object. Where cg.G.graph[j,i]=1 and cg.G.graph[i,j]=-1 indicates  i --> j ,
	//                    cg.G.graph[i,j] = cg.G.graph[j,i] = -1 indicates i --- j,
	//                    cg.G.graph[i,j] = cg.G.graph[j,i] = 1 indicates i <-> j.
	//    background_knowledge : artificial background background_knowledge
	//
	//    Returns
	//    -------
	//    cg_new : a CausalGraph object. Where cg_new.G.graph[j,i]=1 and cg_new.G.graph[i,j]=-1 indicates  i --> j ,
	//                    cg_new.G.graph[i,j] = cg_new.G.graph[j,i] = -1 indicates i --- j,
	//                    cg_new.G.graph[i,j] = cg_new.G.graph[j,i] = 1 indicates i <-> j.
	//    """
	//
	//    cg_new = deepcopy(cg)
	//
	//    Tri = cg_new.find_triangles()
	//    Kite = cg_new.find_kites()
	//
	//    loop = True
	//
	//    while loop:
	//        loop = False
	//        for (i, j, k) in cg_new.definite_non_UC:
	//            if cg_new.is_fully_directed(i, j) and \
	//                    cg_new.is_undirected(j, k) and \
	//                    not ((background_knowledge is not None) and
	//                         (background_knowledge.is_forbidden(cg_new.G.nodes[j], cg_new.G.nodes[k]) or
	//                          background_knowledge.is_required(cg_new.G.nodes[k], cg_new.G.nodes[j]))):
	//                edge1 = cg_new.G.get_edge(cg_new.G.nodes[j], cg_new.G.nodes[k])
	//                if edge1 is not None:
	//                    if cg_new.G.is_ancestor_of(cg_new.G.nodes[k], cg_new.G.nodes[j]):
	//                        continue
	//                    else:
	//                        cg_new.G.remove_edge(edge1)
	//                else:
	//                    continue
	//                cg_new.G.add_edge(Edge(cg_new.G.nodes[j], cg_new.G.nodes[k], Endpoint.TAIL, Endpoint.ARROW))
	//                loop = True
	//            elif cg_new.is_fully_directed(k, j) and \
	//                    cg_new.is_undirected(j, i) and \
	//                    not ((background_knowledge is not None) and
	//                         (background_knowledge.is_forbidden(cg_new.G.nodes[j], cg_new.G.nodes[i]) or
	//                          background_knowledge.is_required(cg_new.G.nodes[i], cg_new.G.nodes[j]))):
	//                edge1 = cg_new.G.get_edge(cg_new.G.nodes[j], cg_new.G.nodes[i])
	//                if edge1 is not None:
	//                    if cg_new.G.is_ancestor_of(cg_new.G.nodes[i], cg_new.G.nodes[j]):
	//                        continue
	//                    else:
	//                        cg_new.G.remove_edge(edge1)
	//                else:
	//                    continue
	//                cg_new.G.add_edge(Edge(cg_new.G.nodes[j], cg_new.G.nodes[i], Endpoint.TAIL, Endpoint.ARROW))
	//                loop = True
	//
	//        for (i, j, k) in Tri:
	//            if cg_new.is_fully_directed(i, j) and cg_new.is_fully_directed(j, k) and cg_new.is_undirected(i, k):
	//                if (background_knowledge is not None) and \
	//                        (background_knowledge.is_forbidden(cg_new.G.nodes[i], cg_new.G.nodes[k]) or
	//                         background_knowledge.is_required(cg_new.G.nodes[k], cg_new.G.nodes[i])):
	//                    pass
	//                else:
	//                    edge1 = cg_new.G.get_edge(cg_new.G.nodes[i], cg_new.G.nodes[k])
	//                    if edge1 is not None:
	//                        if cg_new.G.is_ancestor_of(cg_new.G.nodes[k], cg_new.G.nodes[i]):
	//                            continue
	//                        else:
	//                            cg_new.G.remove_edge(edge1)
	//                    else:
	//                        continue
	//                    cg_new.G.add_edge(Edge(cg_new.G.nodes[i], cg_new.G.nodes[k], Endpoint.TAIL, Endpoint.ARROW))
	//                    loop = True
	//
	//        for (i, j, k, l) in Kite:
	//            if ((j, l, k) in cg_new.definite_UC or (k, l, j) in cg_new.definite_UC) \
	//                    and ((j, i, k) in cg_new.definite_non_UC or (k, i, j) in cg_new.definite_non_UC) \
	//                    and cg_new.is_undirected(i, l):
	//                if (background_knowledge is not None) and \
	//                        (background_knowledge.is_forbidden(cg_new.G.nodes[i], cg_new.G.nodes[l]) or
	//                         background_knowledge.is_required(cg_new.G.nodes[l], cg_new.G.nodes[i])):
	//                    pass
	//                else:
	//                    edge1 = cg_new.G.get_edge(cg_new.G.nodes[i], cg_new.G.nodes[l])
	//                    if edge1 is not None:
	//                        if cg_new.G.is_ancestor_of(cg_new.G.nodes[l], cg_new.G.nodes[i]):
	//                            continue
	//                        else:
	//                            cg_new.G.remove_edge(edge1)
	//                    else:
	//                        continue
	//                    cg_new.G.add_edge(Edge(cg_new.G.nodes[i], cg_new.G.nodes[l], Endpoint.TAIL, Endpoint.ARROW))
	//                    loop = True
	//
	//    return cg_new
	public static CausalGraph definite_meek(CausalGraph cg, BackgroundKnowledge background_knowledge) {
		throw new RuntimeException("not implemented");
	}
}
