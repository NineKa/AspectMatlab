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
    public boolean isPossibleJointPoint(ASTNode astNode, RuntimeInfo runtimeInfo) {
        /* --- structure check --- */
        if (!(astNode instanceof Function)) return false;
        /* --- check the name of the function --- */
        String candidateName = ((Function) astNode).getName().getID();
        String matchName = this.functionName;
        if (!matchName.equals("*") && !matchName.equals(candidateName)) return false;
        /* --- check if the number of input arg could satisfy the pattern --- */
        boolean isInFixNumMatch = true;
        for (Signature signature : inputSignatures) {
            if (signature.getType().getSignature().equals("..")) isInFixNumMatch = false;
        }
        boolean isInFixNumCandidate = true;
        for (Name name : ((Function) astNode).getInputParamList()) {
            if (name.getID().equals("varargin")) isInFixNumCandidate = false;
        }
        if (isInFixNumMatch) {
            if (isInFixNumCandidate) {
                /* fix number of matcher   */
                /* fix number of candidate */
                int numMatcher = this.inputSignatures.size();
                int numCandidate = ((Function) astNode).getNumInputParam();
                if (numMatcher != numCandidate) return false;
            } else {
                /* fix number of matcher        */
                /* variable number of candidate */
                int numMatcher = this.inputSignatures.size();
                int minNumInCandidate = ((Function) astNode).getNumInputParam();
                for (Name name : ((Function) astNode).getInputParamList()) {
                    if (name.getID().equals("varargin")) minNumInCandidate  = minNumInCandidate - 1;
                }
                if (numMatcher < minNumInCandidate) return false;
            }
        } else {
            if (isInFixNumCandidate) {
                /* variable number of matcher */
                /* fix number of candidate    */
                int minNumInMatcher = this.inputSignatures.size();
                int numInCandidate = ((Function) astNode).getNumInputParam();
                for (Signature signature : this.inputSignatures) {
                    if (signature.getType().getSignature().equals("..")) minNumInMatcher = minNumInMatcher - 1;
                }
                if (numInCandidate < minNumInMatcher) return false;
            } else {
                /* variable number of matcher   */
                /* variable number of candidate */
                /* IGNORE in the static decision, leave to dynamic check */
            }
        }
        /* --- check if the number of output arg could satisfy the pattern --- */
        boolean isOutFixNumMatch = true;
        for (Signature signature : outputSignatures) {
            if (signature.getType().getSignature().equals("..")) isOutFixNumMatch = false;
        }
        boolean isOutFixNumCandidate = true;
        for (Name name : ((Function) astNode).getOutputParamList()) {
            if (name.getID().equals("varargout")) isOutFixNumCandidate = false;
        }
        if (isOutFixNumMatch) {
            if (isOutFixNumCandidate) {
                /* fix number of matcher   */
                /* fix number of candidate */
                int numMatcher = this.outputSignatures.size();
                int numCandidate = ((Function) astNode).getNumOutputParam();
                if (numMatcher != numCandidate) return false;
            } else {
                /* fix number of matcher        */
                /* variable number of candidate */
                int minNumCandidate = ((Function) astNode).getNumOutputParam();
                for (Name name : ((Function) astNode).getOutputParamList()) {
                    if (name.getID().equals("varargout")) minNumCandidate = minNumCandidate - 1;
                }
                int numMatcher = this.outputSignatures.size();
                if (minNumCandidate > numMatcher) return false;
            }
        } else {
            if (isOutFixNumCandidate) {
                /* variable number of matcher */
                /* fix number of candidate    */
                int numCandidate = ((Function) astNode).getNumOutputParam();
                int minNumMatcher = outputSignatures.size();
                for (Signature signature : outputSignatures) {
                    if (signature.getType().getSignature().equals("..")) minNumMatcher = minNumMatcher - 1;
                }
                if (numCandidate < minNumMatcher) return false;
            } else {
                /* variable number of matcher   */
                /* variable number of candidate */
                /* IGNORE in the static decision, leave to dynamic check */
            }
        }
        return true;
    }
}
