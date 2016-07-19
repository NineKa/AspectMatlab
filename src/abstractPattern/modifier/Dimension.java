package abstractPattern.modifier;

import Matlab.Utils.IReport;
import abstractPattern.Modifier;
import abstractPattern.utility.SignatureDimension;
import ast.ASTNode;
import ast.PatternDimension;

public class Dimension extends Modifier {
    private PatternDimension astNodes = null;

    private SignatureDimension signature = null;

    public Dimension(PatternDimension dimension) {
        this.astNodes = dimension;

        assert this.astNodes.getDimensionSignature() != null;
        this.signature = new SignatureDimension(this.astNodes.getDimensionSignature());
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
}
