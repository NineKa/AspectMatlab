package util;

import java.util.HashSet;

public class MergeableHashSet<T> extends HashSet<T> implements MergeableCollection<T>{
    @Override
    public MergeableCollection<T> merge(MergeableCollection<T> target) {
        MergeableCollection<T> returnCollection = new MergeableHashSet<>();
        for (T iter : this) { returnCollection.add(iter); }
        for (T iter : target) {
            if (returnCollection.contains(iter)) continue;
            returnCollection.add(iter);
        }
        return returnCollection;
    }

    @Override
    public MergeableCollection<T> intersection(MergeableCollection<T> target) {
        MergeableCollection<T> collection = new MergeableHashSet<T>();
        for (T obj : this) if (target.contains(obj) && !collection.contains(obj)) collection.add(obj);
        for (T obj : target) if (this.contains(obj) && !collection.contains(obj)) collection.add(obj);
        return collection;
    }

    @Override
    public MergeableCollection<T> union(MergeableCollection<T> target) {
        MergeableCollection<T> collection = new MergeableHashSet<T>();
        for (T obj : this)   if (!collection.contains(obj)) collection.add(obj);
        for (T obj : target) if (!collection.contains(obj)) collection.add(obj);
        return collection;
    }
}
