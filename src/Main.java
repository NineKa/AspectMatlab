import Matlab.Nodes.UnitNode;
import Matlab.Recognizer.MRecognizer;
import Matlab.Transformer.NodeToAstTransformer;
import Matlab.Utils.IReport;
import Matlab.Utils.Message;
import Matlab.Utils.Result;
import abstractPattern.AbstractBuilder;
import abstractPattern.primitive.Call;
import abstractPattern.primitive.Execution;
import ast.*;

import java.util.*;

public class Main {
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
    public static void recPrintValidationReport(String pFilepath) {
        for (Call call : calls) {
            System.out.println(call);
            printReport(call.getValidationReport(pFilepath));
        }
        for (Execution execution : executions) {
            System.out.println(execution);
            printReport(execution.getValidationReport(pFilepath));
        }
    }
    public static void recPrintStructure(ASTNode node, int indent) {
        for (int iter = 0; iter < indent; iter++) System.out.print('\t');
        System.out.println(String.format(
                "[%d : %d] %s",
                node.getStartLine(),
                node.getStartColumn(),
                node.getClass().getName()
        ));
        for (int iter = 0; iter < node.getNumChild(); iter++) {
            recPrintStructure(node.getChild(iter), indent + 1);
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

        // recPrintStructure(units, 0);

        AspectDef def = (AspectDef)units.getProgram(0);
        Actions actions = def.getActions().getChild(0);
        Map<String, Expr> definedMap = new HashMap<>();
        for (Pattern pattern : def.getPatternList().getChild(0).getPatternList()) {
            definedMap.put(pattern.getName(), pattern.getExpr());
        }
        Action action = actions.getAction(0);
        Expr pattern = action.getExpr();

        AbstractBuilder abstractBuilder = new AbstractBuilder(matlabFilePath, pattern, definedMap);
        System.out.println(abstractBuilder.getPattern());
        for (Message message : abstractBuilder.getReport()) {
            System.out.println(String.format(
                    "[%s][%d : %d] %s",
                    message.GetSeverity(),
                    message.GetLine(),
                    message.GetColumn(),
                    message.GetText()
            ));
        }

        /*try {
            Analysis analysis = new Analysis(matlabFilePath, pattern);
            System.out.println(analysis.getResult());
            if (analysis.getResult() == PatternType.Modifier) {
                Modifier modifier = PatternClassifier.buildModifier(pattern);
                System.out.println(modifier);
            }
        } catch (Backtrace backtrace) {
            System.out.println(backtrace.toString());
        }*/

        // recPrintValidationReport(matlabFilePath);

    }
}
