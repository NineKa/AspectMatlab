package abstractPattern.analysis;

import ast.*;

public class PatternClassifier {
    public static boolean isBasicPatternPrimitive(Expr expr) {
        if (expr instanceof PatternWithin)        return false;
        if (expr instanceof PatternIsType)        return false;
        if (expr instanceof PatternDimension)     return false;

        if (expr instanceof PatternCall)          return true;
        if (expr instanceof PatternExecution)     return true;
        if (expr instanceof PatternLoop)          return true;
        if (expr instanceof PatternLoopBody)      return true;
        if (expr instanceof PatternLoopHead)      return true;
        if (expr instanceof PatternAnnotate)      return true;
        if (expr instanceof PatternGet)           return true;
        if (expr instanceof PatternSet)           return true;
        if (expr instanceof PatternOperator)      return true;
        if (expr instanceof PatternMainExecution) return true;

        throw new RuntimeException();
    }

    public static boolean isBasicPatternModifier(Expr expr) {
        if (expr instanceof PatternWithin)        return true;
        if (expr instanceof PatternIsType)        return true;
        if (expr instanceof PatternDimension)     return true;

        if (expr instanceof PatternCall)          return false;
        if (expr instanceof PatternExecution)     return false;
        if (expr instanceof PatternLoop)          return false;
        if (expr instanceof PatternLoopBody)      return false;
        if (expr instanceof PatternLoopHead)      return false;
        if (expr instanceof PatternAnnotate)      return false;
        if (expr instanceof PatternGet)           return false;
        if (expr instanceof PatternSet)           return false;
        if (expr instanceof PatternOperator)      return false;
        if (expr instanceof PatternMainExecution) return false;

        throw new RuntimeException();
    }

    public static boolean isBasicPattern(Expr expr) {
        if (expr instanceof PatternWithin)        return true;
        if (expr instanceof PatternIsType)        return true;
        if (expr instanceof PatternDimension)     return true;
        if (expr instanceof PatternCall)          return true;
        if (expr instanceof PatternExecution)     return true;
        if (expr instanceof PatternLoop)          return true;
        if (expr instanceof PatternLoopBody)      return true;
        if (expr instanceof PatternLoopHead)      return true;
        if (expr instanceof PatternAnnotate)      return true;
        if (expr instanceof PatternGet)           return true;
        if (expr instanceof PatternSet)           return true;
        if (expr instanceof PatternOperator)      return true;
        if (expr instanceof PatternMainExecution) return true;

        return false;
    }
}
