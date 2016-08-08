package transformer.expr;

import abstractPattern.Action;
import ast.ASTNode;
import ast.Stmt;
import transformer.util.RuntimeInfo;
import util.Namespace;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;

public class ExprTransArgument {
    /* A collection of all actions of aspects */
    public Collection<Action> actions = null;
    /* A runtime information gathered during statement transformation */
    public RuntimeInfo runtimeInfo = null;
    /* A namespace use to generate temporary variable */
    public Namespace alterNamespace = null;
    /* A function decide if a node transformation is ignored (TRUE -> ignored) */
    public Function<ASTNode, Boolean> ignoreDelegate = null;
    /* A call back function, if a joint point is appearing, a call back is issued */
    public Consumer<Stmt> jointPointDelegate = null;

    public ExprTransArgument(
            Collection<Action> actions,
            RuntimeInfo runtimeInfo,
            Namespace alterNamespace,
            Function<ASTNode, Boolean> ignoreDelegate,
            Consumer<Stmt> jointPointDelegate
    ) {
        this.actions = actions;
        this.runtimeInfo = runtimeInfo;
        this.alterNamespace = alterNamespace;
        this.ignoreDelegate = ignoreDelegate;
        this.jointPointDelegate = jointPointDelegate;
    }
}
