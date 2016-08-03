package debug.isPossibleJointPoint;

import Matlab.Nodes.UnitNode;
import Matlab.Recognizer.MRecognizer;
import Matlab.Transformer.NodeToAstTransformer;
import Matlab.Utils.Result;
import abstractPattern.primitive.Call;
import ast.*;
import natlab.toolkits.analysis.varorfun.VFFlowInsensitiveAnalysis;
import org.javatuples.Pair;
import org.junit.Assert;
import org.junit.Test;
import transformer.IsPossibleJointPointResult;
import transformer.RuntimeInfo;

import java.util.List;

public class DebugCall {

    public static Call buildCallPattern(String functionName, Pair<String, List<String>>... signatures) {
        PatternCall astNodes = new PatternCall();
        astNodes.setIdentifier(new Name(functionName));
        for (Pair<String, List<String>> signature : signatures) {
            DimensionSignature dimensionSignature = new DimensionSignature();
            for (String dim : signature.getValue1()) {
                dimensionSignature.addDimension(new Name(dim));
            }
            astNodes.getInput().addFullSignature(new FullSignature(
                    new Opt<TypeSignature>(new TypeSignature(new Name(signature.getValue0()))),
                    (signature.getValue1().isEmpty())?
                            new Opt<DimensionSignature>():
                            new Opt<DimensionSignature>(dimensionSignature)
            ));
        }
        Call call = new Call(astNodes);
        return call;
    }

    @SuppressWarnings("deprecation")
    public static Pair<ParameterizedExpr, RuntimeInfo> buildAst(String funcName, String signature) {
        String matlabSource = "" +
                "function [] = %s()  \n" +
                "   %s(%s);          \n" +
                "end";
        matlabSource = String.format(matlabSource, funcName, funcName, signature);
        Result<UnitNode> rawResult = MRecognizer.RecognizeText(matlabSource, true);
        assert rawResult.GetIsOk();
        CompilationUnits units = NodeToAstTransformer.Transform(rawResult.GetValue());
        assert units.getProgram(0) instanceof FunctionList;
        assert ((FunctionList) units.getProgram(0)).getFunction(0) instanceof Function;
        RuntimeInfo runtimeInfo = new RuntimeInfo();
        runtimeInfo.kindAnalysis = new VFFlowInsensitiveAnalysis(units, units.getFunctionOrScriptQuery());
        runtimeInfo.kindAnalysis.analyze();

        assert ((FunctionList) units.getProgram(0)).getFunction(0).getStmt(0) instanceof ExprStmt;
        assert ((ExprStmt) ((FunctionList) units.getProgram(0)).getFunction(0).getStmt(0)).getExpr() instanceof ParameterizedExpr;
        return new Pair<ParameterizedExpr, RuntimeInfo>(
                (ParameterizedExpr) ((ExprStmt) ((FunctionList) units.getProgram(0)).getFunction(0).getStmt(0)).getExpr(),
                runtimeInfo
        );
    }

    public static boolean verify(Call pattern, Pair<ParameterizedExpr, RuntimeInfo> candidate) {
        IsPossibleJointPointResult result = pattern.isPossibleJointPoint(candidate.getValue0(), candidate.getValue1());
        return result.isCall();
    }

    @Test
    public void test1() { /* call(*()) */
        Call pattern = buildCallPattern("*");
        Pair<ParameterizedExpr, RuntimeInfo> candidate1 = buildAst("foo", "");
        Pair<ParameterizedExpr, RuntimeInfo> candidate2 = buildAst("foo", "x");

        Assert.assertTrue(verify(pattern, candidate1));
        Assert.assertFalse(verify(pattern, candidate2));
    }

    @Test
    public void test2() { /* call(foo()) */
        Call pattern = buildCallPattern("foo");
        Pair<ParameterizedExpr, RuntimeInfo> candidate1 = buildAst("foo", "");
        Pair<ParameterizedExpr, RuntimeInfo> candidate2 = buildAst("goo", "");

        Assert.assertTrue(verify(pattern, candidate1));
        Assert.assertFalse(verify(pattern, candidate2));
    }
}
