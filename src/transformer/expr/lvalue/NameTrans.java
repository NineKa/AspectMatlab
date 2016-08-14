package transformer.expr.lvalue;

import abstractPattern.Primitive;
import abstractPattern.analysis.PatternType;
import ast.*;
import natlab.DecIntNumericLiteralValue;
import natlab.toolkits.analysis.varorfun.VFAnalysis;
import natlab.toolkits.analysis.varorfun.VFDatum;
import org.javatuples.Pair;
import transformer.expr.ExprTransArgument;
import transformer.util.AccessMode;
import transformer.util.IsPossibleJointPointResult;

import java.util.LinkedList;
import java.util.List;

public final class NameTrans extends LValueTrans {
    public NameTrans(ExprTransArgument argument, NameExpr nameExpr) {
        super(argument, nameExpr);
    }

    @Override
    public Pair<Expr, List<Stmt>> copyAndTransform() {
        assert originalNode instanceof NameExpr;
        VFAnalysis kindAnalysis = this.runtimeInfo.kindAnalysis;
        VFDatum kindAnalsysisResult = kindAnalysis.getResult(((NameExpr) originalNode).getName());

        if (hasTransformOnCurrentNode()) {
            if (kindAnalsysisResult.isFunction()) {
                /* match to a function call (with no argument)  */
                /* [expr]   <=>     t0 = {}                     */
                /*                  t1 = [expr](t0{:})  *       */
                /*                  return t1                   */
                List<Stmt> newPrefixStatementList = new LinkedList<>();

                String t0Name = this.alterNamespace.generateNewName();
                AssignStmt t0Assign = new AssignStmt();
                t0Assign.setLHS(new NameExpr(new Name(t0Name)));
                t0Assign.setRHS(new CellArrayExpr(new ast.List<Row>()));
                t0Assign.setOutputSuppressed(true);
                newPrefixStatementList.add(t0Assign);

                String t1Name = this.alterNamespace.generateNewName();
                AssignStmt t1Assign = new AssignStmt();
                t1Assign.setLHS(new NameExpr(new Name(t1Name)));
                t1Assign.setRHS(new ParameterizedExpr(
                        (NameExpr) this.originalNode.treeCopy(),
                        new ast.List<Expr>(new CellIndexExpr(
                                new NameExpr(new Name(t0Name)),
                                new ast.List<Expr>(new ColonExpr())
                        ))
                ));
                t1Assign.setOutputSuppressed(true);
                newPrefixStatementList.add(t1Assign);

                this.jointPointDelegate.accept(new Pair<>(t1Assign, PatternType.Call));
                return new Pair<>(new NameExpr(new Name(t1Name)), newPrefixStatementList);
            }

            if (kindAnalsysisResult.isVariable()) {
                if (runtimeInfo.accessMode == AccessMode.Read) {
                    /* match to a variable get pattern              */
                    /* [expr]   <=>     t0 = [expr]     *           */
                    /*                  return t0                   */
                    List<Stmt> newPrefixStatementList = new LinkedList<>();

                    String t0Name = this.alterNamespace.generateNewName();
                    AssignStmt t0Assign = new AssignStmt();
                    t0Assign.setLHS(new NameExpr(new Name(t0Name)));
                    t0Assign.setRHS((NameExpr) this.originalNode.treeCopy());
                    t0Assign.setOutputSuppressed(true);
                    newPrefixStatementList.add(t0Assign);

                    this.jointPointDelegate.accept(new Pair<>(t0Assign, PatternType.Get));
                    return new Pair<>(new NameExpr(new Name(t0Name)), newPrefixStatementList);
                }

                if (runtimeInfo.accessMode == AccessMode.Write) {
                    /* match to a variable set pattern              */
                    /* [expr]   <=>     if exist('[expr]', 'var')   */
                    /*                      t0 = [expr]             */
                    /*                  end                         */
                    /*                  return t0                   */
                    /* leave joint point notify to stmt transformer */
                    List<Stmt> newPrefixStatementList = new LinkedList<>();

                    IfBlock checkingBlock = new IfBlock();
                    checkingBlock.setCondition(new ParameterizedExpr(
                            new NameExpr(new Name("exist")),
                            new ast.List<Expr>(
                                    new StringLiteralExpr(((NameExpr) originalNode).getName().getID()),
                                    new StringLiteralExpr("var")
                            )
                    ));

                    String t0Name = this.alterNamespace.generateNewName();
                    AssignStmt t0Assign = new AssignStmt();
                    t0Assign.setLHS(new NameExpr(new Name(t0Name)));
                    t0Assign.setRHS((NameExpr) this.originalNode.treeCopy());
                    t0Assign.setOutputSuppressed(true);
                    checkingBlock.addStmt(t0Assign);

                    IfStmt appendingIfStmt = new IfStmt(new ast.List<>(checkingBlock), new Opt<>());
                    newPrefixStatementList.add(appendingIfStmt);
                    return new Pair<>(new NameExpr(new Name(t0Name)), newPrefixStatementList);
                }

                /* control flow should not reach here */
                throw new AssertionError();
            }

            if (kindAnalsysisResult.isID()) {
                assert (runtimeInfo.accessMode == AccessMode.Read);
                /* [expr]   <=>     if any(cellfun(@(x)(exist('[expr]') == x), {5,3,6,2,4,8}))                  */
                /*                      t1 = {}                                                                 */
                /*                      t2 = [expr](t1{:})                                      *(optional)     */
                /*                  else                                                                        */
                /*                      t2 = [expr]                                             *(optional)     */
                /*                  end                                                                         */
                /*                  return t2                                                                   */

                List<Stmt> newPrefixStatementList = new LinkedList<>();

                ParameterizedExpr existCallExpr = new ParameterizedExpr(
                        new NameExpr(new Name("cellfun")),
                        new ast.List<>(
                                new LambdaExpr(
                                        new ast.List<>(new Name("x")),
                                        new EQExpr(
                                                new ParameterizedExpr(
                                                        new NameExpr(new Name("exist")),
                                                        new ast.List<>(new StringLiteralExpr(((NameExpr) originalNode).getName().getID()))
                                                ),
                                                new NameExpr(new Name("x"))
                                        )
                                ),
                                new CellArrayExpr(new ast.List<Row>(new Row(new ast.List<Expr>(
                                        new IntLiteralExpr(new DecIntNumericLiteralValue("5")),
                                        new IntLiteralExpr(new DecIntNumericLiteralValue("3")),
                                        new IntLiteralExpr(new DecIntNumericLiteralValue("6")),
                                        new IntLiteralExpr(new DecIntNumericLiteralValue("2")),
                                        new IntLiteralExpr(new DecIntNumericLiteralValue("4")),
                                        new IntLiteralExpr(new DecIntNumericLiteralValue("8"))
                                ))))
                        )
                );

                /* building function block */
                IfBlock functionBlock = new IfBlock();
                functionBlock.setCondition(new ParameterizedExpr(
                        new NameExpr(new Name("any")),
                        new ast.List<Expr>(existCallExpr)
                ));

                String t1Name = this.alterNamespace.generateNewName();
                AssignStmt t1Assign = new AssignStmt();
                t1Assign.setLHS(new NameExpr(new Name(t1Name)));
                t1Assign.setRHS(new CellArrayExpr(new ast.List<Row>()));
                t1Assign.setOutputSuppressed(true);
                functionBlock.addStmt(t1Assign);

                String t2Name = this.alterNamespace.generateNewName();
                AssignStmt funct2Assign = new AssignStmt();
                funct2Assign.setLHS(new NameExpr(new Name(t2Name)));
                funct2Assign.setRHS(new ParameterizedExpr(
                        (NameExpr) originalNode.treeCopy(),
                        new ast.List<Expr>(new CellIndexExpr(
                                new NameExpr(new Name(t1Name)),
                                new ast.List<Expr>(new ColonExpr())
                        ))
                ));
                funct2Assign.setOutputSuppressed(true);
                functionBlock.addStmt(funct2Assign);

                /* building variable get block */
                ElseBlock variableBlock = new ElseBlock();

                AssignStmt vart2Assign = new AssignStmt();
                vart2Assign.setLHS(new NameExpr(new Name(t2Name)));
                vart2Assign.setRHS((NameExpr) originalNode.treeCopy());
                vart2Assign.setOutputSuppressed(true);
                variableBlock.addStmt(vart2Assign);

                /* finalize if statement */
                IfStmt appendingIfStmt = new IfStmt(new ast.List<>(functionBlock), new Opt<>(variableBlock));
                newPrefixStatementList.add(appendingIfStmt);

                /* check optional joint point delegate call back */
                boolean reportCall = false;
                boolean reportGet  = false;
                for (abstractPattern.Action action : this.actions) {
                    assert action.getPattern() instanceof Primitive;
                    Primitive pattern = (Primitive) action.getPattern();
                    IsPossibleJointPointResult query = pattern.isPossibleJointPoint(originalNode, runtimeInfo);
                    if (query.isCalls) reportCall = true;
                    if (query.isGets ) reportGet = true;
                    assert !query.isSets;
                }
                assert reportCall || reportGet;

                if (reportCall) jointPointDelegate.accept(new Pair<>(funct2Assign, PatternType.Call));
                if (reportGet) jointPointDelegate.accept(new Pair<>(vart2Assign, PatternType.Get));

                return new Pair<>(new NameExpr(new Name(t2Name)), newPrefixStatementList);
            }

            /* control flow should not reach here */
            throw new AssertionError();
        } else {
            NameExpr copiedNode = (NameExpr) this.originalNode.treeCopy();
            return new Pair<>(copiedNode, new LinkedList<>());
        }
    }

    @Override
    public boolean hasFurtherTransform() {
        if (hasTransformOnCurrentNode()) return true;
        return false;
    }
}
