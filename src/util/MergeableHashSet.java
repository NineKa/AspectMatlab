package util;

import java.util.HashSet;

public class MergeableHashSet<T> extends HashSet<T> implements MergeableCollection<T>{
    public MergeableCollection<T> merge(MergeableCollection<T> target) {
        MergeableCollection<T> returnCollection = new MergeableHashSet<>();
        for (T iter : this) { returnCollection.add(iter); }
        for (T iter : target) {
            if (returnCollection.contains(iter)) continue;
            returnCollection.add(iter);
        }
        return returnCollection;
    }
}
