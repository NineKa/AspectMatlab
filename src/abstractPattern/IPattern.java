package abstractPattern;

import Matlab.Utils.IReport;
import ast.ASTNode;

public interface IPattern {
    public boolean isValid();
    public IReport getValidationReport(String pFilePath);

    public Class<? extends ASTNode> getASTNodeClass();
}
