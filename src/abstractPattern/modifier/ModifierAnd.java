package abstractPattern.modifier;

import Matlab.Utils.IReport;
import Matlab.Utils.Message;
import Matlab.Utils.Report;
import abstractPattern.Modifier;
import abstractPattern.analysis.PatternClassifier;
import ast.ASTNode;
import ast.AndExpr;

import java.util.Collection;
import java.util.HashSet;

public class ModifierAnd extends Modifier{
    private AndExpr astNodes = null;

    private Modifier lhs = null;
    private Modifier rhs = null;

    public ModifierAnd(AndExpr andExpr) {
        this.astNodes = andExpr;
        this.lhs = PatternClassifier.buildModifier(andExpr.getLHS());
        this.rhs = PatternClassifier.buildModifier(andExpr.getRHS());
    }

    public Modifier getLHS() {
        return lhs;
    }

    @Deprecated
    public void setLHS(Modifier lhs) {
        this.lhs = lhs;
    }

    public Modifier getRHS() {
        return rhs;
    }

    @Deprecated
    public void setRHS(Modifier rhs) {
        this.rhs = rhs;
    }

    @Override
    public Collection<Modifier> getAllModfiierSet() {
        Collection<Modifier> collection = new HashSet<>();
        collection.addAll(this.lhs.getAllModfiierSet());
        collection.addAll(this.rhs.getAllModfiierSet());
        return collection;
    }

    @Override
    public ASTNode getASTExpr() {
        return this.astNodes;
    }

    @Override
    public boolean isValid() {
        return this.lhs.isValid() && this.rhs.isValid();
    }

    @Override
    public IReport getValidationReport(String pFilepath) {
        Report retReport = new Report();
        for (Message message : this.lhs.getValidationReport(pFilepath)) retReport.Add(message);
        for (Message message : this.rhs.getValidationReport(pFilepath)) retReport.Add(message);
        return retReport;
    }

    @Override
    public Class<? extends ASTNode> getASTPatternClass() {
        return AndExpr.class;
    }

    @Override
    public String toString() {
        return String.format(
                "(%s & %s)",
                this.lhs.toString(),
                this.rhs.toString()
        );
    }
}
