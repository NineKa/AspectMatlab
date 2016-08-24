package transformer.jointpoint;

import abstractPattern.analysis.PatternType;
import ast.Expr;
import ast.FunctionHandleExpr;
import ast.Stmt;

public final class AMJointPointCall extends AMJointPoint {
    private Expr argumentExpr = null;
    private FunctionHandleExpr functionHandleExpr = null;

    public AMJointPointCall(Stmt inlineStatement, int startLine, int startColumn, String enclosingFile) {
        super(inlineStatement, startLine, startColumn, enclosingFile);
    }

    @Override
    public PatternType getMatchedPatternType() {
        return PatternType.Call;
    }

    public Expr getArgumentExpr() {
        return argumentExpr;
    }

    public void setArgumentExpr(Expr argumentExpr) {
        this.argumentExpr = argumentExpr;
    }

    public FunctionHandleExpr getFunctionHandleExpr() {
        return functionHandleExpr;
    }

    public void setFunctionHandleExpr(FunctionHandleExpr functionHandleExpr) {
        this.functionHandleExpr = functionHandleExpr;
    }

    public String getFunctionName() {
        return functionHandleExpr.getName().getID();
    }

    @Override
    public String toString() {
        return String.format(
                "[%10s] [%3d:%3d] %s with arguments: %s, actions: %s",
                getMatchedPatternType(),
                getStartLine(),
                getStartColumn(),
                getInlineStatement().getPrettyPrinted().trim(),
                getArgumentExpr().getPrettyPrinted().trim(),
                getMatchedActions().toString()
        );
    }
}
