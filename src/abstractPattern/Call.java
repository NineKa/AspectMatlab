package abstractPattern;

import Matlab.Utils.IReport;
import Matlab.Utils.Message;
import Matlab.Utils.Report;
import ast.*;
import matcher.Alphabet;
import matcher.DimAlphabetCompareFunc;
import matcher.TypeAlphabetCompareFunc;
import matcher.dfa.DFA;
import matcher.dfa.DFANode;
import matcher.nfa.NFA;
import matcher.nfa.NFAFactory;
import natlab.DecIntNumericLiteralValue;
import util.Namespace;

import java.util.*;
import java.util.List;

public class Call implements IValidation{
    private final String trueReturn = "true";
    private final String falseReturn = "false";

    private List<ArgumentSignature> inputPramList = new LinkedList();
    private List<ArgumentSignature> outputPramList = new LinkedList<>();
    private String functionName = "";
    private PatternCall astNodes = null;

    private Alphabet<Integer> dimAlphabet = null;
    private Alphabet<String> typeAlphabet = null;

    public Call(PatternCall init) {
        /* initialize astNodes */
        this.astNodes = init.treeCopy();
        /* initialize funcitonName */
        assert (init.getIdentifier() != null);
        this.functionName = init.getIdentifier().getID();
        /* initialize inputPramList */
        ast.List<FullSignature> inputRawList = init.getInput().getFullSignatureList().treeCopy();
        for (int iter = 0; iter < inputRawList.getNumChild(); iter++) {
            ArgumentSignature appendingAbstractToken = new ArgumentSignature(inputRawList.getChild(iter));
            this.inputPramList.add(appendingAbstractToken);
        }
        /* initialize outputPramList */
        ast.List<FullSignature> outputRawList = init.getOutput().getFullSignatureList().treeCopy();
        for (int iter = 0; iter < outputRawList.getNumChild(); iter++) {
            ArgumentSignature appendingAbstractToken = new ArgumentSignature(outputRawList.getChild(iter));
            this.outputPramList.add(appendingAbstractToken);
        }
        /* initialize dimAlphabet */
        this.dimAlphabet = new Alphabet<>();
        for (ArgumentSignature iter : inputPramList) {
            ast.List<Name> dimSignList = iter.getDimensionSignature();
            for (Name nameIter : dimSignList) {
                /* ignore wildcards when construct alphabet */
                if (nameIter.getID().equals("..") || nameIter.getID().equals("*")) continue;
                this.dimAlphabet.add(Integer.parseInt(nameIter.getID()));
            }
        }
        for (ArgumentSignature iter : outputPramList) {
            ast.List<Name> dimSignList = iter.getDimensionSignature();
            for (Name nameIter : dimSignList) {
                /* ignore wildcards when construct alphabet */
                if (nameIter.getID().equals("..") || nameIter.getID().equals("*")) continue;
                this.dimAlphabet.add(Integer.parseInt(nameIter.getID()));
            }
        }
        /* initialize typeAlphabet */
        this.typeAlphabet = new Alphabet<>();
        for (ArgumentSignature iter : inputPramList) {
            if (iter.getTypeSignature().getID().equals("..") || iter.getTypeSignature().getID().equals("*")) continue;
            this.typeAlphabet.add(iter.getTypeSignature().getID());
        }
        for (ArgumentSignature iter : outputPramList) {
            if (iter.getTypeSignature().getID().equals("..") || iter.getTypeSignature().getID().equals("*")) continue;
            this.typeAlphabet.add(iter.getTypeSignature().getID());
        }
    }

    public String getFunctionName() {
        return this.functionName;
    }

    public List<ArgumentSignature> getInputPramList() {
        return this.inputPramList;
    }

    public List<ArgumentSignature> getOutputPramList() {
        return this.outputPramList;
    }

    public boolean isValid() {
        boolean inputPramValid = true;
        for (ArgumentSignature iter : this.inputPramList) inputPramValid = inputPramValid && iter.isValid();
        boolean outputPramValid = true;
        for (ArgumentSignature iter : this.outputPramList) outputPramValid = outputPramValid && iter.isValid();
        /* reject call pattern - function name using ".." wildcard */
        boolean nameValid = !this.getFunctionName().equals("..");
        return nameValid && inputPramValid && outputPramValid;
    }

    public IReport getValidationReport(String pFilePath) {
        IReport retReport = new Report();
        /* recursively analysis the input parameters */
        for (ArgumentSignature iter : this.getInputPramList()) {
            IReport mergingReport = iter.getValidationReport(pFilePath);
            for (Message megIter : mergingReport) {
                retReport.Add(megIter);
            }
        }
        /* recursively analysis the output parameters */
        for (ArgumentSignature iter : this.getOutputPramList()) {
            IReport mergingReport = iter.getValidationReport(pFilePath);
            for (Message megIter : mergingReport) {
                retReport.Add(megIter);
            }
        }
        /* check type patterns */
        List<String> inTypeList = new LinkedList<>();
        List<String> outTypeList = new LinkedList<>();

        for (ArgumentSignature iter : this.getInputPramList()) inTypeList.add(iter.getTypeSignature().getID());
        for (ArgumentSignature iter : this.getOutputPramList()) outTypeList.add(iter.getTypeSignature().getID());

        for (int iter = 0; iter < inTypeList.size() - 1; iter++) {
            if (inTypeList.get(iter).equals("..") && inTypeList.get(iter + 1).equals("..")) {
                retReport.AddWarning(
                        pFilePath,
                        astNodes.getInput().getFullSignature(iter).getStartLine(),
                        astNodes.getInput().getFullSignature(iter).getStartColumn(),
                        "redundant pattern [.., ..], use [..] instead"
                );
            }
        }
        for (int iter = 0; iter < outTypeList.size() - 1; iter++) {
            if (outTypeList.get(iter).equals("..") && outTypeList.get(iter).equals("..")) {
                retReport.AddWarning(
                        pFilePath,
                        astNodes.getOutput().getFullSignature(iter).getStartLine(),
                        astNodes.getOutput().getFullSignature(iter).getStartColumn(),
                        "redundant pattern [.., ..], use [..] instead"
                );
            }
        }

        /* validation on function name */
        if (this.getFunctionName().equals("..")) {
            retReport.AddError(
                    pFilePath,
                    astNodes.getStartLine(),
                    astNodes.getStartColumn(),
                    "Invalid Call Pattern - .. is not a valid matcher on function name"
            );
        }

        return retReport;
    }

    public Alphabet<Integer> generateDimAlphabet() {
        return this.dimAlphabet;
    }

    public Alphabet<String> generateTypeAlphabet() {
        return this.typeAlphabet;
    }

    public java.util.List<NFA> generateInputDimNFAList() {
        java.util.List<NFA> retList = new LinkedList<>();
        for (ArgumentSignature iter : this.getInputPramList()) {
            NFA appendingNFA = NFAFactory.buildNFAfromDimension(this.dimAlphabet, iter.getDimensionSignature());
            retList.add(appendingNFA);
        }
        return retList;
    }

    public java.util.List<NFA> generateOutputDimNFAList() {
        java.util.List<NFA> retList = new LinkedList<>();
        for (ArgumentSignature iter : this.getOutputPramList()) {
            NFA appendingNFA = NFAFactory.buildNFAfromDimension(this.dimAlphabet, iter.getDimensionSignature());
            retList.add(appendingNFA);
        }
        return retList;
    }

    @Override public String toString() {
        return String.format(
                "%s(%s):%s",
                this.getFunctionName(),
                this.getInputPramList().toString(),
                this.getOutputPramList().toString()
        );
    }

    @Override public boolean equals(Object target) {
        if (!(target instanceof Call)) return false;
        Call castedTarget = (Call) target;
        if (!this.getFunctionName().equals(castedTarget.getFunctionName())) return false;
        List<ArgumentSignature> currentInPattern = this.getInputPramList();
        List<ArgumentSignature> targetInPattern = castedTarget.getInputPramList();
        if (currentInPattern.size() != targetInPattern.size()) return false;
        for (int iter = 0; iter < currentInPattern.size(); iter++) {
            if (!currentInPattern.get(iter).equals(targetInPattern.get(iter))) return false;
        }
        List<ArgumentSignature> currentOutPattern = this.getOutputPramList();
        List<ArgumentSignature> targetOutPattern = castedTarget.getOutputPramList();
        if (currentOutPattern.size() != targetOutPattern.size()) return false;
        for (int iter = 0; iter < currentOutPattern.size(); iter++) {
            if (!currentOutPattern.get(iter).equals(targetOutPattern.get(iter))) return false;
        }
        return true;
    }

    @SuppressWarnings("dep-ann") public Function generateInputMatlabMatcher(Namespace funcNamespace, Namespace varNamespace) {
        if (this.inputPramList.isEmpty()) return this.generateMatlabMatcherEmpty(funcNamespace, varNamespace);
        if (this.canGenerateMatlabMatcherSimple(this.getInputPramList()))
            return this.generateMatlabMatcherSimple(this.getInputPramList(), funcNamespace, varNamespace);
        return this.generateMatlabMatcherFull(this.getInputPramList(), funcNamespace, varNamespace);
    }

    @SuppressWarnings("dep-ann") public Function generateOutputMatlabMatcher(Namespace funcNamespace, Namespace varNamespace) {
        if (this.outputPramList.isEmpty()) return this.generateMatlabMatcherEmpty(funcNamespace, varNamespace);
        if (this.canGenerateMatlabMatcherSimple(this.getOutputPramList()))
            return this.generateMatlabMatcherSimple(this.getOutputPramList(), funcNamespace, varNamespace);
        return this.generateMatlabMatcherFull(this.getOutputPramList(), funcNamespace, varNamespace);
    }

    @Deprecated private Function generateMatlabMatcherSimple(
            List<ArgumentSignature> signatures,
            Namespace funcNamespace,
            Namespace varNamespace
    ) {
        Alphabet<String> alphabet = this.generateTypeAlphabet();
        ast.List<Name> types = new ast.List<>();
        for (ArgumentSignature iter : signatures) types.add(iter.getTypeSignature());
        NFA patternNFA = NFAFactory.buildNFAfromType(alphabet, types);
        DFA patternDFA = new DFA(patternNFA);
        Function alphabetFunc = alphabet.generateMatlabFunc(new TypeAlphabetCompareFunc(), funcNamespace, varNamespace);
        Function returnFunc = patternDFA.getMatlabMatchFunction(alphabetFunc, funcNamespace, varNamespace);
        returnFunc.addNestedFunction(alphabetFunc);
        return returnFunc;
    }

    private static int getFixCardNum(List<ArgumentSignature> signatures, int index) {
        int counter = 0;
        for (int iter = index + 1; iter < signatures.size(); iter++) {
            if (signatures.get(iter).getTypeSignature().getID().equals("..")) counter = counter + 1;
        }
        return counter;
    }

    @Deprecated private boolean canGenerateMatlabMatcherSimple(List<ArgumentSignature> signatures) {
        boolean noDimInfo = true;
        for (ArgumentSignature iter : signatures) if (iter.needDimensionValidation()) noDimInfo = false;
        return noDimInfo;
    }

    @Deprecated private Function generateMatlabMatcherFull(
            List<ArgumentSignature> signatures,
            Namespace funcNamespace,
            Namespace varNamespace
    ) {
        // prepare return function
        String retFunctionName = funcNamespace.generateNewName();
        String inPramName = varNamespace.generateNewName();
        String outPramName = varNamespace.generateNewName();

        Function returnFunc = new Function();
        returnFunc.setName(new Name(retFunctionName));
        returnFunc.setInputParamList(new ast.List<>(new Name(inPramName)));
        returnFunc.setOutputParamList(new ast.List<>(new Name(outPramName)));

        Alphabet<Integer> dimAlphabet = this.generateDimAlphabet();
        Function alphatbetFunction = dimAlphabet.generateMatlabFunc(
                new DimAlphabetCompareFunc(),
                funcNamespace,
                varNamespace
        );
        Map<ArgumentSignature, Function> dimVerificationFunctions = new HashMap<>();
        for (ArgumentSignature iter : signatures) {
            if (!iter.needDimensionValidation()) continue;
            NFA argNFA = NFAFactory.buildNFAfromDimension(dimAlphabet, iter.getDimensionSignature());
            DFA argDFA = new DFA(argNFA);
            dimVerificationFunctions.put(iter, argDFA.getMatlabMatchFunction(alphatbetFunction, funcNamespace, varNamespace));
        }

        /* adding nested functions */
        returnFunc.addNestedFunction(alphatbetFunction);
        for (ArgumentSignature iter : dimVerificationFunctions.keySet())
            returnFunc.addNestedFunction(dimVerificationFunctions.get(iter));

        String tokenPosName[] = new String[signatures.size()];
        for (int iter = 0; iter < tokenPosName.length; iter++) {
            tokenPosName[iter] = varNamespace.generateNewName();
        }

        /* --- Check Block --- */
        List<Stmt> checkingStmts = new LinkedList<>();

        /* generate token consumed expr */
        Collection<Expr> summingExprs = new HashSet<>();
        for (int iter = 0; iter < signatures.size(); iter++) {
            ArgumentSignature currentArg = signatures.get(iter);
            if (currentArg.getTypeSignature().getID().equals("..")) {
                if (iter == 0) {
                    summingExprs.add(new NameExpr(new Name(tokenPosName[iter])));
                } else {
                    MinusExpr appendingExpr = new MinusExpr();
                    appendingExpr.setLHS(new NameExpr(new Name(tokenPosName[iter])));
                    appendingExpr.setRHS(new NameExpr(new Name(tokenPosName[iter - 1])));
                    summingExprs.add(appendingExpr);
                }
            } else if (currentArg.getTypeSignature().getID().equals("*")){
                summingExprs.add(new IntLiteralExpr(new DecIntNumericLiteralValue("1")));
            } else {
                summingExprs.add(new IntLiteralExpr(new DecIntNumericLiteralValue("1")));
            }
        }
        Expr consumedTokenExpr = null;
        for (Expr iter : summingExprs) {
            if (consumedTokenExpr == null) {
                consumedTokenExpr = iter;
            } else {
                consumedTokenExpr = new PlusExpr(
                        consumedTokenExpr.treeCopy(),
                        iter
                );
            }
        }
        IfBlock checkTokenIfBlock = new IfBlock();
        checkTokenIfBlock.setCondition(new NotExpr(new EQExpr(
                consumedTokenExpr,
                new ParameterizedExpr(
                        new NameExpr(new Name("length")),
                        new ast.List<Expr>(new NameExpr(new Name(inPramName)))
                )
        )));
        checkTokenIfBlock.addStmt(new ContinueStmt());
        /* if the length check is need or not */
        boolean isLengthCheckNeeded = false;
        for (ArgumentSignature iter : signatures)
            if (iter.getTypeSignature().getID().equals("..")) isLengthCheckNeeded = true;

        if (isLengthCheckNeeded) {
            checkingStmts.add(new IfStmt(new ast.List<>(checkTokenIfBlock), new Opt<ElseBlock>()));
        }

        /* prepare check matrix */
        String checkMatrixName = varNamespace.generateNewName();
        Map<Integer, Expr> checkMatrixAccessMap = new HashMap<>();
        for (int iter = 0; iter < signatures.size(); iter++) {
            ParameterizedExpr appendingExpr = new ParameterizedExpr();
            appendingExpr.setTarget(new NameExpr(new Name(checkMatrixName)));
            appendingExpr.setArgList(new ast.List<>(
                    new IntLiteralExpr(new DecIntNumericLiteralValue(Integer.toString(iter + 1)))
            ));
            checkMatrixAccessMap.put(iter, appendingExpr);
        }
        AssignStmt checkMatrixAssign = new AssignStmt();
        checkMatrixAssign.setLHS(new NameExpr(new Name(checkMatrixName)));
        checkMatrixAssign.setRHS(new ParameterizedExpr(
                new NameExpr(new Name("true")),
                new ast.List<Expr>(
                        new IntLiteralExpr(new DecIntNumericLiteralValue(Integer.toString(signatures.size())))
                )
        ));
        checkMatrixAssign.setOutputSuppressed(true);
        checkingStmts.add(checkMatrixAssign);

        /* generate checking statements */
        /* preparing variable access map */
        Map<Integer, Expr> variableAccessMap = new HashMap<>();
        for (int iter = 0; iter < signatures.size(); iter++) {
            variableAccessMap.put(iter, new CellIndexExpr(
                    new NameExpr(new Name(inPramName)),
                    new ast.List<Expr>(new NameExpr(new Name(tokenPosName[iter])))
            ));
        }

        for (int signatureIndex = 0; signatureIndex < signatures.size(); signatureIndex++) {
            ArgumentSignature signature = signatures.get(signatureIndex);
            Expr varExpr = variableAccessMap.get(signatureIndex);
            if (signature.getTypeSignature().getID().equals("..")) {
                /* for style checking */
                assert(!signature.needTypeValidation());
                if (!signature.needDimensionValidation()) continue;
                RangeExpr rangeExpr = new RangeExpr();
                rangeExpr.setLower(
                        new PlusExpr(
                                new NameExpr(new Name(tokenPosName[signatureIndex])),
                                new IntLiteralExpr(new DecIntNumericLiteralValue("1"))
                        )
                );
                if (signatureIndex + 1 < signatures.size()) {
                    rangeExpr.setUpper(new NameExpr(new Name(tokenPosName[signatureIndex + 1])));
                } else {
                    rangeExpr.setUpper(new ParameterizedExpr(
                                    new NameExpr(new Name("length")),
                                    new ast.List<Expr>(new NameExpr(new Name(inPramName)))
                    ));
                }
                String tempIteraterName = varNamespace.generateNewName();
                AssignStmt forAssignStmt = new AssignStmt();
                forAssignStmt.setLHS(new NameExpr(new Name(tempIteraterName)));
                forAssignStmt.setRHS(rangeExpr);

                ForStmt checkingLoop = new ForStmt();
                checkingLoop.setAssignStmt(forAssignStmt);

                String dimVerifyFuncName = dimVerificationFunctions.get(signature).getName().getID();
                AssignStmt innerAssignStmt = new AssignStmt();
                innerAssignStmt.setLHS(checkMatrixAccessMap.get(signatureIndex).treeCopy());
                innerAssignStmt.setRHS(new AndExpr(
                        checkMatrixAccessMap.get(signatureIndex).treeCopy(),
                        new ParameterizedExpr(
                                new NameExpr(new Name(dimVerifyFuncName)),
                                new ast.List<Expr>(new ParameterizedExpr(
                                        new NameExpr(new Name("num2cell")),
                                        new ast.List<Expr>(new ParameterizedExpr(
                                                new NameExpr(new Name("size")),
                                                new ast.List<Expr>(new CellIndexExpr(
                                                        new NameExpr(new Name(inPramName)),
                                                        new ast.List<Expr>(new NameExpr(new Name(tempIteraterName)))
                                                ))
                                        ))
                                ))
                        )
                ));
                innerAssignStmt.setOutputSuppressed(true);
                checkingLoop.addStmt(innerAssignStmt);

                checkingStmts.add(checkingLoop);
            } else {
                /* single line checking */
                Expr dimExpr = null;
                Expr typeExpr = null;
                if (signature.needDimensionValidation()) {
                    String dimFuncName = dimVerificationFunctions.get(signature).getName().getID();
                    dimExpr = new ParameterizedExpr(
                            new NameExpr(new Name(dimFuncName)),
                            new ast.List<>(new ParameterizedExpr(
                                    new NameExpr(new Name("num2cell")),
                                    new ast.List<>(new ParameterizedExpr(
                                            new NameExpr(new Name("size")),
                                            new ast.List<>(varExpr.treeCopy())
                                    ))
                            ))
                    );
                }
                if (signature.needTypeValidation()) {
                    typeExpr = new ParameterizedExpr(
                            new NameExpr(new Name("isa")),
                            new ast.List<>(
                                    varExpr.treeCopy(),
                                    new StringLiteralExpr(signature.getTypeSignature().getID())
                            )
                    );
                }
                Expr RHSExpr = null;
                if (dimExpr == null && typeExpr == null) continue;
                if (dimExpr != null && typeExpr == null) RHSExpr = dimExpr;
                if (dimExpr == null && typeExpr != null) RHSExpr = typeExpr;
                if (dimExpr != null && typeExpr != null) RHSExpr = new AndExpr(dimExpr, typeExpr);

                AssignStmt appendignAssign = new AssignStmt();
                appendignAssign.setLHS(checkMatrixAccessMap.get(signatureIndex).treeCopy());
                appendignAssign.setRHS(RHSExpr);
                appendignAssign.setOutputSuppressed(true);
                checkingStmts.add(appendignAssign);
            }
        }

        IfBlock trueReturnBlock = new IfBlock();
        trueReturnBlock.setCondition(new EQExpr(
                new NameExpr(new Name(checkMatrixName)),
                new ParameterizedExpr(
                        new NameExpr(new Name("true")),
                        new ast.List<Expr>(new IntLiteralExpr(new DecIntNumericLiteralValue(Integer.toString(signatures.size()))))
                )
        ));
        AssignStmt trueReturnAssign = new AssignStmt();
        trueReturnAssign.setLHS(new NameExpr(new Name(outPramName)));
        trueReturnAssign.setRHS(new NameExpr(new Name(this.trueReturn)));
        trueReturnAssign.setOutputSuppressed(true);
        trueReturnBlock.addStmt(trueReturnAssign);
        trueReturnBlock.addStmt(new ReturnStmt());

        checkingStmts.add(new IfStmt(new ast.List<IfBlock>(trueReturnBlock), new Opt<ElseBlock>()));

        /* --- Check Block --- Complete */
        /* --- Pos Control Loop --- */
        ast.List<Stmt> mergePos = returnFunc.getStmtList();
        for (int signatureIndex = 0; signatureIndex < signatures.size(); signatureIndex++) {
            ArgumentSignature signature = signatures.get(signatureIndex);
            if (signature.getTypeSignature().getID().equals("..")) {
                RangeExpr searchRange = new RangeExpr();
                if (signatureIndex == 0) {
                    searchRange.setLower(new IntLiteralExpr(new DecIntNumericLiteralValue("1")));
                } else {
                    searchRange.setLower(new NameExpr(new Name(tokenPosName[signatureIndex - 1])));
                }
                searchRange.setUpper(new MinusExpr(
                        new ParameterizedExpr(
                                new NameExpr(new Name("length")),
                                new ast.List<Expr>(new NameExpr(new Name(inPramName)))
                        ),
                        new IntLiteralExpr(new DecIntNumericLiteralValue(Integer.toString(getFixCardNum(signatures, signatureIndex))))
                ));
                AssignStmt loopAssignStmt = new AssignStmt();
                loopAssignStmt.setLHS(new NameExpr(new Name(tokenPosName[signatureIndex])));
                loopAssignStmt.setRHS(searchRange);
                ForStmt appendingFor = new ForStmt();
                appendingFor.setAssignStmt(loopAssignStmt);
                mergePos.add(appendingFor);
                mergePos = appendingFor.getStmtList();
            } else {
                if (signatureIndex == 0) {
                    AssignStmt initAssign = new AssignStmt();
                    initAssign.setLHS(new NameExpr(new Name(tokenPosName[signatureIndex])));
                    initAssign.setRHS(new IntLiteralExpr(new DecIntNumericLiteralValue("1")));
                    initAssign.setOutputSuppressed(true);
                    mergePos.add(initAssign);
                } else {
                    AssignStmt initAssign = new AssignStmt();
                    initAssign.setLHS(new NameExpr(new Name(tokenPosName[signatureIndex])));
                    initAssign.setRHS(new PlusExpr(
                            new NameExpr(new Name(tokenPosName[signatureIndex - 1])),
                            new IntLiteralExpr(new DecIntNumericLiteralValue("1"))
                    ));
                    initAssign.setOutputSuppressed(true);
                    mergePos.add(initAssign);
                }
            }
        }

        /* inject */
        for (Stmt iter : checkingStmts) mergePos.add(iter);

        /* default - false return */
        AssignStmt falseReturnAssign = new AssignStmt();
        falseReturnAssign.setLHS(new NameExpr(new Name(outPramName)));
        falseReturnAssign.setRHS(new NameExpr(new Name(this.falseReturn)));
        falseReturnAssign.setOutputSuppressed(true);
        returnFunc.addStmt(falseReturnAssign);
        returnFunc.addStmt(new ReturnStmt());

        return returnFunc;
    }

    @Deprecated private Function generateMatlabMatcherEmpty(Namespace funcNamespace, Namespace varNamespace) {
        String retFunctionName = funcNamespace.generateNewName();
        String inPramName = varNamespace.generateNewName();
        String outPramName = varNamespace.generateNewName();

        Function retFunction = new Function();
        retFunction.setName(new Name(retFunctionName));
        retFunction.setInputParamList(new ast.List<>(new Name(inPramName)));
        retFunction.setOutputParamList(new ast.List<>(new Name(outPramName)));

        IfBlock emptyCheckBlock = new IfBlock();
        emptyCheckBlock.setCondition(new ParameterizedExpr(
                new NameExpr(new Name("isempty")),
                new ast.List<Expr>(new NameExpr(new Name(inPramName)))
        ));

        AssignStmt trueReturnAssign = new AssignStmt();
        trueReturnAssign.setLHS(new NameExpr(new Name(outPramName)));
        trueReturnAssign.setRHS(new NameExpr(new Name(this.trueReturn)));
        trueReturnAssign.setOutputSuppressed(true);

        AssignStmt falseReturnAssign = new AssignStmt();
        falseReturnAssign.setLHS(new NameExpr(new Name(outPramName)));
        falseReturnAssign.setRHS(new NameExpr(new Name(this.falseReturn)));
        falseReturnAssign.setOutputSuppressed(true);

        emptyCheckBlock.addStmt(trueReturnAssign);

        ElseBlock elseBlock = new ElseBlock();
        elseBlock.addStmt(falseReturnAssign);

        retFunction.addStmt(new IfStmt(new ast.List<IfBlock>(emptyCheckBlock), new Opt<ElseBlock>(elseBlock)));

        return retFunction;
    }
}
