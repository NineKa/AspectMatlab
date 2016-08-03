package matcher.parameter;

import abstractPattern.signature.Signature;
import ast.*;
import matcher.alphabet.Alphabet;
import matcher.alphabet.DimAlphabetCompareFunc;
import matcher.dfa.DFA;
import natlab.DecIntNumericLiteralValue;
import util.Namespace;

import java.util.*;
import java.util.List;
import java.util.function.BiFunction;

public class FullMatcher {
    private final String falseReturn = "false";
    private final String trueReturn = "true";

    private List<Signature> signatures = null;
    private Alphabet<Integer> dimAlphabet = null;
    private Namespace funcNamespace = null;
    private Namespace varNamespace = null;

    private String functionName = null;
    private String inPramName = null;
    private String outPramName = null;

    private String validationArrayName = null;

    private Function alphabetFunction = null;
    private Map<Integer, Expr> variableAccessMap = null;
    private Map<Integer, Expr> validationArrayAccessMap = null;
    private Map<Integer, Expr> posArrayAccessMap = null;
    private Map<Signature, Function> dimValidationFuncMap = null;

    private Function generatedFunction = new Function();

    private void initVariableAccessMap() {
        this.variableAccessMap = new HashMap<>();
        for (int iter = 0; iter < this.signatures.size(); iter++) {
            CellIndexExpr appendingExpr = new CellIndexExpr();
            appendingExpr.setTarget(new NameExpr(new Name(inPramName)));
            appendingExpr.addArg(new IntLiteralExpr(new DecIntNumericLiteralValue(String.valueOf(iter + 1))));
            this.variableAccessMap.put(iter, appendingExpr);
        }
    }
    private void initValidationArrayAccessMap() {
        this.validationArrayAccessMap = new HashMap<>();
        this.validationArrayName = this.varNamespace.generateNewName();
        for (int iter = 0; iter < this.signatures.size(); iter++) {
            ParameterizedExpr appendingExpr = new ParameterizedExpr();
            appendingExpr.setTarget(new NameExpr(new Name(this.validationArrayName)));
            appendingExpr.addArg(new IntLiteralExpr(new DecIntNumericLiteralValue(String.valueOf(iter + 1))));
            this.validationArrayAccessMap.put(iter, appendingExpr);
        }
    }
    private void initPosArrayAccessMap() {
        this.posArrayAccessMap = new HashMap<>();
        for (int iter = 0; iter < this.signatures.size(); iter++) {
            this.posArrayAccessMap.put(iter, new NameExpr(new Name(this.varNamespace.generateNewName())));
        }
    }
    private void initDimValidationFuncMap() {
        /* collect Alphabet */
        this.dimAlphabet = new Alphabet<>();
        this.dimValidationFuncMap = new HashMap<>();
        for (Signature signature : signatures) {
            this.dimAlphabet = this.dimAlphabet.merge(signature.getDimension().generateAlphabet());
        }
        this.alphabetFunction = this.dimAlphabet.generateMatlabFunc(
                new DimAlphabetCompareFunc(),
                this.funcNamespace,
                this.varNamespace
        );
        for (Signature signature : signatures) {
            if (signature.getDimension().needValidation()) {
                DFA dfa = signature.getDimension().generateDFA(this.dimAlphabet);
                Function validationFunction = dfa.getMatlabMatchFunction(
                        this.alphabetFunction,
                        this.funcNamespace,
                        this.varNamespace
                );
                this.dimValidationFuncMap.put(signature, validationFunction);
            } else {
                this.dimValidationFuncMap.put(signature, null);
            }
        }
    }

    public FullMatcher(List<Signature> signatures, Namespace funcNamespace, Namespace varNamespace) {
        assert !signatures.isEmpty();

        this.signatures = signatures;
        this.funcNamespace = funcNamespace;
        this.varNamespace = varNamespace;

        this.functionName = funcNamespace.generateNewName();
        this.inPramName = varNamespace.generateNewName();
        this.outPramName = varNamespace.generateNewName();

        this.generatedFunction.setName(new Name(this.functionName));
        this.generatedFunction.setInputParamList(new ast.List<>(new Name(this.inPramName)));
        this.generatedFunction.setOutputParamList(new ast.List<>(new Name(this.outPramName)));

        initVariableAccessMap();
        initValidationArrayAccessMap();
        initPosArrayAccessMap();
        initDimValidationFuncMap();
        /* --- adding nested Functions --- */
        for (Signature iter : signatures) {
            if (dimValidationFuncMap.get(iter) != null) {
                this.generatedFunction.addNestedFunction(dimValidationFuncMap.get(iter));
            }
        }
        this.generatedFunction.addNestedFunction(this.alphabetFunction);
        /* ------------------------------- */

        /* --- build up function --- */
        ast.List<Stmt> stmts = generateWrappedCheckingBlock(generateValidationBlock());
        this.generatedFunction.setStmtList(stmts);
    }

    private ast.List<Stmt> generateValidationBlock() {
        Map<Integer, Expr> posIndexVariableAccessMap = new HashMap<>();
        for (int iter = 0; iter < this.signatures.size(); iter++) {
            CellIndexExpr appendingExpr = new CellIndexExpr();
            appendingExpr.setTarget(new NameExpr(new Name(inPramName)));
            appendingExpr.addArg(this.posArrayAccessMap.get(iter).treeCopy());
            posIndexVariableAccessMap.put(iter, appendingExpr);
        }

        ast.List<Stmt> retList = new ast.List<>();

        /* --- generate token consumption block --- */
        boolean needTokenConsumptionBlock = false;
        for (Signature signature : this.signatures) {
            if (signature.getType().getSignature().equals("..")) needTokenConsumptionBlock = true;
        }
        if (needTokenConsumptionBlock) {
            Collection<Expr> summingExprs = new HashSet<>();
            for (int iter = 0; iter < this.signatures.size(); iter++) {
                Signature signature = this.signatures.get(iter);
                if (signature.getType().getSignature().equals("..")) {
                    if (iter == 0) {
                        summingExprs.add(posArrayAccessMap.get(iter).treeCopy());
                    } else {
                        MinusExpr appendingExpr = new MinusExpr();
                        appendingExpr.setLHS(posArrayAccessMap.get(iter).treeCopy());
                        appendingExpr.setRHS(posArrayAccessMap.get(iter - 1).treeCopy());
                        summingExprs.add(appendingExpr);
                    }
                } else {
                    summingExprs.add(new IntLiteralExpr(new DecIntNumericLiteralValue("1")));
                }
            }
            /* --- little adjustment on token consumption summing set --- */
            int staticCumCounter = 0;
            Collection<Expr> removingSet = new HashSet<>();
            for (Expr expr : summingExprs) {
                if (expr instanceof IntLiteralExpr) {
                    int value = ((IntLiteralExpr)expr).getValue().getValue().intValue();
                    staticCumCounter = staticCumCounter + value;
                    removingSet.add(expr);
                }
            }
            summingExprs.removeAll(removingSet);
            if (staticCumCounter != 0) {
                summingExprs.add(new IntLiteralExpr(new DecIntNumericLiteralValue(String.valueOf(staticCumCounter))));
            }
            /* ---------------------------------------------------------- */
            Expr sumedExpr = null;
            for (Expr iter : summingExprs) {
                if (sumedExpr == null) {
                    sumedExpr = iter;
                } else {
                    PlusExpr newExpr = new PlusExpr();
                    newExpr.setLHS(sumedExpr.treeCopy());
                    newExpr.setRHS(iter);
                    sumedExpr = newExpr;
                }
            }
            assert sumedExpr != null;

            ParameterizedExpr currentExpr = new ParameterizedExpr();
            currentExpr.setTarget(new NameExpr(new Name("length")));
            currentExpr.addArg(new NameExpr(new Name(this.inPramName)));

            IfBlock tokenConsumptionBlock = new IfBlock();
            tokenConsumptionBlock.setCondition(
                    new NotExpr(new EQExpr(
                            sumedExpr,
                            currentExpr
                    ))
            );
            tokenConsumptionBlock.addStmt(new ContinueStmt());
            retList.add(new IfStmt(new ast.List<IfBlock>(tokenConsumptionBlock), new Opt<ElseBlock>()));
        }

        /* --- generate validation array initialize statement --- */
        AssignStmt validationArrayStmt = new AssignStmt();
        validationArrayStmt.setLHS(new NameExpr(new Name(this.validationArrayName)));
        validationArrayStmt.setRHS(new ParameterizedExpr(
                new NameExpr(new Name("true")),
                new ast.List<Expr>(new IntLiteralExpr(new DecIntNumericLiteralValue(String.valueOf(this.signatures.size()))))
        ));
        validationArrayStmt.setOutputSuppressed(true);
        retList.add(validationArrayStmt);

        /* --- generate validation block --- */
        java.util.function.Function<Expr, Expr> variableCellTokenizeFunc = new java.util.function.Function<Expr, Expr>() {
            @Override
            public Expr apply(Expr expr) {
                ParameterizedExpr getSizeParameterizedExpr = new ParameterizedExpr();
                getSizeParameterizedExpr.setTarget(new NameExpr(new Name("size")));
                getSizeParameterizedExpr.addArg(expr);
                ParameterizedExpr cellized = new ParameterizedExpr();
                cellized.setTarget(new NameExpr(new Name("num2cell")));
                cellized.addArg(getSizeParameterizedExpr);
                return cellized;
            }
        };
        for (int iter = 0; iter < this.signatures.size(); iter++) {
            Signature signature = this.signatures.get(iter);
            if (signature.getType().getSignature().equals("..")) {
                /* for loop style check */
                String iterControlName = this.varNamespace.generateNewName();
                RangeExpr loopRangeExpr = new RangeExpr();
                loopRangeExpr.setLower(
                        (iter == 0) ?
                                new IntLiteralExpr(new DecIntNumericLiteralValue("1")):
                                new PlusExpr(
                                        this.posArrayAccessMap.get(iter - 1).treeCopy(),
                                        new IntLiteralExpr(new DecIntNumericLiteralValue("1"))
                                )
                );
                loopRangeExpr.setUpper(posArrayAccessMap.get(iter));
                AssignStmt iterControlAssignStmt = new AssignStmt();
                iterControlAssignStmt.setLHS(new NameExpr(new Name(iterControlName)));
                iterControlAssignStmt.setRHS(loopRangeExpr);

                ForStmt appendingStmt = new ForStmt();
                appendingStmt.setAssignStmt(iterControlAssignStmt);

                IfBlock checkingBlock = new IfBlock();
                ParameterizedExpr iterateVariable = new ParameterizedExpr();
                iterateVariable.setTarget(new NameExpr(new Name(inPramName)));
                iterateVariable.addArg(new NameExpr(new Name(iterControlName)));

                ParameterizedExpr dimValidationExpr = null;
                ParameterizedExpr typeValidationExpr = null;
                if (signature.getDimension().needValidation()) {
                    assert this.dimValidationFuncMap.get(signature) != null;
                    String validationFuncName = this.dimValidationFuncMap.get(signature).getName().getID();
                    dimValidationExpr = new ParameterizedExpr();
                    dimValidationExpr.setTarget(new NameExpr(new Name(validationFuncName)));
                    dimValidationExpr.addArg(variableCellTokenizeFunc.apply(iterateVariable.treeCopy()));
                }
                if (signature.getType().needValidation()) {
                    typeValidationExpr = new ParameterizedExpr();
                    typeValidationExpr.setTarget(new NameExpr(new Name("isa")));
                    typeValidationExpr.addArg(iterateVariable.treeCopy());
                    typeValidationExpr.addArg(new StringLiteralExpr(signature.getType().getSignature()));
                }
                Expr resultExpr = null;
                if (dimValidationExpr == null) {
                    if (typeValidationExpr == null) {
                        continue;
                    } else {
                        resultExpr = typeValidationExpr;
                    }
                } else {
                    if (typeValidationExpr == null) {
                        resultExpr = dimValidationExpr;
                    } else {
                        resultExpr = new AndExpr();
                        ((AndExpr)resultExpr).setLHS(dimValidationExpr);
                        ((AndExpr)resultExpr).setRHS(typeValidationExpr);
                    }
                }

                checkingBlock.setCondition(resultExpr);
                AssignStmt falseReturnStmt = new AssignStmt();
                falseReturnStmt.setLHS(validationArrayAccessMap.get(iter).treeCopy());
                falseReturnStmt.setRHS(new NameExpr(new Name(this.falseReturn)));
                falseReturnStmt.setOutputSuppressed(true);
                checkingBlock.addStmt(falseReturnStmt);

                appendingStmt.addStmt(new IfStmt(new ast.List<IfBlock>(checkingBlock), new Opt<ElseBlock>()));

                retList.add(appendingStmt);
            } else {
                /* single check */
                ParameterizedExpr dimValidationExpr = null;
                ParameterizedExpr typeValidationExpr = null;
                if (signature.getDimension().needValidation()) {
                    assert this.dimValidationFuncMap.get(signature) != null;
                    String validationFuncName = this.dimValidationFuncMap.get(signature).getName().getID();
                    dimValidationExpr = new ParameterizedExpr();
                    dimValidationExpr.setTarget(new NameExpr(new Name(validationFuncName)));
                    dimValidationExpr.addArg(variableCellTokenizeFunc.apply(posIndexVariableAccessMap.get(iter).treeCopy()));
                }
                if (signature.getType().needValidation()) {
                    String targetType = signature.getType().getSignature();
                    typeValidationExpr = new ParameterizedExpr();
                    typeValidationExpr.setTarget(new NameExpr(new Name("isa")));
                    typeValidationExpr.addArg(posIndexVariableAccessMap.get(iter).treeCopy());
                    typeValidationExpr.addArg(new StringLiteralExpr(targetType));
                }
                Expr resultExpr = null;
                if (dimValidationExpr == null) {
                    if (typeValidationExpr == null) {
                        throw new RuntimeException();
                    } else {
                        resultExpr = typeValidationExpr;
                    }
                } else {
                    if (typeValidationExpr == null) {
                        resultExpr = dimValidationExpr;
                    } else {
                        resultExpr = new AndExpr();
                        ((AndExpr)resultExpr).setLHS(dimValidationExpr);
                        ((AndExpr)resultExpr).setRHS(typeValidationExpr);
                    }
                }
                AssignStmt appendingStmt = new AssignStmt();
                appendingStmt.setLHS(this.validationArrayAccessMap.get(iter).treeCopy());
                appendingStmt.setRHS(resultExpr);
                appendingStmt.setOutputSuppressed(true);
                retList.add(appendingStmt);
            }
        }

        /* --- generate true return stmt --- */
        EQExpr trueRetCondition = new EQExpr();
        trueRetCondition.setLHS(new NameExpr(new Name(this.validationArrayName)));
        trueRetCondition.setRHS(new ParameterizedExpr(
                new NameExpr(new Name("true")),
                new ast.List<Expr>(new IntLiteralExpr(new DecIntNumericLiteralValue(String.valueOf(this.signatures.size()))))
        ));
        AssignStmt trueRetAssign = new AssignStmt();
        trueRetAssign.setLHS(new NameExpr(new Name(outPramName)));
        trueRetAssign.setRHS(new NameExpr(new Name(this.trueReturn)));
        trueRetAssign.setOutputSuppressed(true);
        IfBlock trueRetBlock = new IfBlock();
        trueRetBlock.setCondition(trueRetCondition);
        trueRetBlock.addStmt(trueRetAssign);
        trueRetBlock.addStmt(new ReturnStmt());
        retList.add(new IfStmt(new ast.List<IfBlock>(trueRetBlock), new Opt<ElseBlock>()));

        return retList;
    }

    private ast.List<Stmt> generateWrappedCheckingBlock(ast.List<Stmt> checkingBlock) {
        ast.List<Stmt> retList = new ast.List<>();
        ast.List<Stmt> appendingPos = retList;

        /* --- generate direct length check --- */
        int minArgs = 0;
        for (Signature signature : this.signatures) if (signature.getType().isFixMatch()) minArgs = minArgs + 1;
        if (minArgs != 0) {
            LTExpr boundaryCondition = new LTExpr();
            boundaryCondition.setLHS(new ParameterizedExpr(
                    new NameExpr(new Name("length")),
                    new ast.List<Expr>(new NameExpr(new Name(this.inPramName)))
            ));
            boundaryCondition.setRHS(new IntLiteralExpr(new DecIntNumericLiteralValue(String.valueOf(minArgs))));
            IfBlock checkBlock = new IfBlock();
            checkBlock.setCondition(boundaryCondition);
            AssignStmt falseReturnAssignStmt = new AssignStmt();
            falseReturnAssignStmt.setLHS(new NameExpr(new Name(this.outPramName)));
            falseReturnAssignStmt.setRHS(new NameExpr(new Name(this.falseReturn)));
            falseReturnAssignStmt.setOutputSuppressed(true);
            checkBlock.addStmt(falseReturnAssignStmt);
            checkBlock.addStmt(new ReturnStmt());
            appendingPos.add(new IfStmt(new ast.List<IfBlock>(checkBlock), new Opt<ElseBlock>()));
        }

        /* --- generate wrapper ---- */
        java.util.function.BiFunction<Signature, List<Signature>, Integer> remindingFixPointFunc = new BiFunction<Signature, List<Signature>, Integer>() {
            @Override
            public Integer apply(Signature signature, List<Signature> signatures) {
                int currentPos = -1;
                for (int iter = 0; iter < signatures.size(); iter++) {
                    if (signatures.get(iter) == signature) {
                        currentPos = iter;
                        break;
                    }
                }
                if (currentPos == -1) throw new RuntimeException();
                int returnValue = 0;
                for (int iter = currentPos + 1; iter < signatures.size(); iter++) {
                    if (signatures.get(iter).getType().isFixMatch()) returnValue = returnValue + 1;
                }
                return returnValue;
            }
        };
        for (int iter = 0; iter < signatures.size(); iter++) {
            Signature signature = this.signatures.get(iter);
            if (signature.getType().isFixMatch()) {
                AssignStmt posAssignStmt = new AssignStmt();
                posAssignStmt.setLHS(this.posArrayAccessMap.get(iter).treeCopy());
                posAssignStmt.setRHS(
                        (iter == 0)?
                                new IntLiteralExpr(new DecIntNumericLiteralValue("1")) :
                                new PlusExpr(
                                        this.posArrayAccessMap.get(iter - 1).treeCopy(),
                                        new IntLiteralExpr(new DecIntNumericLiteralValue("1"))
                                )
                );
                posAssignStmt.setOutputSuppressed(true);
                appendingPos.add(posAssignStmt);
            } else {
                int fixpoints = remindingFixPointFunc.apply(signature, this.signatures);
                RangeExpr iterateRangeExpr = new RangeExpr();
                iterateRangeExpr.setLower(
                        (iter == 0)?
                                new IntLiteralExpr(new DecIntNumericLiteralValue("0")) :
                                this.posArrayAccessMap.get(iter - 1).treeCopy()
                );
                iterateRangeExpr.setUpper(
                        (fixpoints == 0)?
                                new ParameterizedExpr(
                                        new NameExpr(new Name("length")),
                                        new ast.List<Expr>(new NameExpr(new Name(this.inPramName)))
                                ) :
                                new MinusExpr(
                                        new ParameterizedExpr(
                                                new NameExpr(new Name("length")),
                                                new ast.List<Expr>(new NameExpr(new Name(this.inPramName)))
                                        ),
                                        new IntLiteralExpr(new DecIntNumericLiteralValue(String.valueOf(fixpoints)))
                                )
                );
                AssignStmt posAssignStmt = new AssignStmt();
                posAssignStmt.setLHS(this.posArrayAccessMap.get(iter).treeCopy());
                posAssignStmt.setRHS(iterateRangeExpr);
                ForStmt appendignFor = new ForStmt();
                appendignFor.setAssignStmt(posAssignStmt);
                appendingPos.add(appendignFor);
                appendingPos = appendignFor.getStmtList();
            }
        }

        /* --- merging --- */
        for (Stmt stmt : checkingBlock) appendingPos.add(stmt);

        /* --- adding false return assignment --- */
        AssignStmt falseReturnAssignStmt = new AssignStmt();
        falseReturnAssignStmt.setLHS(new NameExpr(new Name(this.outPramName)));
        falseReturnAssignStmt.setRHS(new NameExpr(new Name(this.falseReturn)));
        falseReturnAssignStmt.setOutputSuppressed(true);
        retList.add(falseReturnAssignStmt);
        retList.add(new ReturnStmt());

        return retList;
    }

    public Function getFunction() {
        return this.generatedFunction.treeCopy();
    }
}
