package abstractPattern.modifier;

import Matlab.Utils.IReport;
import Matlab.Utils.Message;
import Matlab.Utils.Report;
import abstractPattern.Modifier;
import abstractPattern.analysis.PatternClassifier;
import ast.ASTNode;
import ast.OrExpr;
import transformer.RuntimeInfo;

import java.util.Collection;
import java.util.HashSet;

public class ModifierOr extends Modifier{
    private OrExpr astNodes = null;

    private Modifier lhs = null;
    private Modifier rhs = null;

    public ModifierOr(OrExpr orExpr) {
        this.astNodes = orExpr;
        this.lhs = PatternClassifier.buildModifier(orExpr.getLHS());
        this.rhs = PatternClassifier.buildModifier(orExpr.getRHS());
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
        return OrExpr.class;
    }

    @Override
    public String toString() {
        return String.format(
                "(%s | %s)",
                this.lhs.toString(),
                this.rhs.toString()
        );
    }

    @Override
    public boolean isPossibleWeave(ASTNode astNode, RuntimeInfo runtimeInfo) {
        return this.lhs.isPossibleWeave(astNode, runtimeInfo) || this.rhs.isPossibleWeave(astNode, runtimeInfo);
    }
}
