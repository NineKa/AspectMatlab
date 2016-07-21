package abstractPattern.modifier;

import Matlab.Utils.IReport;
import Matlab.Utils.Report;
import abstractPattern.Modifier;
import abstractPattern.type.ScopeType;
import ast.ASTNode;
import ast.PatternWithin;

import java.util.Collection;
import java.util.HashSet;

public class Within extends Modifier {
    private PatternWithin astNodes = null;

    private ScopeType scopeType = null;
    private String identifier = null;

    public Within(PatternWithin within) {
        this.astNodes = within;

        assert this.astNodes.getType() != null;
        this.scopeType = ScopeType.fromString(this.astNodes.getType().getID());
        this.identifier = this.astNodes.getIdentifier().getID();
    }

    public ScopeType getScopeType() {
        return scopeType;
    }

    public String getIdentifier() {
        return identifier;
    }

    @Override
    public Collection<Modifier> getAllModfiierSet() {
        Collection<Modifier> collection = new HashSet<>();
        collection.add(this);
        return collection;
    }

    @Override
    public ASTNode getASTExpr() {
        return this.astNodes;
    }

    @Override
    public boolean isValid() {
        if (this.identifier.equals("..")) return false;

        return true;
    }

    @Override
    public IReport getValidationReport(String pFilepath) {
        Report retReport = new Report();
        if (this.identifier.equals("..")) retReport.AddError(
                pFilepath,
                this.astNodes.getIdentifier().getStartLine(),
                this.astNodes.getIdentifier().getStartColumn(),
                "wildcard [..] is not a valid matcher in within signature for scope name, use [*] instead"
        );
        return retReport;
    }

    @Override
    public Class<? extends ASTNode> getASTPatternClass() {
        return PatternWithin.class;
    }

    @Override
    public String toString() {
        return String.format(
                "within(%s : %s)",
                this.scopeType.toString(),
                this.identifier
        );
    }
}
