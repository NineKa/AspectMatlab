import abstractPattern.Call;
import Matlab.Nodes.UnitNode;
import Matlab.Recognizer.MRecognizer;
import Matlab.Transformer.NodeToAstTransformer;
import Matlab.Utils.Result;
import ast.*;
import util.FunctionNamespace;
import util.VarNamespace;

import java.util.*;

public class Main {
    public static java.util.List<Call> patternCollection = new LinkedList<>();

    public static void recPrintType(ASTNode node, int indent) {
        if (node instanceof PatternCall) {
            patternCollection.add(new Call((PatternCall)node));
        }
        for (int iter = 0; iter < node.getNumChild(); iter++) {
            recPrintType(node.getChild(iter), indent + 1);
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

        recPrintType(units, 0);

        Call p1 = patternCollection.get(0);

        Function genFunc = p1.generateInputMatlabMatcher(new FunctionNamespace(), new VarNamespace());
        genFunc.setName(new Name("AspectMatlabMatcher"));
        System.out.println(genFunc.getPrettyPrinted());
    }
}
