package matcher;

import ast.Expr;

public abstract class AbstractAlphabetCompareFunc<T> {
    public abstract Expr getCompareFunc (Expr pCompareIter, T pCompareTarget);
}
