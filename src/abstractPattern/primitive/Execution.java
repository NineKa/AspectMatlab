package abstractPattern.primitive;

import Matlab.Utils.IReport;
import Matlab.Utils.Message;
import Matlab.Utils.Report;
import abstractPattern.Primitive;
import abstractPattern.utility.Signature;
import ast.*;

import java.util.LinkedList;
import java.util.List;

public class Execution extends Primitive {
    private PatternExecution astNodes = null;

    private List<Signature> inputSignatures = new LinkedList<>();
    private List<Signature> outputSignatures = new LinkedList<>();
    private String functionName = null;

    public Execution(PatternExecution execution) {
        this.astNodes = execution;
        /* --- refactor --- */
        assert this.astNodes.getIdentifier() != null;
        if (this.astNodes.getInput() == null) this.astNodes.setInput(new Input(new ast.List<FullSignature>()));
        if (this.astNodes.getOutput() == null) this.astNodes.setOutput(new Output(new ast.List<FullSignature>()));
        /* ---------------- */
        for (int iter = 0; iter < this.astNodes.getInput().getNumFullSignature(); iter++) {
            this.inputSignatures.add(new Signature(this.astNodes.getInput().getFullSignature(iter)));
        }
        for (int iter = 0; iter < this.astNodes.getOutput().getNumFullSignature(); iter++) {
            this.outputSignatures.add(new Signature(this.astNodes.getOutput().getFullSignature(iter)));
        }
        this.functionName = this.astNodes.getIdentifier().getID();
    }

    public List<Signature> getInputSignatures() {
        return inputSignatures;
    }

    public List<Signature> getOutputSignatures() {
        return outputSignatures;
    }

    public String getFunctionName() {
        return functionName;
    }

    @Override
    public IReport getValidationReport(String pFilepath) {
        Report retReport = new Report();
        for (Signature signature : this.inputSignatures) {
            for (Message message : signature.getValidationReport(pFilepath)) {
                retReport.Add(message);
            }
        }
        for (Signature signature : this.outputSignatures) {
            for (Message message : signature.getValidationReport(pFilepath)) {
                retReport.Add(message);
            }
        }
        if (this.functionName.equals("..")) {
            retReport.AddError(
                    pFilepath,
                    this.astNodes.getIdentifier().getStartLine(),
                    this.astNodes.getIdentifier().getStartColumn(),
                    "wildcard [..] is not a valid matcher in execution pattern for function name, use [*] instead"
            );
        }
        return retReport;
    }

    @Override
    public boolean isValid() {
        for (Signature signature : this.inputSignatures) if (!signature.isValid()) return false;
        for (Signature signature : this.outputSignatures) if (!signature.isValid()) return false;
        if (this.functionName.equals("..")) return false;

        return true;
    }

    @Override
    public Class<? extends ASTNode> getASTPatternClass() {
        return PatternExecution.class;
    }

    @Override
    public String toString() {
        String inputPramString = "";
        for (int iter = 0; iter < this.inputSignatures.size(); iter++) {
            inputPramString = inputPramString + this.inputSignatures.get(iter).toString();
            inputPramString = inputPramString + ((iter + 1 < this.inputSignatures.size())?", ":"");
        }
        String outputPramString = "";
        for (int iter = 0; iter < this.outputSignatures.size(); iter++) {
            outputPramString = outputPramString + this.outputSignatures.get(iter).toString();
            outputPramString = outputPramString + ((iter + 1< this.outputSignatures.size())?", ":"");
        }
        String executinStr;
        if (this.outputSignatures.isEmpty()) {
            executinStr = String.format("execution(%s(%s))", this.functionName, inputPramString);
        } else {
            executinStr = String.format("execution(%s(%s):%s)", this.functionName, inputPramString, outputPramString);
        }
        if (this.isModified()) {
            String appendingStr = "";
            for (int iter = 0; iter < this.getModifiers().size(); iter++) {
                appendingStr = appendingStr + this.getModifiers().get(iter);
                if (iter + 1 < this.getModifiers().size()) appendingStr = appendingStr + " & ";
            }
            return String.format("(%s & %s)", executinStr, appendingStr);
        } else {
            return executinStr;
        }
    }

    @Override
    public ASTNode getASTExpr() {
        return this.astNodes;
    }

    @Override
    public boolean isProperlyModified() {   // TODO
        return false;
    }

    @Override
    public IReport getModifierValidationReport(String pFilepath) {  // TODO
        return null;
    }
}
