package abstractPattern.primitive;

import Matlab.Utils.IReport;
import Matlab.Utils.Message;
import Matlab.Utils.Report;
import abstractPattern.Modifier;
import abstractPattern.Primitive;
import ast.ASTNode;
import ast.AndExpr;

public class And extends Primitive{
    private AndExpr astNodes = null;

    private Primitive lhs = null;
    private Primitive rhs = null;

    public void setAndExpr(AndExpr astNodes) {
        this.astNodes = astNodes;
    }

    public void setRHS(Primitive rhs) {
        this.rhs = rhs;
    }

    public void setLHS(Primitive lhs) {
        this.lhs = lhs;
    }

    public Primitive getRHS() {
        return rhs;
    }

    public Primitive getLHS() {
        return lhs;
    }

    @Override
    public boolean isValid() {
        return this.lhs.isValid() && this.rhs.isValid();
    }

    @Override
    public IReport getValidationReport(String pFilepath) {
        Report report = new Report();
        for (Message message : this.lhs.getValidationReport(pFilepath)) report.Add(message);
        for (Message message : this.rhs.getValidationReport(pFilepath)) report.Add(message);
        return report;
    }

    @Override
    public Class<? extends ASTNode> getASTPatternClass() {
        return AndExpr.class;
    }

    @Override
    public String toString() {
        if (this.isModified()) {
            String appendingStr = "";
            for (int iter = 0; iter < this.getModifiers().size(); iter++) {
                appendingStr = appendingStr + this.getModifiers().get(iter);
                if (iter + 1 < this.getModifiers().size()) appendingStr = appendingStr + " & ";
            }
            return String.format("(%s & %s) & (%s)", this.lhs.toString(), this.rhs.toString(), appendingStr);
        } else {
            return String.format("(%s & %s)", this.lhs.toString(), this.rhs.toString());
        }
    }

    @Override
    public void addModifier(Modifier modifier) {
        this.lhs.addModifier(modifier);
        this.rhs.addModifier(modifier);
    }
}
