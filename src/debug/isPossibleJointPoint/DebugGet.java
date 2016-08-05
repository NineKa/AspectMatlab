package debug.isPossibleJointPoint;

import Matlab.Nodes.UnitNode;
import Matlab.Recognizer.MRecognizer;
import Matlab.Transformer.NodeToAstTransformer;
import Matlab.Utils.Result;
import abstractPattern.primitive.Get;
import ast.*;
import natlab.toolkits.analysis.varorfun.VFDatum;
import org.javatuples.Pair;
import org.junit.Assert;
import org.junit.Test;
import transformer.util.AccessMode;
import transformer.util.IsPossibleJointPointResult;
import transformer.util.RuntimeInfo;

public class DebugGet {
    public static Get buildPattern(String pattern) {
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
        assert action.getExpr() instanceof PatternGet;
        Get get = new Get((PatternGet) action.getExpr());
        return get;
    }

    @SuppressWarnings("deprecation")
    public static Pair<NameExpr, RuntimeInfo> buildAST(String variablename, VFDatum kindAnalysis, AccessMode accessMode) {
        RuntimeInfo runtimeInfo = new RuntimeInfo();
        String matlabFileTemplte = "" +
                "function [] = demoFunction() \n" +
                "   %s; \n" +
                "end \n";
        matlabFileTemplte = String.format(matlabFileTemplte, variablename);
        Result<UnitNode> unitNodeResult = MRecognizer.RecognizeText(matlabFileTemplte, true);
        assert unitNodeResult.GetIsOk();
        CompilationUnits units = NodeToAstTransformer.Transform(unitNodeResult.GetValue());
        assert units.getProgram(0) instanceof FunctionList;
        assert ((FunctionList) units.getProgram(0)).getFunction(0).getStmt(0) instanceof ExprStmt;

        ExprStmt exprStmt =(ExprStmt) ((FunctionList) units.getProgram(0)).getFunction(0).getStmt(0);
        assert exprStmt.getExpr() instanceof NameExpr;
        NameExpr expr = (NameExpr) exprStmt.getExpr();

        runtimeInfo.kindAnalysis = new DebugVFAnalysis(units, units.getFunctionOrScriptQuery());
        assert runtimeInfo.kindAnalysis instanceof DebugVFAnalysis;
        ((DebugVFAnalysis) runtimeInfo.kindAnalysis).override(expr.getName(), kindAnalysis);
        runtimeInfo.accessMode = accessMode;

        return new Pair<>(expr, runtimeInfo);
    }

    public static boolean verify(Get pattern, Pair<NameExpr, RuntimeInfo> candidate) {
        IsPossibleJointPointResult result = pattern.isPossibleJointPoint(candidate.getValue0(), candidate.getValue1());
        return result.isGets;
    }

    @Test
    public void test1() { /* variable name match (wildcards) */
        Get pattern = buildPattern("get(*)");

        Pair<NameExpr, RuntimeInfo> candidate1 = buildAST("x", VFDatum.VAR, AccessMode.Read);
        Pair<NameExpr, RuntimeInfo> candidate2 = buildAST("y", VFDatum.VAR, AccessMode.Read);

        Assert.assertTrue(verify(pattern, candidate1));
        Assert.assertTrue(verify(pattern, candidate2));
    }

    @Test
    public void test2() { /* variable name match (specific) */
        Get pattern = buildPattern("get(x)");

        Pair<NameExpr, RuntimeInfo> candidate1 = buildAST("x", VFDatum.VAR, AccessMode.Read);
        Pair<NameExpr, RuntimeInfo> candidate2 = buildAST("y", VFDatum.VAR, AccessMode.Read);

        Assert.assertTrue(verify(pattern, candidate1));
        Assert.assertFalse(verify(pattern, candidate2));
    }

    @Test
    public void test3() { /* variable Access Mode */
        Get pattern = buildPattern("get(*)");

        Pair<NameExpr, RuntimeInfo> candidate1 = buildAST("x", VFDatum.VAR, AccessMode.Read);
        Pair<NameExpr, RuntimeInfo> candidate2 = buildAST("y", VFDatum.VAR, AccessMode.Write);

        Assert.assertTrue(verify(pattern, candidate1));
        Assert.assertFalse(verify(pattern, candidate2));
    }

    @Test
    public void test4() { /* Kind analysis */
        Get pattern = buildPattern("get(*)");

        Pair<NameExpr, RuntimeInfo> candidate1 = buildAST("x", VFDatum.VAR, AccessMode.Read); /* resolve as VAR  */
        Pair<NameExpr, RuntimeInfo> candidate2 = buildAST("x", VFDatum.BOT, AccessMode.Read); /* cannot resolve  */
        Pair<NameExpr, RuntimeInfo> candidate3 = buildAST("x", VFDatum.FUN, AccessMode.Read); /* resolve as FUNC */

        Assert.assertTrue(verify(pattern, candidate1));
        Assert.assertTrue(verify(pattern, candidate2));
        Assert.assertFalse(verify(pattern, candidate3));
    }
}
