package transformer.stmt;

import abstractPattern.Action;
import abstractPattern.Primitive;
import abstractPattern.analysis.PatternType;
import ast.ASTNode;
import ast.Stmt;
import org.javatuples.Pair;
import transformer.Transformer;
import transformer.TransformerArgument;
import transformer.util.IsPossibleJointPointResult;
import transformer.util.RuntimeInfo;
import util.Namespace;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class StmtTrans implements Transformer<Stmt>{
    protected Stmt originalStmt = null;
    protected TransformerArgument originalArgument = null;

    protected Namespace alterNamespace = null;
    protected Collection<Action> actions = null;
    protected RuntimeInfo runtimeInfo = null;
    protected Function<ASTNode, Boolean> ignoreDelegate = null;
    protected Consumer<Pair<Stmt, PatternType>> jointPointDelegate = null;

    public StmtTrans(TransformerArgument argument, Stmt stmt) {
        this.originalStmt = stmt;
        this.originalArgument = argument;

        this.alterNamespace = argument.alterNamespace;
        this.actions = argument.actions;
        this.runtimeInfo = argument.runtimeInfo;
        this.ignoreDelegate = argument.ignoreDelegate;
        this.jointPointDelegate = argument.jointPointDelegate;
    }

    public abstract Pair<Stmt, List<Stmt>> copyAndTransform();

    @Override
    public boolean hasTransformOnCurrentNode() {
        if (ignoreDelegate.apply(originalStmt) == true) return false;
        for (Action action : actions) {
            assert action.getPattern() instanceof Primitive;
            Primitive pattern = (Primitive) action.getPattern();
            IsPossibleJointPointResult query = pattern.isPossibleJointPoint(originalStmt, runtimeInfo);
            if (query.isPossible()) return true;
        }
        return false;
    }

    @Override
    public abstract boolean hasFurtherTransform();
}
