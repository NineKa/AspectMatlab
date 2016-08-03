package util;

public interface Mergeable<T> {
    public T merge(T target);
    public T intersection(T target);
    public T union(T target);
}
