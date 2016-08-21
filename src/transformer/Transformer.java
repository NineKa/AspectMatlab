package transformer;

import ast.Stmt;
import org.javatuples.Pair;

import java.util.List;

public interface Transformer<T> {

    Pair<T, List<Stmt>> copyAndTransform();
    boolean hasFurtherTransform();
    boolean hasTransformOnCurrentNode();

}
