package abstractPattern.utility;

import Matlab.Utils.IReport;
import Matlab.Utils.Report;
import abstractPattern.Pattern;
import ast.ASTNode;
import ast.TypeSignature;

public class SignatureType extends Pattern {
    private TypeSignature astNodes = null;
    private String signatureType = null;

    public SignatureType(TypeSignature signature) {
        this.astNodes = signature;
        this.signatureType = signature.getType().getID();
    }

    public boolean needValidation() {
        if (this.signatureType.equals("*")) return false;
        if (this.signatureType.equals("..")) return false;
        return true;
    }

    public boolean isFixMatch() {
        if (this.signatureType.equals("..")) return false;
        return true;
    }

    public boolean isWildcard() {
        if (this.signatureType.equals("*")) return true;
        if (this.signatureType.equals("..")) return true;
        return false;
    }

    public String getSignature() {
        return this.signatureType;
    }

    @Override
    public boolean isValid() {
        if (signatureType.equals("..")) return false;
        return true;
    }

    @Override
    public IReport getValidationReport(String pFilepath) {
        Report retReport = new Report();
        if (signatureType.equals("..")) retReport.AddError(
                pFilepath,
                this.astNodes.getStartLine(),
                this.astNodes.getStartColumn(),
                "wildcard [..] is not a valid matcher in type signature, use [*] instead"
        );

        return retReport;
    }

    @Override
    public Class<? extends ASTNode> getASTPatternClass() {
        return TypeSignature.class;
    }

    @Override
    public String toString() {
        return this.signatureType;
    }
}
