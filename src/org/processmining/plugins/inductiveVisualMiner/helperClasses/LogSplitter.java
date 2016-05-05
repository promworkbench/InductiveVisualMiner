package org.processmining.plugins.inductiveVisualMiner.helperClasses;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.common.collect.FluentIterable;

public class LogSplitter {
	
	/**
	 * Holds information for log splitting
	 * @author sleemans
	 *
	 * @param <X>
	 */
	public static class SigmaMaps <X> {
		public List<TIntSet> partition = new ArrayList<>();
		public List<List<X>> sublogs = new ArrayList<List<X>>();
		public HashMap<TIntSet, List<X>> mapSigma2subtrace = new HashMap<>();
		public TIntObjectMap<TIntSet> mapUnode2sigma= new TIntObjectHashMap<>(10, 0.5f, -1);
	}

	/**
	 * Provides the information to split a log
	 * @param unode
	 * @return
	 */
	public static <X> SigmaMaps<X> makeSigmaMaps(
			IvMEfficientTree tree, int unode) {
		SigmaMaps<X> r = new SigmaMaps<X>();
		
		//make a partition
		for (int child : tree.getChildren(unode)) {
			TIntHashSet part = new TIntHashSet(10, 0.5f, -1);
			part.addAll(FluentIterable.from(tree.getAllNodes(child)).toList());
			r.partition.add(part);
		}

		//map activities to sigmas
		for (TIntSet sigma : r.partition) {
			List<X> subtrace = new ArrayList<X>();
			r.sublogs.add(subtrace);
			r.mapSigma2subtrace.put(sigma, subtrace);
			for (int unode2 : sigma.toArray()) {
				r.mapUnode2sigma.put(unode2, sigma);
			}
		}
		
		return r;
	}
}
