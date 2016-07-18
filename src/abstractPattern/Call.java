package abstractPattern;

import Matlab.Utils.IReport;
import Matlab.Utils.Message;
import Matlab.Utils.Report;
import abstractPattern.utility.Signature;
import ast.*;

import java.util.LinkedList;
import java.util.List;

public class Call extends Primitive{
    private PatternCall astNodes = null;

    private List<Signature> inputSignatures = new LinkedList<>();
    private List<Signature> outputSignatures = new LinkedList<>();
    private String functionName = null;

    public Call(PatternCall call) {
        this.astNodes = call;
        // --- refactor ---
        assert(call.getIdentifier() != null);
        if (call.getInput() == null) this.astNodes.setInput(new Input(new ast.List<FullSignature>()));
        if (call.getOutput() == null) this.astNodes.setOutput(new Output(new ast.List<FullSignature>()));
        // ----------------

        for (int iter = 0; iter < call.getInput().getNumFullSignature(); iter++) {
            inputSignatures.add(new Signature(call.getInput().getFullSignature(iter)));
        }
        for (int iter = 0; iter < call.getOutput().getNumFullSignature(); iter++) {
            outputSignatures.add(new Signature(call.getOutput().getFullSignature(iter)));
        }
        this.functionName = this.astNodes.getIdentifier().getID();
    }

    public List<Signature> getInputSignatures() {
        return this.inputSignatures;
    }

    public List<Signature> getOutputSignatures() {
        return this.outputSignatures;
    }

    @Override
    public boolean isValid() {
        /* recursively check input prams */
        for (Signature iter : inputSignatures) if (!iter.isValid()) return false;
        /* recursively check output prams */
        for (Signature iter : outputSignatures) if (!iter.isValid()) return false;
        if (this.functionName.equals("..")) return false;
        return true;
    }

    @Override
    public IReport getValidationReport(String pFilepath) {
        Report retReport = new Report();
        for (Signature iter : inputSignatures) {
            for (Message message : iter.getValidationReport(pFilepath)) retReport.Add(message);
        }

        for (Signature iter : outputSignatures) {
            for (Message message : iter.getValidationReport(pFilepath)) retReport.Add(message);
        }
        if (this.functionName.equals("..")) retReport.AddError(
                pFilepath,
                this.astNodes.getIdentifier().getStartLine(),
                this.astNodes.getIdentifier().getStartColumn(),
                "wildcard [..] is not a valid matcher in call signature for function name, use [*] instead"
        );

        return retReport;
    }

    @Override
    public Class<? extends ASTNode> getASTPatternClass() {
        return PatternCall.class;
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
        if (this.outputSignatures.isEmpty()) {
            return String.format("%s(%s)", this.functionName, inputPramString);
        } else {
            return String.format("%s(%s):%s", this.functionName, inputPramString, outputPramString);
        }

    }
}
