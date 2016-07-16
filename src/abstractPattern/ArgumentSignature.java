package abstractPattern;

import Matlab.Utils.IReport;
import Matlab.Utils.Report;
import ast.*;
import ast.List;

import java.util.*;

public class ArgumentSignature implements IPattern{
    private FullSignature signature = null;

    public ArgumentSignature(FullSignature init) {
        this.signature = init.treeCopy();
    }

    public List<Name> getDimensionSignature() {
        DimensionSignature dimensionSignature = this.signature.getDimensionSignature();
        if (dimensionSignature == null) {
            /* trivial cases, use default pattern [..] */
            List<Name> retList = new List<>();
            retList.add(new Name(".."));
            return retList;
        } else {
            List<Name> retList = new List<>();
            for (int iter = 0; iter < dimensionSignature.getNumDimension(); iter++) {
                retList.add(new Name(dimensionSignature.getDimension(iter).getID()));
            }
            return retList;
        }
    }

    public Name getTypeSignature() {
        TypeSignature typeSignature = this.signature.getTypeSignature();
        if (typeSignature == null) {
            /* trival cases, use default pattern "*" */
            return new Name("*");
        } else {
            return this.signature.getTypeSignature().getType().treeCopy();
        }
    }

    public boolean needDimensionValidation() {
        List<Name> dimensionSignature = this.getDimensionSignature();
        /* skip pattern like [.., .., ..] */
        boolean skipFlag = true;
        for (Name iter : dimensionSignature) if (!iter.getID().equals("..")) skipFlag = false;
        if (skipFlag) return false;
        return true;
    }

    public boolean needTypeValidation() {
        Name typeSignature = this.getTypeSignature();
        if (typeSignature.getID().equals("*")) return false;
        if (typeSignature.getID().equals("..")) return false;
        return true;
    }

    public boolean isValid() {
        List<Name> dimensionSignature = this.getDimensionSignature();
        /* reject dimension pattern such as [] */
        if (dimensionSignature.getNumChild() == 0) return false;
        return true;
    }

    public IReport getValidationReport(String pFilePath) {
        Report retReport = new Report();
        /* add waring pattern such as [.., ..] */
        for (int iter = 0; iter < this.getDimensionSignature().getNumChild() - 1; iter++) {
            Name currentSign = this.getDimensionSignature().getChild(iter);
            Name nextSign = this.getDimensionSignature().getChild(iter + 1);
            if (currentSign.getID().equals("..") && nextSign.getID().equals("..")) {
                retReport.AddWarning(
                        pFilePath,
                        signature.getStartLine(),
                        signature.getStartColumn(),
                        "redundant pattern [.., ..], use [..] instead"
                );
            }
        }

        return retReport;
    }

    public Class<? extends ASTNode> getASTNodeClass() { return FullSignature.class; }

    @Override public String toString() {
        java.util.List<String> dimensionList = new LinkedList();
        List<Name> dimensionASTList = this.getDimensionSignature();
        for (int iter = 0; iter < dimensionASTList.getNumChild(); iter++) {
            dimensionList.add(dimensionASTList.getChild(iter).getID());
        }
        return this.getTypeSignature().getID() + dimensionList.toString();
    }

    @Override public boolean equals(Object target) {
        if (!(target instanceof ArgumentSignature)) return false;
        List<Name> targetDimSign = ((ArgumentSignature)target).getDimensionSignature();
        List<Name> currentDimSign = this.getDimensionSignature();
        if (targetDimSign.getNumChild() != currentDimSign.getNumChild()) return false;
        for (int iter = 0; iter < targetDimSign.getNumChild(); iter++) {
            if (!targetDimSign.getChild(iter).getID().equals(currentDimSign.getChild(iter).getID())) {
                return false;
            }
        }
        Name targetTypeSign = ((ArgumentSignature)target).getTypeSignature();
        Name currentTypeSign = this.getTypeSignature();
        return targetTypeSign.getID().equals(currentTypeSign.getID());
    }
}
