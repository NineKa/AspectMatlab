package util;

import java.util.Collection;

public interface MergeableCollection<T> extends Collection<T> {
    public MergeableCollection<T> merge(MergeableCollection<T> target);
}
