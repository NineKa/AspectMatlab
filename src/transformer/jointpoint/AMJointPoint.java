package transformer.jointpoint;

import abstractPattern.analysis.PatternType;
import abstractPattern.type.ScopeType;
import ast.Stmt;
import org.javatuples.Pair;

import java.util.Stack;

public abstract class AMJointPoint {
    public abstract Stmt getRelativeStmt();

    public abstract int getStartLine();
    public abstract int getStartColumn();
    public abstract Stack<Pair<ScopeType, String>> getScopeTrace();

    public abstract PatternType getMatchedPatternType();
    public abstract String getEnclosedFileName();
}
