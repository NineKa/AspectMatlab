package abstractPattern.primitive;

import Matlab.Utils.IReport;
import Matlab.Utils.Report;
import abstractPattern.Primitive;
import ast.ASTNode;
import ast.PatternMainExecution;

public class MainExecution extends Primitive{
    private PatternMainExecution astNodes = null;

    public MainExecution(PatternMainExecution mainExecution) {
        this.astNodes = mainExecution;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public IReport getValidationReport(String pFilepath) {
        return new Report();
    }

    @Override
    public Class<? extends ASTNode> getASTPatternClass() {
        return PatternMainExecution.class;
    }

    @Override
    public String toString() {
        String mainexecutonStr = "mainexecution()";
        if (this.isModified()) {
            String appendingStr = "";
            for (int iter = 0; iter < this.getModifiers().size(); iter++) {
                appendingStr = appendingStr + this.getModifiers().get(iter);
                if (iter + 1 < this.getModifiers().size()) appendingStr = appendingStr + " & ";
            }
            return String.format("(%s & %s)", mainexecutonStr, appendingStr);
        } else {
            return mainexecutonStr;
        }
    }

    @Override
    public ASTNode getASTExpr() {
        return this.astNodes;
    }

    @Override
    public boolean isProperlyModified() {   // TODO
        return false;
    }

    @Override
    public IReport getModifierValidationReport(String pFilepath) {  // TODO
        return null;
    }
}
