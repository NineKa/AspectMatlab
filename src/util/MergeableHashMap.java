package util;

import java.util.HashMap;
import java.util.Map;

public class MergeableHashMap<K,V> extends HashMap<K, V> implements Mergeable<MergeableHashMap<K, V>> {

    @Override
    public MergeableHashMap<K, V> merge(MergeableHashMap<K, V> target) {    /* using union merge as default */
        return this.union(target);
    }

    @Override
    public MergeableHashMap<K, V> union(MergeableHashMap<K, V> target) {
        MergeableHashMap<K, V> returnMap = new MergeableHashMap<K, V>();
        returnMap.putAll(this);
        for (Map.Entry entry : target.entrySet()) {
            if (returnMap.containsKey(entry.getKey())) {
                assert entry.getValue().equals(returnMap.get(entry.getKey())); /* check conflict */
                continue;
            }
            returnMap.put((K) entry.getKey(), (V) entry.getValue());
        }
        return returnMap;
    }

    @Override
    public MergeableHashMap<K, V> intersection(MergeableHashMap<K, V> target) {
        MergeableHashMap<K, V> returnMap = new MergeableHashMap<K, V>();
        for (Map.Entry entry : this.entrySet()) {
            K key = (K) entry.getKey();
            V value = (V) entry.getValue();
            if (target.containsKey(key)) {
                assert target.get(key).equals(value);                       /* check conflict */
                returnMap.put(key, value);
            }
        }
        for (Map.Entry entry : target.entrySet()) {
            K key = (K) entry.getKey();
            V value = (V) entry.getValue();
            if (this.containsKey(key)) {
                assert this.get(key).equals(value);                         /* check conflict */
                if (returnMap.keySet().contains(key)) continue;
                returnMap.put(key, value);
            }
        }
        return returnMap;
    }
}