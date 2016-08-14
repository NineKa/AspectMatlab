package abstractPattern.analysis;

import ast.*;

public enum PatternType {
    Within, IsType, Dimension, Call, Execution, Loop, LoopBody, LoopHead, Annotate, Get, Set, Operator, MainExecution;

    public static PatternType fromASTNode(ASTNode astNode) {
        if (astNode instanceof PatternWithin)        return Within;
        if (astNode instanceof PatternIsType)        return IsType;
        if (astNode instanceof PatternDimension)     return Dimension;

        if (astNode instanceof PatternCall)          return Call;
        if (astNode instanceof PatternExecution)     return Execution;
        if (astNode instanceof PatternLoop)          return Loop;
        if (astNode instanceof PatternLoopBody)      return LoopBody;
        if (astNode instanceof PatternLoopHead)      return LoopHead;
        if (astNode instanceof PatternAnnotate)      return Annotate;
        if (astNode instanceof PatternGet)           return Get;
        if (astNode instanceof PatternSet)           return Set;
        if (astNode instanceof PatternOperator)      return Operator;
        if (astNode instanceof PatternMainExecution) return MainExecution;

        /* control flow should not reach here */
        throw new AssertionError();
    }
}
