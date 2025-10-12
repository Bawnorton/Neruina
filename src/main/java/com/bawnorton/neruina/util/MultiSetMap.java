package com.bawnorton.neruina.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class MultiSetMap<K, V> {
	private final Map<K, Set<V>> map = new HashMap<>();

	public void put(K key, V value) {
		Set<V> set = map.get(key);
		if (set == null) {
			set = new HashSet<>();
		}
		set.add(value);
		map.put(key, set);
	}

	public void remove(K key, V value) {
		Set<V> set = map.get(key);
		if (set != null) {
			set.remove(value);
		}
	}

	public boolean contains(K key, V value) {
		Set<V> set = map.get(key);
		return set != null && set.contains(value);
	}
}
