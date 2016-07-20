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
        return "mainexecution()";
    }
}