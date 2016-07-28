package abstractPattern;

import abstractPattern.utility.RuntimeInfo;
import ast.ASTNode;

import java.util.Collection;

public abstract class Modifier extends Pattern{
    public abstract Collection<Modifier> getAllModfiierSet();

    public abstract boolean isPossibleWeave(ASTNode astNode, RuntimeInfo runtimeInfo);
}
