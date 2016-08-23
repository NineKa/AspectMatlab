package debug;

import Matlab.Nodes.UnitNode;
import Matlab.Recognizer.INotifier;
import Matlab.Recognizer.MRecognizer;
import Matlab.Transformer.NodeToAstTransformer;
import Matlab.Utils.IReport;
import Matlab.Utils.Message;
import Matlab.Utils.Result;
import abstractPattern.Action;
import ast.*;
import org.javatuples.Pair;
import transformer.TransformerArgument;
import transformer.expr.ExprTrans;
import transformer.jointpoint.AMJointPoint;
import transformer.util.AccessMode;
import transformer.util.RuntimeInfo;
import transformer.util.VFAnalysisOverride;
import util.VarNamespace;

import java.util.*;
import java.util.List;

public class ExprTransformDebug {
    public static final String ASPECT_FILE = "/Users/k9/Documents/AspectMatlab/src/matlab.m";
    public static final String FUNCTION_FILE = "/Users/k9/Documents/AspectMatlab/src/function.m";

    public static CompilationUnits parseFile(String filePath) {
        INotifier notifier = new INotifier() {
            @Override
            public void Notify(String s, IReport iReport) {
                if (iReport.GetIsOk()) {
                    System.out.println(String.format("File %s OK", s));
                    return;
                }
                System.out.println(String.format("In file %s:", s));
                int totalMessage = 0;
                for (Message message : iReport) {
                    System.out.println(String.format(
                            "[%s] [%d:%d] %s",
                            message.GetSeverity(),
                            message.GetLine(),
                            message.GetColumn(),
                            message.GetText()
                    ));
                    totalMessage = totalMessage + 1;
                }
                System.out.println(String.format("total %d errors/warnings encountered", totalMessage));
            }
        };
        Result<UnitNode> rawParsedResult = MRecognizer.RecognizeFile(filePath, true, notifier);
        if (!rawParsedResult.GetIsOk()) throw new AssertionError();
        CompilationUnits parsedResult = NodeToAstTransformer.Transform(rawParsedResult.GetValue());
        return parsedResult;
    }

    public static void main(String argv[]) {
        CompilationUnits aspect = parseFile(ASPECT_FILE);
        CompilationUnits function = parseFile(FUNCTION_FILE);

        assert aspect.getProgram(0) instanceof AspectDef;
        assert function.getProgram(0) instanceof FunctionList;

        Action action = new Action(
                ((AspectDef) aspect.getProgram(0)).getAction(0).getAction(0),
                new HashMap<>(),
                ASPECT_FILE
        );

        Collection<AMJointPoint> jointpoints = new HashSet<>();

        RuntimeInfo runtimeInfo = new RuntimeInfo();
        runtimeInfo.accessMode = AccessMode.Read;
        runtimeInfo.annotationMap = new HashMap<>();
        runtimeInfo.kindAnalysis = new VFAnalysisOverride(function, function.getFunctionOrScriptQuery());
        runtimeInfo.kindAnalysis.analyze();
        runtimeInfo.loopNameResolveMap = new HashMap<>();
        runtimeInfo.scopeTraceStack = new Stack<>();

        TransformerArgument argument = new TransformerArgument(
                Arrays.asList(action),
                runtimeInfo,
                new VarNamespace(),
                (ASTNode node) -> false,
                (AMJointPoint jointpoint) -> jointpoints.add(jointpoint),
                FUNCTION_FILE
        );

        assert ((FunctionList) function.getProgram(0)).getFunction(0).getStmt(1) instanceof ExprStmt;
        Expr targetExpr = ((ExprStmt) ((FunctionList) function.getProgram(0)).getFunction(0).getStmt(1)).getExpr();
        System.out.println(targetExpr.getPrettyPrinted());

        ExprTrans transformer = ExprTrans.buildExprTransformer(argument, targetExpr);
        Pair<Expr, List<Stmt>> transformResult = transformer.copyAndTransform();

        System.out.println("Transform Result: ");
        for (Stmt stmt : transformResult.getValue1()) {
            System.out.println(stmt.getPrettyPrinted());
        }
        System.out.println(transformResult.getValue0().getPrettyPrinted());

        System.out.println("Joint Points :");
        for (AMJointPoint jointPoint : jointpoints) {
            System.out.println(jointPoint);
        }
    }
}
