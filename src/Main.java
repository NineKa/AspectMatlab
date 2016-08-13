import Matlab.Nodes.UnitNode;
import Matlab.Recognizer.MRecognizer;
import Matlab.Transformer.NodeToAstTransformer;
import Matlab.Utils.*;
import abstractPattern.primitive.Call;
import abstractPattern.primitive.Execution;
import ast.*;
import natlab.toolkits.analysis.varorfun.VFAnalysis;
import natlab.toolkits.analysis.varorfun.VFFlowInsensitiveAnalysis;
import org.javatuples.Pair;
import transformer.expr.ExprTrans;
import transformer.expr.ExprTransArgument;
import transformer.util.RuntimeInfo;
import util.VarNamespace;

import java.util.*;

public class Main {
    public static VFAnalysis analysis = null;
    public static java.util.List<Call> calls = new LinkedList<>();
    public static java.util.List<Execution> executions = new LinkedList<>();

    public static void recFind(ASTNode node) {
        if (node instanceof PatternCall) calls.add(new Call((PatternCall) node));
        if (node instanceof PatternExecution) executions.add(new Execution((PatternExecution) node));
        for (int iter = 0; iter < node.getNumChild(); iter++) recFind(node.getChild(iter));
    }

    public static void printReport(IReport report) {
        for (Message message : report) {
            System.out.println(String.format(
                    "[%s][%d:%d]%s",
                    message.GetSeverity().toString(),
                    message.GetLine(),
                    message.GetColumn(),
                    message.GetText()
            ));
        }
    }

    public static void recPrintStructure(ASTNode node, int indent) {
        for (int iter = 0; iter < indent; iter++) System.out.print('\t');
        System.out.print(String.format("[%x][%d] %s", node.hashCode(), node.GetRelativeChildIndex(), node.getClass().getName()));

        if (node instanceof Name) {
            String kindAnalysisResult;
            try {
                kindAnalysisResult = analysis.getResult((Name) node).toString();
            } catch (NullPointerException except) {
                kindAnalysisResult = "null";
            }
            System.out.println(" Kind Analysis: " + kindAnalysisResult);
        } else {
            System.out.println();
        }

        for (int iter = 0; iter < node.getNumChild(); iter++) {
            recPrintStructure(node.getChild(iter), indent + 1);
        }
    }

    public static void printRawTree(Node node, int indent) {
        for (int iter = 0; iter < indent; iter++) System.out.print('\t');
        System.out.println(node.getClass().getName());
        NodeCollection collection = node.GetChildren();
        for (Node unitNode : node.GetChildren()) {
            printRawTree(unitNode, indent + 1);
        }
    }

    public static ASTNode testClone(ASTNode astNode) throws CloneNotSupportedException {
        ASTNode suchClone = astNode.copy();
        //for (int iter = 0; iter < astNode.getNumChild(); iter++) {
        //    suchClone.setChild(testClone(astNode.getChild(iter)), iter);
        //}
        return suchClone;
    }

    @SuppressWarnings("deprecation")
    public static void main(String argv[]) throws CloneNotSupportedException {

        String matlabFilePath = "/Users/k9/Documents/AspectMatlab/src/matlab.m";
        String functionFilePath = "/Users/k9/Documents/AspectMatlab/src/function.m";

        Result<UnitNode> result = MRecognizer.RecognizeFile(
                functionFilePath,
                true,
                new Notifier()
        );
        if (!result.GetIsOk()) return;
        CompilationUnits units = NodeToAstTransformer.Transform(result.GetValue());

        VFAnalysis kindAnalysis = new VFFlowInsensitiveAnalysis(units, units.getFunctionOrScriptQuery());
        kindAnalysis.analyze();
        analysis = kindAnalysis;

        recPrintStructure(units, 0);
        System.out.println(units.getPrettyPrinted());

        Result<UnitNode> aspectResult = MRecognizer.RecognizeFile(
                matlabFilePath,
                true,
                new Notifier()
        );
        CompilationUnits aspects = NodeToAstTransformer.Transform(aspectResult.GetValue());

        assert units.getProgram(0) instanceof FunctionList;
        Function function = ((FunctionList) units.getProgram(0)).getFunction(0);

        assert function.getStmt(1) instanceof AssignStmt;
        AssignStmt stmt = (AssignStmt) function.getStmt(1);
        Expr rhs = stmt.getRHS();

        assert aspects.getProgram(0) instanceof AspectDef;
        Action action = ((AspectDef) aspects.getProgram(0)).getAction(0).getAction(0);
        abstractPattern.Action abstractAction = new abstractPattern.Action(action, new HashMap<>(), matlabFilePath);

        RuntimeInfo runtimeInfo = new RuntimeInfo();
        runtimeInfo.kindAnalysis = kindAnalysis;

        Collection<Stmt> jointPoints = new HashSet<>();

        ExprTransArgument argument = new ExprTransArgument(
                Arrays.asList(abstractAction),
                runtimeInfo,
                new VarNamespace(),
                (ASTNode node) -> false,
                (Stmt statement) -> jointPoints.add(statement)
        );

        ExprTrans transformer = ExprTrans.buildExprTransformer(argument, rhs);
        System.out.println(rhs.getPrettyPrinted());

        Pair<Expr, java.util.List<Stmt>> r = transformer.copyAndTransform();
        for (Stmt stmt1 : r.getValue1()) {
            System.out.println(stmt1.getPrettyPrinted());
        }
        System.out.println(r.getValue0().getPrettyPrinted());

        System.out.println("Joint Points:");
        for (Stmt statement : jointPoints) {
            System.out.println(statement.getPrettyPrinted());
        }

        //Map<EmptyStmt, HelpComment> map = RuntimeInfo.insertAnnotationEmptyStmt(units);
        //for (EmptyStmt emptyStmt : map.keySet()) {
        //    System.out.println(emptyStmt + " " + map.get(emptyStmt).getText());
        //}

        /*
        assert units.getProgram(0) instanceof AspectDef;
        for (Actions actions : ((AspectDef) units.getProgram(0)).getActionList()) {
            for (Action action : actions.getActionList()) {
                abstractPattern.Action abstractAction = new abstractPattern.Action(
                        action,
                        new HashMap<>(),
                        matlabFilePath
                );
                System.out.println(abstractAction.toString());
            }
        }

        VFAnalysis analysis = new VFFlowInsensitiveAnalysis(new CompilationUnits());
        analysis.analyze();
        */
        //recPrintStructure(units, 0);
    }
}

