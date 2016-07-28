import Matlab.Nodes.FileNode;
import Matlab.Nodes.UnitNode;
import Matlab.Recognizer.MRecognizer;
import Matlab.Transformer.NodeToAstTransformer;
import Matlab.Utils.*;
import abstractPattern.primitive.Call;
import abstractPattern.primitive.Execution;
import abstractPattern.type.LoopType;
import ast.*;
import matcher.nameResolve.LoopNameSolver;
import natlab.toolkits.analysis.varorfun.VFAnalysis;
import org.javatuples.Pair;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

public class Main {
    public static VFAnalysis analysis = null;
    public static java.util.List<Call> calls = new LinkedList<>();
    public static java.util.List<Execution> executions = new LinkedList<>();

    public static void recFind(ASTNode node) {
        if (node instanceof PatternCall) calls.add(new Call((PatternCall)node));
        if (node instanceof PatternExecution) executions.add(new Execution((PatternExecution)node));
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
        try {
            if (node instanceof Name) {
                System.out.print(String.format(
                        "[%d : %d] [%b] %s : ",
                        node.getStartLine(),
                        node.getStartColumn(),
                        node.hasComments(),
                        node.getClass().getName()
                ));
                if (analysis.getResult((Name) node) == null) {
                    System.out.println("null");
                } else {
                    System.out.println(analysis.getResult((ast.Name) node));
                }

            } else {
                System.out.println(String.format(
                        "[%d : %d] [%b] %s",
                        node.getStartLine(),
                        node.getStartColumn(),
                        node.hasComments(),
                        node.getClass().getName()
                ));
            }
        } catch (NullPointerException exception) {
            System.out.println("here");
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
            printRawTree(unitNode, indent+1);
        }
    }

    public static void main(String argv[]) {
        String matlabFilePath = "/Users/k9/Documents/AspectMatlab/src/matlab.m";
        String functionFilePath = "/Users/k9/Documents/AspectMatlab/src/function.m";

        Result<UnitNode> result = MRecognizer.RecognizeFile(
                functionFilePath,
                true,
                new Notifier()
        );
        if (!result.GetIsOk()) return;
        CompilationUnits units = NodeToAstTransformer.Transform(result.GetValue());

        Iterator<FileNode> fileNodeIterator = result.GetValue().GetFiles().iterator();
        Iterator<Program> programIterator = units.getProgramList().iterator();

        while (fileNodeIterator.hasNext() && programIterator.hasNext()) {
            Map<Stmt, Pair<LoopType, String>> map = null;
            LoopNameSolver solver = new LoopNameSolver(programIterator.next(), fileNodeIterator.next());
            map = solver.getSolveMap();
            System.out.println(map);
        }

        // recPrintStructure(units, 0);
    }
}
