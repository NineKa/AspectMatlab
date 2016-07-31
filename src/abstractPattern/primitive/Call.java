package abstractPattern.primitive;

import Matlab.Utils.IReport;
import Matlab.Utils.Message;
import Matlab.Utils.Report;
import abstractPattern.Modifier;
import abstractPattern.Primitive;
import abstractPattern.modifier.Dimension;
import abstractPattern.modifier.IsType;
import abstractPattern.modifier.Within;
import abstractPattern.type.WeaveType;
import abstractPattern.utility.Signature;
import ast.*;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Call extends Primitive {
    private PatternCall astNodes = null;

    private List<Signature> inputSignatures = new LinkedList<>();
    private List<Signature> outputSignatures = new LinkedList<>();
    private String functionName = null;

    public Call(PatternCall call) {
        this.astNodes = call;
        // --- refactor ---
        assert(this.astNodes.getIdentifier() != null);
        if (this.astNodes.getInput() == null) this.astNodes.setInput(new Input(new ast.List<FullSignature>()));
        if (this.astNodes.getOutput() == null) this.astNodes.setOutput(new Output(new ast.List<FullSignature>()));
        // ----------------

        for (int iter = 0; iter < this.astNodes.getInput().getNumFullSignature(); iter++) {
            inputSignatures.add(new Signature(this.astNodes.getInput().getFullSignature(iter)));
        }
        for (int iter = 0; iter < this.astNodes.getOutput().getNumFullSignature(); iter++) {
            outputSignatures.add(new Signature(this.astNodes.getOutput().getFullSignature(iter)));
        }
        this.functionName = this.astNodes.getIdentifier().getID();
    }

    public List<Signature> getInputSignatures() {
        return this.inputSignatures;
    }

    public List<Signature> getOutputSignatures() {
        return this.outputSignatures;
    }

    public String getFunctionName() {
        return this.functionName;
    }

    public boolean needInputDimensionValidation() {
        for (Signature signature : inputSignatures) {
            if (signature.getDimension().needValidation()) return true;
        }
        return false;
    }

    public boolean needOutputDimensionValidation() {
        for (Signature signature : outputSignatures) {
            if (signature.getDimension().needValidation()) return true;
        }
        return false;
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
                "wildcard [..] is not a valid matcher in call pattern for function name, use [*] instead"
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
        String callStr = "";
        if (this.outputSignatures.isEmpty()) {
            callStr =  String.format("call(%s(%s))", this.functionName, inputPramString);
        } else {
            callStr =  String.format("call(%s(%s):%s)", this.functionName, inputPramString, outputPramString);
        }
        if (this.isModified()) {
            String appendingStr = "";
            for (int iter = 0; iter < this.getModifiers().size(); iter++) {
                appendingStr = appendingStr + this.getModifiers().get(iter);
                if (iter + 1 < this.getModifiers().size()) appendingStr = appendingStr + " & ";
            }
            return String.format("(%s & %s)", callStr, appendingStr);
        } else {
            return callStr;
        }
    }

    @Override
    public ASTNode getASTExpr() {
        return this.astNodes;
    }

    @Override
    public boolean isProperlyModified() {
        for (Modifier modifier : this.getBadicModifierSet()) {
            if (modifier instanceof Dimension)  continue;
            if (modifier instanceof IsType)     continue;
            if (modifier instanceof Within)     continue;
            /* control flow should not reach here */
            throw new AssertionError();
        }
        return true;
    }

    @Override
    public IReport getModifierValidationReport(String pFilepath) {  // TODO
        Report report = new Report();
        for (Modifier modifier : this.getBadicModifierSet()) {
            if (modifier instanceof Dimension) {
                if (this.outputSignatures.isEmpty()) continue;
                report.AddWarning(
                        pFilepath,
                        this.astNodes.getStartLine(),
                        this.astNodes.getStartColumn(),
                        String.format(
                                "apply dimension (%s[%d : %d]) pattern to such pattern, may result in no match",
                                modifier.toString(),
                                modifier.getASTExpr().getStartLine(),
                                modifier.getASTExpr().getStartColumn()
                        )
                );
                continue;
            }
            if (modifier instanceof IsType) {
                if (this.outputSignatures.isEmpty()) continue;
                report.AddWarning(
                        pFilepath,
                        this.astNodes.getStartLine(),
                        this.astNodes.getStartColumn(),
                        String.format(
                                "apply type (%s[%d : %d]) pattern to such pattern, may result in no match",
                                modifier.toString(),
                                modifier.getASTExpr().getStartLine(),
                                modifier.getASTExpr().getStartColumn()
                        )
                );
                continue;
            }
            if (modifier instanceof Within)     continue;
            /* control flow should not reach here */
            throw new AssertionError();
        }
        return report;
    }

    @Override
    public Map<WeaveType, Boolean> getWeaveInfo() {
        Map<WeaveType, Boolean> weaveTypeBooleanMap = new HashMap<>();
        boolean needReturnValidation = false;
        if (!this.outputSignatures.isEmpty()) needReturnValidation = true;
        for (Modifier modifier : this.getBadicModifierSet()) {
            if (modifier instanceof Dimension) needReturnValidation = true;
            if (modifier instanceof IsType) needReturnValidation = true;
        }
        weaveTypeBooleanMap.put(WeaveType.Before, (needReturnValidation)?false:true);
        weaveTypeBooleanMap.put(WeaveType.After, true);
        weaveTypeBooleanMap.put(WeaveType.Around, (needReturnValidation)?false:true);
        return weaveTypeBooleanMap;
    }
}
