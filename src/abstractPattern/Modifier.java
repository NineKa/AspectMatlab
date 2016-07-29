package abstractPattern;

import ast.ASTNode;
import transformer.RuntimeInfo;

import java.util.Collection;

public abstract class Modifier extends Pattern{
    public abstract Collection<Modifier> getAllModfiierSet();

    public abstract boolean isPossibleWeave(ASTNode astNode, RuntimeInfo runtimeInfo);
}
