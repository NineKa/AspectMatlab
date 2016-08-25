package transformer.joinpoint;

import abstractPattern.Action;
import abstractPattern.analysis.PatternType;
import ast.Stmt;

import java.util.Collection;
import java.util.HashSet;

public abstract class AMJoinPoint {
    protected Stmt inlineStatement = null;
    protected int startLine = -1;
    protected int startColumn = -1;
    protected Collection<Action> matchedActions = new HashSet<>();
    protected String filepath = null;

    public AMJoinPoint(Stmt inlineStatement, int startLine, int startColumn, String filepath) {
        this.inlineStatement = inlineStatement;
        this.startLine = startLine;
        this.startColumn = startColumn;
        this.filepath = filepath;
    }

    public Stmt getInlineStatement() {
        return inlineStatement;
    }

    public int getStartLine() {
        return startLine;
    }

    public int getStartColumn() {
        return startColumn;
    }

    public String getEnclosingFileName() {
        return this.filepath;
    }

    public abstract PatternType getMatchedPatternType();

    public Collection<Action> getMatchedActions() {
        return matchedActions;
    }

    public void addMatchedAction(Action action) {
        this.matchedActions.add(action);
    }

    public void addAllMatchedAction(Collection<? extends Action> collection) {
        this.matchedActions.addAll(collection);
    }
}
