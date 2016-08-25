package transformer.joinpoint;

import abstractPattern.analysis.PatternType;
import ast.Expr;
import ast.Stmt;

import java.util.LinkedList;
import java.util.List;

public final class AMJoinPointOperator extends AMJoinPoint {
    private List<Expr> operandsList = new LinkedList<>();

    public AMJoinPointOperator(Stmt inlineStatement, int startLine, int startColumn, String enclosingFilename) {
        super(inlineStatement, startLine, startColumn, enclosingFilename);
    }

    @Override
    public PatternType getMatchedPatternType() {
        return PatternType.Operator;
    }

    public void addOperand(Expr expr) {
        operandsList.add(expr);
    }

    public List<Expr> getOperandsList() {
        return operandsList;
    }

    @Override
    public String toString() {
        List<String> exprString = new LinkedList<>();
        operandsList.forEach((expr -> exprString.add(expr.getPrettyPrinted().trim())));

        return String.format(
                "[%10s] [%3d:%3d] %s with operands: %s, actions: %s",
                getMatchedPatternType(),
                getStartLine(),
                getStartColumn(),
                getInlineStatement().getPrettyPrinted().trim(),
                exprString.toString(),
                getMatchedActions().toString()
        );
    }
}
