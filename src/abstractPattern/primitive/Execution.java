package abstractPattern.primitive;

import Matlab.Utils.IReport;
import Matlab.Utils.Message;
import Matlab.Utils.Report;
import abstractPattern.Modifier;
import abstractPattern.Primitive;
import abstractPattern.modifier.Dimension;
import abstractPattern.modifier.IsType;
import abstractPattern.modifier.Within;
import abstractPattern.signature.Signature;
import abstractPattern.type.WeaveType;
import ast.*;
import transformer.IsPossibleJointPointResult;
import transformer.RuntimeInfo;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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

    @Override
    public IsPossibleJointPointResult isPossibleJointPoint(ASTNode astNode, RuntimeInfo runtimeInfo) {
        /* structure check */
        if (!(astNode instanceof Function)) { /* false return*/
            IsPossibleJointPointResult result = new IsPossibleJointPointResult();
            result.reset();
            return result;
        }
        assert astNode instanceof Function;
        /* function name check */
        String actualName = ((Function) astNode).getName().getID();
        if (!this.functionName.equals("*") && !this.functionName.equals(actualName)) { /* name mismatch */
            IsPossibleJointPointResult result = new IsPossibleJointPointResult();
            result.reset();
            return result;
        }

        /* static signature check */
        /* Input Section  : */ boolean inputStaticPossible  = true;
        /* Output Section : */ boolean outputStaticPossible = true;

        /* static check input parameters (if number could possibly matched) */
        boolean inputFixedPattern = true; int inputMinNumberPattern = this.getInputSignatures().size();
        boolean inputFixedInput   = true; int inputMinNumberInput   = ((Function) astNode).getNumInputParam();
        for (Signature signature : this.getInputSignatures()) {
            /* [..] wildcards may consume more than one token -> not fixed number of match */
            if (signature.getType().getSignature().equals("..")) {
                inputFixedPattern = false;
                inputMinNumberPattern = inputMinNumberPattern - 1;
            }
        }
        for (Name arg : ((Function) astNode).getInputParamList()) {
            /* varargin <=> variable-length input argument list */
            if (arg.getID().equals("varargin")) {
                inputFixedInput = false;
                inputMinNumberInput = inputMinNumberInput - 1;
            }
        }
        if (inputFixedPattern) {
            if (inputFixedInput) {
                if (inputMinNumberInput != inputMinNumberPattern) inputStaticPossible = false;
            } else {
                if (inputMinNumberPattern < inputMinNumberInput) inputStaticPossible = false;
            }
        } else {
            if (inputFixedInput) {
                if (inputMinNumberInput < inputMinNumberPattern) inputStaticPossible = false;
            } else {
                /* both pattern and candidate's size can not static decide */
                /* filtering impossible, leave for dynamic check */
            }
        }

        /* static check output parameters (if number could possibly matched) */
        boolean outputFixedPattern = true; int outputMinNumberPattern = this.getOutputSignatures().size();
        boolean outputFixedInput   = true; int outputMinNumberInput   = ((Function) astNode).getNumOutputParam();
        for (Signature signature : this.getOutputSignatures()) {
            /* [..] wildcards may consume more than one token -> not fixed number of match */
            if (signature.getType().getSignature().equals("..")) {
                outputFixedPattern = false;
                outputMinNumberPattern = outputMinNumberPattern - 1;
            }
        }
        for (Name arg : ((Function) astNode).getOutputParamList()) {
            /* varargout <=> variable-length output argument list */
            if (arg.getID().equals("varargout")) {
                outputFixedInput = false;
                outputMinNumberInput = outputMinNumberInput - 1;
            }
        }
        if (outputFixedPattern) {
            if (outputFixedInput) {
                if (outputMinNumberInput != outputMinNumberPattern) outputStaticPossible = false;
            } else {
                if (outputMinNumberPattern < outputMinNumberInput) outputStaticPossible = false;
            }
        } else {
            if (outputFixedInput) {
                if (outputMinNumberInput < outputMinNumberPattern) outputStaticPossible = false;
            } else {
                /* both pattern and candidate's size can not static decide */
                /* filtering impossible, leave for dynamic check */
            }
        }

        /* static check summary */
        if (!inputStaticPossible || !outputStaticPossible) { /* false return */
            IsPossibleJointPointResult result = new IsPossibleJointPointResult();
            result.reset();
            return result;
        }

        /* claim such pattern is possibly matched joint point */
        IsPossibleJointPointResult result = new IsPossibleJointPointResult();
        result.isExecutions = true;
        return result;
    }
}
