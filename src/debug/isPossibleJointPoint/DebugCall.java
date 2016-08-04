package debug.isPossibleJointPoint;

import Matlab.Nodes.UnitNode;
import Matlab.Recognizer.MRecognizer;
import Matlab.Transformer.NodeToAstTransformer;
import Matlab.Utils.Result;
import abstractPattern.primitive.Call;
import ast.*;
import natlab.toolkits.analysis.varorfun.VFDatum;
import org.javatuples.Pair;
import org.junit.Assert;
import org.junit.Test;
import transformer.IsPossibleJointPointResult;
import transformer.RuntimeInfo;

import java.util.LinkedList;
import java.util.List;

public class DebugCall {

    public static Call buildCallPattern(String functionName, Pair<String, List<String>>... signatures) {
        PatternCall astNodes = new PatternCall();
        astNodes.setIdentifier(new Name(functionName));
        astNodes.setInput(new Input());
        astNodes.setOutput(new Output());
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
    public static Pair<ParameterizedExpr, RuntimeInfo> buildAst(String code, VFDatum vfDatum) {
        String matlabSource = "" +
                "function [] = debugFuncCall()  \n" +
                "   %s;          \n" +
                "end";
        matlabSource = String.format(matlabSource, code);
        Result<UnitNode> rawResult = MRecognizer.RecognizeText(matlabSource, true);
        assert rawResult.GetIsOk();
        CompilationUnits units = NodeToAstTransformer.Transform(rawResult.GetValue());
        assert units.getProgram(0) instanceof FunctionList;
        assert ((FunctionList) units.getProgram(0)).getFunction(0) instanceof Function;
        RuntimeInfo runtimeInfo = new RuntimeInfo();
        runtimeInfo.kindAnalysis = new DebugVFAnalysis(units, units.getFunctionOrScriptQuery());
        runtimeInfo.kindAnalysis.analyze();

        assert ((FunctionList) units.getProgram(0)).getFunction(0).getStmt(0) instanceof ExprStmt;
        assert ((ExprStmt) ((FunctionList) units.getProgram(0)).getFunction(0).getStmt(0)).getExpr() instanceof ParameterizedExpr;

        ParameterizedExpr expr = (ParameterizedExpr) ((ExprStmt) ((FunctionList) units.getProgram(0)).getFunction(0).getStmt(0)).getExpr();
        assert expr.getTarget() instanceof NameExpr;
        Name targetName = ((NameExpr) expr.getTarget()).getName();
        /* override Kind Analysis result */
        assert runtimeInfo.kindAnalysis instanceof DebugVFAnalysis;
        ((DebugVFAnalysis) runtimeInfo.kindAnalysis).override(targetName, vfDatum);

        return new Pair<>(expr, runtimeInfo);
    }

    public static boolean verify(Call pattern, Pair<ParameterizedExpr, RuntimeInfo> candidate) {
        IsPossibleJointPointResult result = pattern.isPossibleJointPoint(candidate.getValue0(), candidate.getValue1());
        return result.isCalls;
    }

    @Test
    public void test1() { /* call(*()) */
        Call pattern = buildCallPattern("*");
        Pair<ParameterizedExpr, RuntimeInfo> candidate1 = buildAst("foo()", VFDatum.FUN);
        Pair<ParameterizedExpr, RuntimeInfo> candidate2 = buildAst("foo(x)", VFDatum.FUN);

        Assert.assertTrue(verify(pattern, candidate1));
        Assert.assertFalse(verify(pattern, candidate2));
    }

    @Test
    public void test2() { /* call(foo()) */
        Call pattern = buildCallPattern("foo");
        Pair<ParameterizedExpr, RuntimeInfo> candidate1 = buildAst("foo()", VFDatum.FUN);
        Pair<ParameterizedExpr, RuntimeInfo> candidate2 = buildAst("goo()", VFDatum.FUN);

        Assert.assertTrue(verify(pattern, candidate1));
        Assert.assertFalse(verify(pattern, candidate2));
    }

    @Test
    public void test3() { /* Kind analysis test */
        Call pattern = buildCallPattern("foo");
        Pair<ParameterizedExpr, RuntimeInfo> candidate1 = buildAst("foo()", VFDatum.FUN);
        Pair<ParameterizedExpr, RuntimeInfo> candidate2 = buildAst("foo()", VFDatum.VAR);
        Pair<ParameterizedExpr, RuntimeInfo> candidate3 = buildAst("foo()", VFDatum.BOT);

        Assert.assertTrue(verify(pattern, candidate1));
        Assert.assertFalse(verify(pattern, candidate2));
        Assert.assertTrue(verify(pattern, candidate3));
    }

    @Test
    public void test4() { /* fix number of inputs and pattern test */
        Call pattern = buildCallPattern( /* foo(*, *) */
                "foo",
                new Pair<String, List<String>>("*", new LinkedList<String>()),
                new Pair<String, List<String>>("*", new LinkedList<String>())
        );

        Pair<ParameterizedExpr, RuntimeInfo> candidate1 = buildAst("foo(arg1)", VFDatum.FUN);
        Pair<ParameterizedExpr, RuntimeInfo> candidate2 = buildAst("foo(arg1, arg2)", VFDatum.FUN);
        Pair<ParameterizedExpr, RuntimeInfo> candidate3 = buildAst("foo(arg1, arg2, arg3)", VFDatum.FUN);

        Assert.assertFalse(verify(pattern, candidate1));    /* not enough argument */
        Assert.assertTrue(verify(pattern, candidate2));     /* matched */
        Assert.assertFalse(verify(pattern, candidate3));    /* too may arguments */
    }

    @Test
    public void test5() { /* fix number of inputs and undetermined pattern */
        Call pattern = buildCallPattern( /* foo(*, *) */
                "foo",
                new Pair<String, List<String>>("*", new LinkedList<String>()),
                new Pair<String, List<String>>("*", new LinkedList<String>())
        );
        Pair<ParameterizedExpr, RuntimeInfo> candidate1 = buildAst("foo(arg1, arg2, arg3(:))", VFDatum.FUN);
        Pair<ParameterizedExpr, RuntimeInfo> candidate2 = buildAst("foo(arg1, arg2, arg3{:})", VFDatum.FUN);
        Pair<ParameterizedExpr, RuntimeInfo> candidate3 = buildAst("foo(arg1, arg2, arg3(x:y))", VFDatum.FUN);
        Pair<ParameterizedExpr, RuntimeInfo> candidate4 = buildAst("foo(arg1, arg2, arg3{x:y})", VFDatum.FUN);


        Pair<ParameterizedExpr, RuntimeInfo> candidate5 = buildAst("foo(arg1, arg2(:))", VFDatum.FUN);
        Pair<ParameterizedExpr, RuntimeInfo> candidate6 = buildAst("foo(arg1, arg2{:})", VFDatum.FUN);
        Pair<ParameterizedExpr, RuntimeInfo> candidate7 = buildAst("foo(arg1, arg2(x:y))", VFDatum.FUN);
        Pair<ParameterizedExpr, RuntimeInfo> candidate8 = buildAst("foo(arg1, arg2{x:y})", VFDatum.FUN);

        Pair<ParameterizedExpr, RuntimeInfo> candidate9  = buildAst("foo(arg1, arg2, arg3, arg4(:))", VFDatum.FUN);
        Pair<ParameterizedExpr, RuntimeInfo> candidate10 = buildAst("foo(arg1, arg2, arg3, arg4{:})", VFDatum.FUN);
        Pair<ParameterizedExpr, RuntimeInfo> candidate11 = buildAst("foo(arg1, arg2, arg3, arg4(x:y))", VFDatum.FUN);
        Pair<ParameterizedExpr, RuntimeInfo> candidate12 = buildAst("foo(arg1, arg2, arg3, arg4{x:y})", VFDatum.FUN);

        Assert.assertTrue(verify(pattern, candidate1));
        Assert.assertTrue(verify(pattern, candidate2));
        Assert.assertTrue(verify(pattern, candidate3));
        Assert.assertTrue(verify(pattern, candidate4));

        Assert.assertTrue(verify(pattern, candidate5));
        Assert.assertTrue(verify(pattern, candidate6));
        Assert.assertTrue(verify(pattern, candidate7));
        Assert.assertTrue(verify(pattern, candidate8));

        Assert.assertFalse(verify(pattern, candidate9));
        Assert.assertFalse(verify(pattern, candidate10));
        Assert.assertFalse(verify(pattern, candidate11));
        Assert.assertFalse(verify(pattern, candidate12));
    }

    @Test
    public void test6() { /* undetermined inputs and fixed number of pattern */
        Call pattern = buildCallPattern( /* foo(*, *, ..) */
                "foo",
                new Pair<String, List<String>>("*", new LinkedList<String>()),
                new Pair<String, List<String>>("*", new LinkedList<String>()),
                new Pair<String, List<String>>("..", new LinkedList<String>())
        );

        Pair<ParameterizedExpr, RuntimeInfo> candidate1 = buildAst("foo(arg1)", VFDatum.FUN);
        Pair<ParameterizedExpr, RuntimeInfo> candidate2 = buildAst("foo(arg1, arg2)", VFDatum.FUN);
        Pair<ParameterizedExpr, RuntimeInfo> candidate3 = buildAst("foo(arg1, arg2, arg3)", VFDatum.FUN);

        Assert.assertFalse(verify(pattern, candidate1));
        Assert.assertTrue(verify(pattern, candidate2));
        Assert.assertTrue(verify(pattern, candidate3));
    }
}
