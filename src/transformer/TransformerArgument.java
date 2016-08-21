package transformer;

import abstractPattern.Action;
import abstractPattern.analysis.PatternType;
import ast.*;
import org.javatuples.Pair;
import transformer.util.RuntimeInfo;
import util.Namespace;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
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
    public Consumer<Pair<Stmt, PatternType>> jointPointDelegate = null;

    public Map<Stmt, CellArrayExpr> variableIndiceMap = new HashMap<>();
    public Map<EndExpr, ParameterizedExpr> endExpressionResolveMap = new HashMap<>();

    public TransformerArgument(
            Collection<Action> actions,
            RuntimeInfo runtimeInfo,
            Namespace alterNamespace,
            Function<ASTNode, Boolean> ignoreDelegate,
            Consumer<Pair<Stmt, PatternType>> jointPointDelegate,
            Map<Stmt, CellArrayExpr> variableIndiceMap
    ) {
        this.actions = actions;
        this.runtimeInfo = runtimeInfo;
        this.alterNamespace = alterNamespace;
        this.ignoreDelegate = ignoreDelegate;
        this.jointPointDelegate = jointPointDelegate;
        this.variableIndiceMap = variableIndiceMap;
    }

    public TransformerArgument copy() {
        TransformerArgument retArgument = new TransformerArgument(
                actions,
                runtimeInfo.copy(),
                alterNamespace,
                ignoreDelegate,
                jointPointDelegate,
                variableIndiceMap
        );
        return retArgument;
    }
}
