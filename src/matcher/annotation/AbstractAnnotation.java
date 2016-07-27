package matcher.annotation;

import ast.Expr;
import ast.List;

import java.util.Iterator;

public class AbstractAnnotation implements Iterable<List<Expr>>{
    private String annotationName;
    private ast.List<ast.List<Expr>> annotationArgs;

    public AbstractAnnotation(String annotationName, ast.List<ast.List<Expr>> annotationArgs) {
        this.annotationName = annotationName;
        this.annotationArgs = annotationArgs;
    }

    public String getAnnotationName() {
        return annotationName;
    }

    public List<List<Expr>> getAnnotationArgs() {
        return annotationArgs;
    }

    @Override
    public Iterator<List<Expr>> iterator() {
        return this.annotationArgs.iterator();
    }

    @Override
    public String toString() {
        String elemStr = "";
        String argsStr = "";
        for (int argsIter = 0; argsIter < this.annotationArgs.getNumChild(); argsIter++) {
            for (int elemIter = 0; elemIter < this.annotationArgs.getChild(argsIter).getNumChild(); elemIter++) {
                elemStr = elemStr + this.annotationArgs.getChild(argsIter).getChild(elemIter).getPrettyPrinted();
                if (elemIter + 1 < this.annotationArgs.getChild(argsIter).getNumChild()) {
                    elemStr = elemStr + ", ";
                }
            }
            argsStr = argsStr + "[" + elemStr + "]";
            elemStr = "";
            if (argsIter + 1 < this.annotationArgs.getNumChild()) {
                argsStr = argsStr + ", ";
            }
        }
        return String.format("%%@%s %s", this.annotationName, argsStr);
    }
}
