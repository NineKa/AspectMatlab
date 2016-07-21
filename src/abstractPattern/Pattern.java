package abstractPattern;

import Matlab.Utils.IReport;
import ast.ASTNode;

public abstract class Pattern {
    public abstract Class<? extends ASTNode> getASTPatternClass();
    public abstract ASTNode getASTExpr();

    public abstract boolean isValid();
    public abstract IReport getValidationReport(String pFilepath);
}
