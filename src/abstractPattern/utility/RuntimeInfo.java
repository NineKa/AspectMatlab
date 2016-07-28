package abstractPattern.utility;

import abstractPattern.type.ScopeType;
import ast.Stmt;
import natlab.toolkits.analysis.varorfun.VFAnalysis;
import org.javatuples.Pair;

import java.util.Map;
import java.util.Stack;

public class RuntimeInfo {
    public VFAnalysis kindAnalysis = null;
    public Stack<Pair<ScopeType, String>> scopeTraceStack = null;
    public Map<Stmt, String> loopResolveMap = null;

    public RuntimeInfo() { /* default constructor */ }

    public RuntimeInfo(
            VFAnalysis kindAnalysis,
            Stack<Pair<ScopeType, String>> scopeTraceStack,
            Map<Stmt, String> loopResolveMap
    ) {
        this.kindAnalysis = kindAnalysis;
        this.scopeTraceStack = scopeTraceStack;
        this.loopResolveMap = loopResolveMap;
    }

    public void setKindAnalysis(VFAnalysis kindAnalysis) {
        this.kindAnalysis = kindAnalysis;
    }

    public void setScopeTraceStack(Stack<Pair<ScopeType, String>> scopeTraceStack) {
        this.scopeTraceStack = scopeTraceStack;
    }

    public void setLoopResolveMap(Map<Stmt, String> loopResolveMap) {
        this.loopResolveMap = loopResolveMap;
    }

    public boolean withinScope(ScopeType type, String name) {
        for (Pair<ScopeType, String> scope : this.scopeTraceStack) {
            if (scope.getValue0().equals(type) && scope.getValue1().equals(name)) return true;
        }
        return false;
    }

    public VFAnalysis getKindAnalysis() {
        return kindAnalysis;
    }

    public Stack<Pair<ScopeType, String>> getScopeTraceStack() {
        return scopeTraceStack;
    }

    public Map<Stmt, String> getLoopResolveMap() {
        return loopResolveMap;
    }
}
