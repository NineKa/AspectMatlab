package abstractPattern.utility;

import Matlab.Utils.IReport;
import Matlab.Utils.Message;
import Matlab.Utils.Report;
import abstractPattern.Pattern;
import ast.*;

public class Signature extends Pattern{
    private FullSignature astNodes = null;
    private SignatureDimension dimension = null;
    private SignatureType type = null;

    public Signature(FullSignature signature) {
        this.astNodes = signature;
        // --- refactor ---
        if (this.astNodes.getDimensionSignature() == null) {
            this.astNodes.setDimensionSignature(new DimensionSignature(new List<Name>(new Name(".."))));
        }
        if (this.astNodes.getTypeSignature() == null) {
            this.astNodes.setTypeSignature(new TypeSignature(new Name("*")));
        }
        // ----------------

        DimensionSignature dimensionSignature = signature.getDimensionSignature();
        TypeSignature typeSignature = signature.getTypeSignature();
        this.dimension = new SignatureDimension(dimensionSignature);
        this.type = new SignatureType(typeSignature);
    }

    public SignatureDimension getDimension() {
        return this.dimension;
    }

    public SignatureType getType() {
        return this.type;
    }

    @Override
    public boolean isValid() {
        if (!this.dimension.isValid()) return false;
        if (!this.type.isValid()) return false;

        return true;
    }

    @Override
    public IReport getValidationReport(String pFilepath) {
        Report retReport = new Report();

        IReport dimReport= this.dimension.getValidationReport(pFilepath);
        IReport typeReprot = this.type.getValidationReport(pFilepath);

        for (Message iter : dimReport) retReport.Add(iter);
        for (Message iter : typeReprot) retReport.Add(iter);

        return retReport;
    }

    @Override public Class<? extends ASTNode> getASTPatternClass() {
        return FullSignature.class;
    }

    @Override
    public String toString() {
        return this.type.toString() + this.dimension.toString();
    }
}
