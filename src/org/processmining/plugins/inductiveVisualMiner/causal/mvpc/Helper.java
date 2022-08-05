package org.processmining.plugins.inductiveVisualMiner.causal.mvpc;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.processmining.plugins.InductiveMiner.Triple;

import gnu.trove.list.TIntList;
import gnu.trove.set.hash.THashSet;

public class Helper {
	//	def append_value(array, i, j, value):
	//	    """
	//	    Append value to the list at array[i, j]
	//	    """
	//	    if array[i, j] is None:
	//	        array[i, j] = [value]
	//	    else:
	//	        array[i, j].append(value)
	public static <T> void append_value(List<T>[][] array, int i, int j, T value) {
		if (array[i][j] == null) {
			array[i][j] = new ArrayList<>();
			array[i][j].add(value);
		} else {
			array[i][j].add(value);
		}
	}

	//	def sort_dict_ascending(dict, descending=False):
	//	    "Sort dict (dictionary) by its value in ascending order"
	//	    dict_list = sorted(dict.items(), key=lambda x: x[1], reverse=descending)
	//	    return {dict_list[i][0]: dict_list[i][1] for i in range(len(dict_list))}
	public static <A> Map<Triple<Integer, Integer, Integer>, A> sort_dict_ascending(
			Map<Triple<Integer, Integer, Integer>, A> dict) {
		boolean descending = false;
		return sort_dict_ascending(dict, descending);
	}

	public static <A> Map<Triple<Integer, Integer, Integer>, A> sort_dict_ascending(
			Map<Triple<Integer, Integer, Integer>, A> dict, boolean descending) {

		Comparator<Triple<Integer, Integer, Integer>> comparator;
		if (descending) {
			//reverse
			comparator = new Comparator<Triple<Integer, Integer, Integer>>() {
				public int compare(Triple<Integer, Integer, Integer> o1, Triple<Integer, Integer, Integer> o2) {
					return -o1.getB().compareTo(o2.getB());
				}
			};
		} else {
			comparator = new Comparator<Triple<Integer, Integer, Integer>>() {

				public int compare(Triple<Integer, Integer, Integer> o1, Triple<Integer, Integer, Integer> o2) {
					return o1.getB().compareTo(o2.getB());
				}

			};
		}
		return new TreeMap<>(comparator);
	}

	//	def powerset(L):
	//	    """
	//	    Return the powerset of L (list)
	//	    """
	//	    s = list(L)
	//	    return list(chain.from_iterable(combinations(s, r) for r in range(len(s) + 1)))
	public static List<TIntList> powerset(TIntList L) {
		List<TIntList> result = new ArrayList<>();
		for (int r = 0; r < L.size() + 1; r++) {
			for (TIntList x : JavaHelperClasses.combinations(L, r)) {
				result.add(x);
			}
		}
		return result;
	}

	//	def list_union(L1, L2):
	//	    "Return the union of L1 and L2 (lists)"
	//	    return list(set(L1 + L2))
	public static <X> List<X> list_union(List<X> L1, List<X> L2) {
		THashSet<X> set = new THashSet<>(L1);
		set.addAll(L2);
		return new ArrayList<>(set);
	}
}