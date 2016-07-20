package abstractPattern.analysis;

import abstractPattern.Modifier;
import abstractPattern.Primitive;
import abstractPattern.modifier.*;
import abstractPattern.primitive.*;
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

    public static Modifier buildModifier(Expr expr) {
        if (expr instanceof PatternIsType) return new IsType((PatternIsType)expr);
        if (expr instanceof PatternDimension) return new Dimension((PatternDimension)expr);
        if (expr instanceof PatternWithin) return new Within((PatternWithin)expr);
        if (expr instanceof AndExpr) return new ModifierAnd((AndExpr)expr);
        if (expr instanceof OrExpr) return new ModifierOr((OrExpr)expr);
        if (expr instanceof NotExpr) return new ModifierNot((NotExpr)expr);
        /* control flow should not reach here */
        throw new RuntimeException();
    }

    public static Primitive buildBasicPrimitive(Expr expr) {
        if (expr instanceof PatternAnnotate) return new Annotate((PatternAnnotate)expr);
        if (expr instanceof PatternCall) return new Call((PatternCall)expr);
        if (expr instanceof PatternExecution) return new Execution((PatternExecution)expr);
        if (expr instanceof PatternGet) return new Get((PatternGet)expr);
        if (expr instanceof PatternLoop) return new Loop((PatternLoop)expr);
        if (expr instanceof PatternLoopBody) return new LoopBody((PatternLoopBody)expr);
        if (expr instanceof PatternLoopHead) return new LoopHead((PatternLoopHead)expr);
        if (expr instanceof PatternMainExecution) return new MainExecution((PatternMainExecution)expr);
        if (expr instanceof PatternOperator) return new Operator((PatternOperator)expr);
        if (expr instanceof PatternSet) return new Set((PatternSet)expr);

        throw new RuntimeException();
    }
}
