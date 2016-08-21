import Matlab.Nodes.UnitNode;
import Matlab.Recognizer.MRecognizer;
import Matlab.Transformer.NodeToAstTransformer;
import Matlab.Utils.*;
import abstractPattern.primitive.Call;
import abstractPattern.primitive.Execution;
import ast.*;
import natlab.toolkits.analysis.varorfun.VFAnalysis;

import java.util.LinkedList;

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

        assert units.getProgram(0) instanceof FunctionList;
        assert ((FunctionList) units.getProgram(0)).getFunction(0).getStmt(1) instanceof ExprStmt;
        Expr target = ((ExprStmt) ((FunctionList) units.getProgram(0)).getFunction(0).getStmt(1)).getExpr();
        assert target instanceof CellIndexExpr;
    }
}

