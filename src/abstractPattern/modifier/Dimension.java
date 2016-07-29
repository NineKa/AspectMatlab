package abstractPattern.modifier;

import Matlab.Utils.IReport;
import abstractPattern.Modifier;
import abstractPattern.utility.SignatureDimension;
import ast.ASTNode;
import ast.PatternDimension;
import transformer.RuntimeInfo;

import java.util.Collection;
import java.util.HashSet;

public class Dimension extends Modifier {
    private PatternDimension astNodes = null;

    private SignatureDimension signature = null;

    public Dimension(PatternDimension dimension) {
        this.astNodes = dimension;

        assert this.astNodes.getDimensionSignature() != null;
        this.signature = new SignatureDimension(this.astNodes.getDimensionSignature());
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
        return this.signature.isValid();
    }

    @Override
    public IReport getValidationReport(String pFilepath) {
        return this.signature.getValidationReport(pFilepath);
    }

    @Override
    public Class<? extends ASTNode> getASTPatternClass() {
        return PatternDimension.class;
    }

    @Override
    public String toString() {
        return String.format(
                "dimension(%s)",
                this.signature.toString()
        );
    }

    @Override
    public boolean isPossibleWeave(ASTNode astNode, RuntimeInfo runtimeInfo) {
        /* need run-time check, we assume every pattern with dimension pattern is true */
        return true;
    }
}
