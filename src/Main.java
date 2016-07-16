import Matlab.Nodes.UnitNode;
import Matlab.Recognizer.MRecognizer;
import Matlab.Transformer.NodeToAstTransformer;
import Matlab.Utils.IReport;
import Matlab.Utils.Message;
import Matlab.Utils.Result;
import abstractPattern.*;
import abstractPattern.Set;
import ast.*;
import util.FunctionNamespace;
import util.VarNamespace;

import java.util.*;

public class Main {
    private static java.util.List<Operator> operators = new LinkedList<>();
    private static java.util.List<Call> calls = new LinkedList<>();
    private static java.util.List<Loop> loops = new LinkedList<>();
    private static java.util.List<LoopHead> loopHeads = new LinkedList<>();
    private static java.util.List<LoopBody> loopBodies = new LinkedList<>();
    private static java.util.List<Get> gets = new LinkedList<>();
    private static java.util.List<Set> sets = new LinkedList<>();

    public static void recFind(ASTNode node) {
        if (node instanceof PatternCall) calls.add(new Call((PatternCall)node));
        if (node instanceof PatternOperator) operators.add(new Operator((PatternOperator)node));
        if (node instanceof PatternLoop) loops.add(new Loop((PatternLoop)node));
        if (node instanceof PatternLoopHead) loopHeads.add(new LoopHead((PatternLoopHead)node));
        if (node instanceof PatternLoopBody) loopBodies.add(new LoopBody((PatternLoopBody)node));
        if (node instanceof PatternGet) gets.add(new Get((PatternGet)node));
        if (node instanceof PatternSet) sets.add(new Set((PatternSet)node));
        for (int iter = 0; iter < node.getNumChild(); iter++) {
            recFind(node.getChild(iter));
        }
    }
    public static void recPrint(ASTNode node, int indent) {
        for (int iter = 0; iter < indent; iter++) System.out.print("\t");
        System.out.println(node.getClass().getName());
        for (int iter = 0; iter < node.getNumChild(); iter++) {
            recPrint(node.getChild(iter), indent + 1);
        }
    }
    public static void printReport(IReport report) {
        for (Message iter : report) {
            System.out.println(String.format(
                    "[%s][%d, %d] %s",
                    iter.GetSeverity().toString(),
                    iter.GetLine(),
                    iter.GetColumn(),
                    iter.GetText()
            ));
        }
    }
    public static void printAllPatterns() {
        for (Operator iter : operators) System.out.println(iter);
        for (Call iter : calls) System.out.println(iter);
        for (Loop iter : loops) System.out.println(iter);
        for (LoopHead iter : loopHeads) System.out.println(iter);
        for (LoopBody iter : loopBodies) System.out.println(iter);
        for (Get iter : gets) System.out.println(iter);
        for (Set iter : sets) System.out.println(iter);
    }
    public static void printAllReport(String pFilepath) {
        for (Operator iter : operators) printReport(iter.getValidationReport(pFilepath));
        for (Call iter : calls) printReport(iter.getValidationReport(pFilepath));
        for (Loop iter : loops) printReport(iter.getValidationReport(pFilepath));
        for (LoopHead iter : loopHeads) printReport(iter.getValidationReport(pFilepath));
        for (LoopBody iter : loopBodies) printReport(iter.getValidationReport(pFilepath));
        for (Get iter : gets) printReport(iter.getValidationReport(pFilepath));
        //for (Set iter : sets) printReport(iter.getValidationReport(pFilepath));
    }

    public static void main(String argv[]) {
        String matlabFilePath = "/Users/k9/Documents/AspectMatlab/src/matlab.m";
        Result<UnitNode> result = MRecognizer.RecognizeFile(
                matlabFilePath,
                true,
                new Notifier()
        );
        if (!result.GetIsOk()) return;
        CompilationUnits units = NodeToAstTransformer.Transform(result.GetValue());
        recFind(units);

        //printReport(calls.get(0).getValidationReport(matlabFilePath));
        //printReport(operators.get(0).getValidationReport(matlabFilePath));

        printAllPatterns();
        printAllReport(matlabFilePath);

        //System.out.println(op.getType().getID());


    }
}
