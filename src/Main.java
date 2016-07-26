import Matlab.Nodes.UnitNode;
import Matlab.Recognizer.MRecognizer;
import Matlab.Transformer.NodeToAstTransformer;
import Matlab.Utils.IReport;
import Matlab.Utils.Message;
import Matlab.Utils.Result;
import abstractPattern.primitive.Call;
import abstractPattern.primitive.Execution;
import ast.*;
import ast.Action;
import matcher.annotation.AbstractAnnotation;
import matcher.annotation.AnnotateLexer;
import matcher.annotation.AnnotateParser;
import natlab.DecIntNumericLiteralValue;
import natlab.FPNumericLiteralValue;
import natlab.toolkits.analysis.varorfun.VFAnalysis;
import natlab.toolkits.analysis.varorfun.VFFlowInsensitiveAnalysis;
import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;

import java.util.*;
import java.util.List;

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

    public static void main(String argv[]) {
        /*
        String matlabFilePath = "/Users/k9/Documents/AspectMatlab/src/matlab.m";
        String functionFilePath = "/Users/k9/Documents/AspectMatlab/src/function.m";

        Result<UnitNode> result = MRecognizer.RecognizeFile(
                functionFilePath,
                true,
                new Notifier()
        );
        if (!result.GetIsOk()) return;
        CompilationUnits units = NodeToAstTransformer.Transform(result.GetValue());
        */
        AnnotateLexer lexer = new AnnotateLexer(new ANTLRStringStream("%@abc 10.0i, ['asdf', var2]"));
        AnnotateParser parser = new AnnotateParser(new CommonTokenStream(lexer));
        try {
            AbstractAnnotation abstractAnnotation = parser.annotate();
            System.out.println(abstractAnnotation.toString());
        } catch (RecognitionException e) {
            e.printStackTrace();
        }

    }
}
