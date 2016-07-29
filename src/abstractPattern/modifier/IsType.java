package abstractPattern.modifier;

import Matlab.Utils.IReport;
import abstractPattern.Modifier;
import abstractPattern.utility.SignatureType;
import ast.ASTNode;
import ast.PatternIsType;
import transformer.RuntimeInfo;

import java.util.Collection;
import java.util.HashSet;

public class IsType extends Modifier {
    private PatternIsType astNodes = null;

    private SignatureType signature = null;

    public IsType(PatternIsType isType) {
        this.astNodes = isType;

        assert this.astNodes.getTypeSignature() != null;
        this.signature = new SignatureType(this.astNodes.getTypeSignature());
    }

    public SignatureType getSignature() {
        return signature;
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
        return PatternIsType.class;
    }

    @Override
    public String toString() {
        return String.format(
                "istype(%s)",
                this.signature.toString()
        );
    }

    @Override
    public boolean isPossibleWeave(ASTNode astNode, RuntimeInfo runtimeInfo) {
        /* need run-time check, we assume every pattern with type pattern is true */
        return true;
    }
}
