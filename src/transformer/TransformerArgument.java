package transformer;

import abstractPattern.Action;
import ast.ASTNode;
import ast.AssignStmt;
import ast.EndExpr;
import ast.Expr;
import transformer.joinpoint.AMJoinPoint;
import transformer.util.RuntimeInfo;
import util.Namespace;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.function.Consumer;
import java.util.function.Function;

public class TransformerArgument {
    /* A collection of all actions of aspects */
    public Collection<Action> actions = null;
    /* A runtime information gathered during statement transformation */
    public RuntimeInfo runtimeInfo = null;
    /* A namespace use to generate temporary variable */
    public Namespace alterNamespace = null;
    /* A function decide if a node transformation is ignored (TRUE -> ignored) */
    public Function<ASTNode, Boolean> ignoreDelegate = null;
    /* A call back function, if a joint point is appearing, a call back is issued */
    public Consumer<AMJoinPoint> jointPointDelegate = null;

    public String enclosingFilename = null;

    public Map<EndExpr, Expr> endExpressionResolveMap = new HashMap<>();

    @Deprecated
    public Stack<AssignStmt> assignRetriveStack = new Stack<>();

    public TransformerArgument(
            Collection<Action> actions,
            RuntimeInfo runtimeInfo,
            Namespace alterNamespace,
            Function<ASTNode, Boolean> ignoreDelegate,
            Consumer<AMJoinPoint> jointPointDelegate,
            String enclosingFilename
    ) {
        this.actions = actions;
        this.runtimeInfo = runtimeInfo;
        this.alterNamespace = alterNamespace;
        this.ignoreDelegate = ignoreDelegate;
        this.jointPointDelegate = jointPointDelegate;
        this.enclosingFilename = enclosingFilename;
    }

    public TransformerArgument copy() {
        TransformerArgument retArgument = new TransformerArgument(
                actions,
                runtimeInfo.copy(),
                alterNamespace,
                ignoreDelegate,
                jointPointDelegate,
                enclosingFilename
        );

        retArgument.endExpressionResolveMap = endExpressionResolveMap;

        return retArgument;
    }
}
