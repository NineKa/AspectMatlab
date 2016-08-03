package util;

import java.util.Collection;

public interface MergeableCollection<T> extends Collection<T>, Mergeable<MergeableCollection<T>> {
    /* nothing here */
}
