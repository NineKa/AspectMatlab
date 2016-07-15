import Matlab.Nodes.UnitNode;
import Matlab.Recognizer.MRecognizer;
import Matlab.Transformer.NodeToAstTransformer;
import Matlab.Utils.IReport;
import Matlab.Utils.Message;
import Matlab.Utils.Result;
import abstractPattern.Call;
import abstractPattern.Operator;
import ast.*;
import util.FunctionNamespace;
import util.VarNamespace;

import java.util.*;

public class Main {
    private static java.util.List<Operator> operators = new LinkedList<>();
    private static java.util.List<Call> calls = new LinkedList<>();

    public static void recFind(ASTNode node) {
        if (node instanceof PatternCall) calls.add(new Call((PatternCall)node));
        if (node instanceof PatternOperator) operators.add(new Operator((PatternOperator)node));
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

        printReport(calls.get(0).getValidationReport(matlabFilePath));
        printReport(operators.get(0).getValidationReport(matlabFilePath));

        //System.out.println(op.getType().getID());


    }
}
