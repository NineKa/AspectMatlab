package matcher.parameter;

import abstractPattern.utility.Signature;
import ast.*;
import util.Namespace;

import java.util.List;

public class EmptyMatcher {
    private final String trueReturn = "true";
    private final String falseReturn = "false";

    private List<Signature> signatures = null;
    private Namespace funcNamespace = null;
    private Namespace varNamespace = null;

    private String functionName = null;
    private String inPramName = null;
    private String outPramName = null;

    private Function generatedFunction = null;

    public EmptyMatcher(List<Signature> signatures, Namespace funcNamespace, Namespace varNamespace) {
        this.signatures = signatures;
        this.funcNamespace = funcNamespace;
        this.varNamespace = varNamespace;

        this.functionName = this.funcNamespace.generateNewName();
        this.inPramName = this.varNamespace.generateNewName();
        this.outPramName = this.varNamespace.generateNewName();

        /* --- generate function head --- */
        assert this.signatures.isEmpty();

        this.generatedFunction = new Function();
        this.generatedFunction.setName(new Name(this.functionName));
        this.generatedFunction.addInputParam(new Name(this.inPramName));
        this.generatedFunction.addOutputParam(new Name(this.outPramName));

        ParameterizedExpr checkingExpr = new ParameterizedExpr();
        checkingExpr.setTarget(new NameExpr(new Name("isempty")));
        checkingExpr.addArg(new NameExpr(new Name(this.inPramName)));

        AssignStmt trueAssignStmt = new AssignStmt();
        trueAssignStmt.setLHS(new NameExpr(new Name(this.outPramName)));
        trueAssignStmt.setRHS(new NameExpr(new Name(this.trueReturn)));
        trueAssignStmt.setOutputSuppressed(true);
        AssignStmt falseAssignStmt = new AssignStmt();
        falseAssignStmt.setLHS(new NameExpr(new Name(this.outPramName)));
        falseAssignStmt.setRHS(new NameExpr(new Name(this.falseReturn)));
        falseAssignStmt.setOutputSuppressed(true);

        IfBlock checkingIfBlock = new IfBlock(checkingExpr, new ast.List<>(trueAssignStmt));
        ElseBlock elseBlock = new ElseBlock(new ast.List<>(falseAssignStmt));
        this.generatedFunction.addStmt(new IfStmt(new ast.List<IfBlock>(checkingIfBlock), new Opt<ElseBlock>(elseBlock)));
    }

    public static boolean canEmptyMatched(List<Signature> signatures) {
        return signatures.isEmpty();
    }

    public Function getFunction() {
        return this.generatedFunction;
    }
}
