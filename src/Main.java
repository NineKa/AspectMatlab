import Matlab.Nodes.UnitNode;
import Matlab.Recognizer.MRecognizer;
import Matlab.Transformer.NodeToAstTransformer;
import Matlab.Utils.Result;
import abstractPattern.Call;
import ast.*;
import matcher.parameter.AutoMatcher;
import matcher.parameter.EmptyMatcher;
import matcher.parameter.FullMatcher;
import matcher.parameter.SimpleMatcher;
import util.FunctionNamespace;
import util.VarNamespace;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;

public class Main {
    public static Collection<Call> calls = new HashSet<>();

    public static void recFind(ASTNode node) {
        if (node instanceof PatternCall) calls.add(new Call((PatternCall)node));
        for (int iter = 0; iter < node.getNumChild(); iter++) recFind(node.getChild(iter));
    }

    public static void main(String argv[]) {
        String k[] = new String[0];

        String matlabFilePath = "/Users/k9/Documents/AspectMatlab/src/matlab.m";
        Result<UnitNode> result = MRecognizer.RecognizeFile(
                matlabFilePath,
                true,
                new Notifier()
        );
        if (!result.GetIsOk()) return;
        CompilationUnits units = NodeToAstTransformer.Transform(result.GetValue());
        recFind(units);

        for (Call iter : calls) System.out.println(iter.toString());

        for (Call iter : calls) {
            AutoMatcher matcher = new AutoMatcher(
                    iter.getInputSignatures(),
                    new FunctionNamespace(),
                    new VarNamespace()
            );
            Function func = matcher.getFunction();
            func.setName(new Name("AspectMatlabMatcher"));
            System.out.println(func.getPrettyPrinted());
        }
    }
}
