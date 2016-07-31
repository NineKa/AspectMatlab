package abstractPattern.modifier;

import Matlab.Utils.IReport;
import abstractPattern.Modifier;
import abstractPattern.analysis.PatternClassifier;
import ast.ASTNode;
import ast.NotExpr;

import java.util.Collection;

public class ModifierNot extends Modifier{
    private NotExpr astNodes = null;

    private Modifier operand = null;

    public ModifierNot(NotExpr notExpr) {
        this.astNodes = notExpr;
        this.operand = PatternClassifier.buildModifier(notExpr.getOperand());
    }

    public Modifier getOperand() {
        return operand;
    }

    public void setOperand(Modifier operand) {
        this.operand = operand;
    }

    @Override
    public Collection<Modifier> getAllModfiierSet() {
        return this.operand.getAllModfiierSet();
    }

    @Override
    public ASTNode getASTExpr() {
        return this.astNodes;
    }

    @Override
    public boolean isValid() {
        return this.operand.isValid();
    }

    @Override
    public IReport getValidationReport(String pFilepath) {
        return this.operand.getValidationReport(pFilepath);
    }

    @Override
    public Class<? extends ASTNode> getASTPatternClass() {
        return NotExpr.class;
    }

    @Override
    public String toString() {
        return String.format(
                "~%s",
                this.operand.toString()
        );
    }
}
