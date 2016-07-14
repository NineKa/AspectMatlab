import Matlab.Nodes.UnitNode;
import Matlab.Recognizer.MRecognizer;
import Matlab.Transformer.NodeToAstTransformer;
import Matlab.Utils.Result;
import ast.*;

public class Main {
    public static void recPrint(ASTNode node, int indent) {
        for (int iter = 0; iter < indent; iter++) System.out.print("\t");
        System.out.println(node.getClass().getName());
        for (int iter = 0; iter < node.getNumChild(); iter++) {
            recPrint(node.getChild(iter), indent + 1);
        }
    }

    public static void main(String argv[]) {
        Result<UnitNode> result = MRecognizer.RecognizeText("10, 20, [x, '123']", true);
        CompilationUnits unites= NodeToAstTransformer.Transform(result.GetValue());
        System.out.println(unites.getPrettyPrinted());

        recPrint(unites, 0);
    }
}
