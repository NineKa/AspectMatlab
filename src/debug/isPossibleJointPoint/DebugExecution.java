package debug.isPossibleJointPoint;

import Matlab.Nodes.UnitNode;
import Matlab.Recognizer.MRecognizer;
import Matlab.Transformer.NodeToAstTransformer;
import Matlab.Utils.Result;
import abstractPattern.primitive.Execution;
import ast.*;
import org.javatuples.Pair;
import org.junit.Assert;
import org.junit.Test;
import transformer.util.IsPossibleJointPointResult;
import transformer.util.RuntimeInfo;

public class DebugExecution {
    public static Execution buildPattern(String pattern) {
        String aspectFileTempate = "" +
                "aspect_ demoAspect\n" +
                "actions \n" +
                "   a : after %s : () \n" +
                "      %%nothing here \n" +
                "   end \n" +
                "end \n" +
                "end \n";
        aspectFileTempate = String.format(aspectFileTempate, pattern);
        Result<UnitNode> unitNodeResult = MRecognizer.RecognizeText(aspectFileTempate, true);
        assert unitNodeResult.GetIsOk();
        CompilationUnits units = NodeToAstTransformer.Transform(unitNodeResult.GetValue());
        assert units.getProgram(0) instanceof AspectDef;
        assert ((AspectDef) units.getProgram(0)).getActionList().getNumChild() != 0;
        Actions actions = ((AspectDef) units.getProgram(0)).getActionList().getChild(0);
        assert actions.getNumAction() != 0;
        Action action = actions.getAction(0);
        assert action.getExpr() instanceof PatternExecution;
        Execution execution = new Execution((PatternExecution) action.getExpr());
        return execution;
    }

    public static Pair<Function, RuntimeInfo> buildAST(String functionHeader) {
        String matlabFileTemplte = "" +
                "function %s \n" +
                "   %%nothing here \n" +
                "end \n";
        matlabFileTemplte = String.format(matlabFileTemplte, functionHeader);
        Result<UnitNode> unitNodeResult = MRecognizer.RecognizeText(matlabFileTemplte, true);
        assert unitNodeResult.GetIsOk();
        CompilationUnits units = NodeToAstTransformer.Transform(unitNodeResult.GetValue());
        assert units.getProgram(0) instanceof FunctionList;
        Function retFunc = ((FunctionList) units.getProgram(0)).getFunction(0);
        return new Pair<>(retFunc, new RuntimeInfo());
    }

    public static boolean verify(Execution pattern, Pair<Function, RuntimeInfo> candidate) {
        IsPossibleJointPointResult result = pattern.isPossibleJointPoint(candidate.getValue0(), candidate.getValue1());
        return result.isExecutions;
    }

    @Test
    public void test1() { /* name matching check */
        Execution pattern = buildPattern("execution(foo(..):..)");

        Pair<Function, RuntimeInfo> candidate1 = buildAST("[] = foo()");
        Pair<Function, RuntimeInfo> candidate2 = buildAST("[] = goo()");

        Assert.assertTrue(verify(pattern, candidate1));
        Assert.assertFalse(verify(pattern, candidate2));
    }

    @Test
    public void test2() { /* fix number pattern and input in function input args */
        Execution pattern = buildPattern("execution(foo(*,*):..)");

        Pair<Function, RuntimeInfo> candidate1 = buildAST("[] = foo(arg1)");
        Pair<Function, RuntimeInfo> candidate2 = buildAST("[] = foo(arg1, arg2)");
        Pair<Function, RuntimeInfo> candidate3 = buildAST("[] = foo(arg1, arg2, arg3)");

        Assert.assertFalse(verify(pattern, candidate1));    /* not enough arguments */
        Assert.assertTrue(verify(pattern, candidate2));     /* matched */
        Assert.assertFalse(verify(pattern, candidate3));    /* too many arguments */
    }

    @Test
    public void test3() { /* fix number pattern and input on function output args */
        Execution pattern = buildPattern("execution(foo(..):*,*)");

        Pair<Function, RuntimeInfo> candidate1 = buildAST("[arg1] = foo()");
        Pair<Function, RuntimeInfo> candidate2 = buildAST("[arg1, arg2] = foo()");
        Pair<Function, RuntimeInfo> candidate3 = buildAST("[arg1, arg2, arg3] = foo()");

        Assert.assertFalse(verify(pattern, candidate1));    /* not enough arguments */
        Assert.assertTrue(verify(pattern, candidate2));     /* matched */
        Assert.assertFalse(verify(pattern, candidate3));    /* too many arguments */
    }

    @Test
    public void test4() { /* empty match */
        Execution patternIn  = buildPattern("execution(foo():..)");
        Execution patternOut = buildPattern("execution(foo(..):)");

        Pair<Function, RuntimeInfo> candidate1 = buildAST("[] = foo()");
        Pair<Function, RuntimeInfo> candidate2 = buildAST("[] = foo(arg)");
        Pair<Function, RuntimeInfo> candidate3 = buildAST("[arg] = foo()");

        Assert.assertTrue(verify(patternIn, candidate1));
        Assert.assertFalse(verify(patternIn, candidate2));

        Assert.assertTrue(verify(patternOut, candidate1));
        Assert.assertFalse(verify(patternOut, candidate3));
    }

    @Test
    public void test5() { /* fix number of input pattern, and undetermined number of args */
        Execution pattern = buildPattern("execution(foo(*,*):..)");

        Pair<Function, RuntimeInfo> candidate1 = buildAST("[] = foo(arg1, varargin)");
        Pair<Function, RuntimeInfo> candidate2 = buildAST("[] = foo(arg1, arg2, varargin)");
        Pair<Function, RuntimeInfo> candidate3 = buildAST("[] = foo(arg1, arg2, arg3, varargin)");

        Assert.assertTrue(verify(pattern, candidate1));
        Assert.assertTrue(verify(pattern, candidate2));
        Assert.assertFalse(verify(pattern, candidate3));
    }

    @Test
    public void test6() { /* fix number of output pattern, and undetermined number of args */
        Execution pattern = buildPattern("execution(foo(..):*,*)");

        Pair<Function, RuntimeInfo> candidate1 = buildAST("[arg1, varargout] = foo()");
        Pair<Function, RuntimeInfo> candidate2 = buildAST("[arg1, arg2, varargout] = foo()");
        Pair<Function, RuntimeInfo> candidate3 = buildAST("[arg1, arg2, arg3, varargout] = foo()");

        Assert.assertTrue(verify(pattern, candidate1));
        Assert.assertTrue(verify(pattern, candidate2));
        Assert.assertFalse(verify(pattern, candidate3));
    }

    @Test
    public void test7() { /* undetermined number of input pattern, and fixed number of args */
        Execution pattern = buildPattern("execution(foo(*,*,..):..)");

        Pair<Function, RuntimeInfo> candidate1 = buildAST("[] = foo(arg1)");
        Pair<Function, RuntimeInfo> candidate2 = buildAST("[] = foo(arg1, arg2)");
        Pair<Function, RuntimeInfo> candidate3 = buildAST("[] = foo(arg1, arg2, arg3)");

        Assert.assertFalse(verify(pattern, candidate1));
        Assert.assertTrue(verify(pattern, candidate2));
        Assert.assertTrue(verify(pattern, candidate3));
    }

    @Test
    public void test8() { /* undetermined number of output pattern, and fixed number of args */
        Execution pattern = buildPattern("execution(foo(..):*,*,..)");

        Pair<Function, RuntimeInfo> candidate1 = buildAST("[arg1] = foo()");
        Pair<Function, RuntimeInfo> candidate2 = buildAST("[arg1, arg2] = foo()");
        Pair<Function, RuntimeInfo> candidate3 = buildAST("[arg1, arg2, arg3] = foo()");

        Assert.assertFalse(verify(pattern, candidate1));
        Assert.assertTrue(verify(pattern, candidate2));
        Assert.assertTrue(verify(pattern, candidate3));
    }
}
