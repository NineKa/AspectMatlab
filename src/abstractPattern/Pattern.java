package abstractPattern;

import Matlab.Utils.IReport;
import abstractPattern.analysis.Analysis;
import ast.ASTNode;
import ast.Expr;

public abstract class Pattern {
    public abstract Class<? extends ASTNode> getASTPatternClass();

    public abstract boolean isValid();
    public abstract IReport getValidationReport(String pFilepath);
}
