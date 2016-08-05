package transformer.util;

import abstractPattern.type.ScopeType;
import ast.EmptyStmt;
import ast.HelpComment;
import ast.Stmt;
import natlab.toolkits.analysis.varorfun.VFAnalysis;
import org.javatuples.Pair;

import java.util.Map;
import java.util.Stack;

public class RuntimeInfo {
    public VFAnalysis kindAnalysis = null;
    public Stack<Pair<ScopeType, String>> scopeTraceStack = null;
    public Map<Stmt, String> loopNameResolveMap = null;
    public AccessMode accessMode = null;
    public Map<EmptyStmt, HelpComment> annotationMap = null;
}
