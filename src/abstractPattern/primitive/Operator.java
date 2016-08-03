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
import abstractPattern.type.OperatorType;
import abstractPattern.type.WeaveType;
import ast.ASTNode;
import ast.FullSignature;
import ast.PatternOperator;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Operator extends Primitive{
    private PatternOperator astNodes = null;

    private OperatorType operatorType = null;
    private List<Signature> operands = null;

    public Operator(PatternOperator operator) {
        this.astNodes = operator;
        /* --- refactor --- */
        assert this.astNodes.getType() != null;
        if (this.astNodes.getFullSignatureList() == null) this.astNodes.setFullSignatureList(new ast.List<>());
        /* ---------------- */
        this.operatorType = OperatorType.fromString(this.astNodes.getType().getID());
        this.operands = new LinkedList<>();
        for (int iter = 0; iter < this.astNodes.getFullSignatureList().getNumChild(); iter++) {
            FullSignature fullSignature = this.astNodes.getFullSignature(iter);
            this.operands.add(new Signature(fullSignature));
        }
    }

    public OperatorType getOperatorType() {
        return operatorType;
    }

    public List<Signature> getOperands() {
        return operands;
    }

    @Override
    public boolean isValid() {
        for (Signature signature : operands) if (!signature.isValid()) return false;
        int maxArgs = this.operatorType.getMaximumArgumentNumber();
        if (this.operands.size() > maxArgs) return false;
        return true;
    }

    @Override
    public IReport getValidationReport(String pFilepath) {
        Report report = new Report();
        for (Signature signature : operands) {
            for (Message message : signature.getValidationReport(pFilepath)) {
                report.Add(message);
            }
        }
        int maxArgs = this.operatorType.getMaximumArgumentNumber();
        if (this.operands.size() > maxArgs) {
            report.AddError(
                    pFilepath,
                    this.astNodes.getStartLine(),
                    this.astNodes.getStartColumn(),
                    String.format(
                            "operator pattern %s, expect maximum %d operand(s), but %d operand(s) found",
                            this.operatorType.toString(),
                            maxArgs,
                            this.operands.size()
                    )
            );
        }
        return report;
    }

    @Override
    public Class<? extends ASTNode> getASTPatternClass() {
        return PatternOperator.class;
    }

    @Override
    public String toString() {
        String operandStr = "";
        for (int iter = 0; iter < this.operands.size(); iter++) {
            Signature signature = this.operands.get(iter);
            operandStr = operandStr + signature.toString();
            if (iter + 1 < this.operands.size()) {
                operandStr = operandStr + ", ";
            }
        }
        String operatorStr = String.format("op(%s : %s)", this.operatorType.toString(), operandStr);
        if (this.isModified()) {
            String appendingStr = "";
            for (int iter = 0; iter < this.getModifiers().size(); iter++) {
                appendingStr = appendingStr + this.getModifiers().get(iter);
                if (iter + 1 < this.getModifiers().size()) appendingStr = appendingStr + " & ";
            }
            return String.format("(%s & %s)", operatorStr, appendingStr);
        } else {
            return operatorStr;
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
    public IReport getModifierValidationReport(String pFilepath) {
        Report report = new Report();

        for (Modifier modifier : this.getBadicModifierSet()) {
            if (modifier instanceof Dimension)  continue;
            if (modifier instanceof IsType)     continue;
            if (modifier instanceof Within)     continue;
            /* control flow should not reach here */
            throw new AssertionError();
        }

        return report;
    }

    @Override
    public Map<WeaveType, Boolean> getWeaveInfo() {
        Map<WeaveType, Boolean> weaveTypeBooleanMap = new HashMap<>();
        weaveTypeBooleanMap.put(WeaveType.Before, true);
        weaveTypeBooleanMap.put(WeaveType.After, true);
        weaveTypeBooleanMap.put(WeaveType.Around, true);
        return weaveTypeBooleanMap;
    }
}
