import Matlab.Nodes.UnitNode;
import Matlab.Recognizer.MRecognizer;
import Matlab.Transformer.NodeToAstTransformer;
import Matlab.Utils.IReport;
import Matlab.Utils.Message;
import Matlab.Utils.Result;
import abstractPattern.Call;
import ast.*;
import util.FunctionNamespace;
import util.VarNamespace;

import java.util.*;

public class Main {
    private static java.util.List<Call> patternCollection = new LinkedList<>();
    public static void recFind(ASTNode node) {
        if (node instanceof PatternCall) patternCollection.add(new Call((PatternCall)node));
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

        Call p1 = patternCollection.get(0);
        System.out.println(p1.toString());

        IReport report = p1.getValidationReport(matlabFilePath);
        for (Message iter : report) {
            System.out.println(String.format(
                    "[%d, %d] %s",
                    iter.GetLine(),
                    iter.GetColumn(),
                    iter.GetText()
            ));
        }
        System.out.println(report.GetIsOk());

    }
}
