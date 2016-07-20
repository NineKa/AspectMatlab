package abstractPattern.primitive;

import Matlab.Utils.IReport;
import Matlab.Utils.Message;
import Matlab.Utils.Report;
import abstractPattern.Primitive;
import abstractPattern.type.OperatorType;
import abstractPattern.utility.Signature;
import ast.ASTNode;
import ast.FullSignature;
import ast.PatternOperator;

import java.util.List;

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
        return String.format(
                "op(%s : %s)",
                this.operatorType.toString(),
                operandStr
        );
    }
}
