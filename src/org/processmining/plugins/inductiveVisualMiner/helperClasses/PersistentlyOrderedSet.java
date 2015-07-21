package org.processmining.plugins.inductiveVisualMiner.helperClasses;

import gnu.trove.map.hash.TObjectIntHashMap;

import java.util.ArrayList;
import java.util.List;

/**
 * Keep a set of elements. The index is kept.
 * @author sleemans
 *
 * @param <T>
 */
public class PersistentlyOrderedSet<T, P1, P2> {

	private final TObjectIntHashMap<T> hash;
	private final List<T> list;
	private final List<P1> payloads1;
	private final List<P2> payloads2;
	
	public PersistentlyOrderedSet() {
		hash = new TObjectIntHashMap<>();
		list = new ArrayList<>();
		payloads1 = new ArrayList<>();
		payloads2 = new ArrayList<>();
	}
	
	/**
	 * Adds an object to the list and returns the index at which it was inserted.
	 * @param object
	 * @return
	 */
	public int add(T object, P1 payload1, P2 payload2) {
		int result = hash.putIfAbsent(object, list.size());
		if (result == hash.getNoEntryValue()) {
			list.add(object);
			payloads1.add(payload1);
			payloads2.add(payload2);
			return list.size() - 1;
		}
		return result;
	}
	
	public T get(int index) {
		return list.get(index);
	}
	
	public P1 getPayload1(int index) {
		return payloads1.get(index);
	}
	
	public P2 getPayload2(int index) {
		return payloads2.get(index);
	}
	
	/**
	 * removes temporary storage
	 */
	public void cleanUp() {
		hash.clear();
	}
}
