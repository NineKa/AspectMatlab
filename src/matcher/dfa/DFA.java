package matcher.dfa;

import ast.*;
import matcher.Alphabet;
import matcher.nfa.NFA;
import matcher.nfa.NFANode;
import natlab.DecIntNumericLiteralValue;
import util.MergeableCollection;
import util.MergeableHashSet;
import util.Namespace;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

public class DFA implements Iterable<DFANode>{
    private final String genTrueReturn = "true";
    private final String genFalseReturn = "false";

    private int stateCounter = 1;
    private Alphabet alphabet = null;
    private Collection<DFANode> nodes = new HashSet<>();
    private NFA nfa = null;

    private DFANode entryState = null;
    private Collection<DFANode> acceptStates = new HashSet<>();

    private DFANode getNewState() {
        DFANode retNode = new DFANode(this.alphabet, this, stateCounter);
        this.nodes.add(retNode);
        this.stateCounter = stateCounter + 1;
        return retNode;
    }

    @SuppressWarnings("ann-dep") public DFA(NFA pNFA) {
        this.nfa = pNFA;
        this.alphabet = this.nfa.getAlphabet();
        this.entryState = getNewState();
        Collection<NFANode> entryEplisonClosure = this.nfa.getEntryState().getEpsilonClosureSet();
        for (NFANode appendingNode : entryEplisonClosure) {
            this.entryState.addNFANode(appendingNode);
        }
        recBuildDFA(this.entryState);

        for (DFANode iter : this) {
            for (NFANode NFAAcceptIter : this.nfa.getAcceptStates()) {
                if (iter.getNFASubset().contains(NFAAcceptIter)) {
                    if (acceptStates.contains(iter)) continue;
                    acceptStates.add(iter);
                }
            }
        }
    }

    @Deprecated @SuppressWarnings("ann-dep") private void recBuildDFA(DFANode pos) {
        Collection<Integer> searchCodeSet = new HashSet<>();
        for (Object letter : this.alphabet) searchCodeSet.add(this.alphabet.getLetterCode(letter));
        searchCodeSet.add(this.alphabet.getSigmaTransitionCode());
        for (int stateTransferCode : searchCodeSet) {
            MergeableCollection<NFANode> reachableNodes = new MergeableHashSet<>();
            for (NFANode nodeIter : pos.getNFASubset()) {
                MergeableCollection<NFANode> currentReachable = nodeIter.getReachableNodeSet(stateTransferCode);
                reachableNodes = reachableNodes.merge(currentReachable);
            }
            DFANode target = null;
            for (DFANode nodeIter : this.nodes) {
                if (nodeIter.hasSameNFASubset(reachableNodes)) target = nodeIter;
            }
            if (target == null) {
                DFANode newState = this.getNewState();
                newState.addNFANode(reachableNodes);
                pos.addEdge(stateTransferCode, newState);
                recBuildDFA(newState);
            } else {
                pos.addEdge(stateTransferCode, target);
            }
        }
    }

    @Override public String toString() { return this.nodes.toString(); }

    public int size() { return this.stateCounter - 1; }

    public Alphabet getAlphabet() { return this.alphabet; }

    public Iterator<DFANode> iterator() { return this.nodes.iterator(); }

    public Collection<DFANode> getAcceptStates() { return this.acceptStates; }

    public DFANode get(int index) {
        for (DFANode iter : this) {
            if (iter.getStateNumber() == index) return iter;
        }
        throw new IndexOutOfBoundsException();
    }

    public DFAStateTable getStateTable() { return new DFAStateTable(this); }

    public Function getMatlabMatchFunction(
            Function alphabetFunction,
            Namespace funcNamespace,
            Namespace varNamespace) {
        String functionName = funcNamespace.generateNewName();
        String inPramName = varNamespace.generateNewName();
        String outPramName = varNamespace.generateNewName();

        String alphabetFunctionName = alphabetFunction.getName().getID();

        Function retFunction = new Function();
        retFunction.setName(new Name(functionName));
        retFunction.setInputParamList(new List<>(new Name(inPramName)));
        retFunction.setOutputParamList(new List<>(new Name(outPramName)));

        DFAStateTable stateTable = this.getStateTable();

        /* Variable Init */
        String stateTableName = varNamespace.generateNewName();
        AssignStmt stateTableAssignStmt = new AssignStmt();
        stateTableAssignStmt.setLHS(new NameExpr(new Name(stateTableName)));
        stateTableAssignStmt.setRHS(stateTable.generateMatlabStateArray().treeCopy());
        stateTableAssignStmt.setOutputSuppressed(true);
        retFunction.addStmt(stateTableAssignStmt);

        String stateCounterName = varNamespace.generateNewName();
        AssignStmt stateCounterAssigStmt = new AssignStmt();
        stateCounterAssigStmt.setLHS(new NameExpr(new Name(stateCounterName)));
        stateCounterAssigStmt.setRHS(new IntLiteralExpr(new DecIntNumericLiteralValue("1")));
        stateCounterAssigStmt.setOutputSuppressed(true);
        retFunction.addStmt(stateCounterAssigStmt);

        /* state transfer loop */
        ForStmt stateTransferLoop = new ForStmt();
        String stateTransferLoopName = varNamespace.generateNewName();
        RangeExpr stateTransferLoopRange = new RangeExpr();
        stateTransferLoopRange.setLower(new IntLiteralExpr(new DecIntNumericLiteralValue("1")));
        stateTransferLoopRange.setUpper(new ParameterizedExpr(
                new NameExpr(new Name("length")),
                new List<Expr>(new NameExpr(new Name(inPramName)))
        ));
        AssignStmt stateTransferLoopAssign = new AssignStmt();
        stateTransferLoopAssign.setLHS(new NameExpr(new Name(stateTransferLoopName)));
        stateTransferLoopAssign.setRHS(stateTransferLoopRange);
        stateTransferLoop.setAssignStmt(stateTransferLoopAssign);

        retFunction.addStmt(stateTransferLoop);

        AssignStmt stateTransferAssign = new AssignStmt();
        stateTransferAssign.setLHS(new NameExpr(new Name(stateCounterName)));
        stateTransferAssign.setRHS(
                new ParameterizedExpr(
                        new NameExpr(new Name(stateTableName)),
                        new List<Expr>(
                                new NameExpr(new Name(stateCounterName)),
                                new ParameterizedExpr(
                                        new NameExpr(new Name(alphabetFunctionName)),
                                        new List<Expr>(
                                                new CellIndexExpr(
                                                        new NameExpr(new Name(inPramName)),
                                                        new List<Expr>(
                                                                new NameExpr(new Name(stateTransferLoopName))
                                                        )
                                                )
                                        )
                                )
                        )
                )
        );
        stateTransferAssign.setOutputSuppressed(true);
        stateTransferLoop.addStmt(stateTransferAssign);

        /* state verification loop */
        String acceptStatesName = varNamespace.generateNewName();
        AssignStmt acceptStatesAssignStmt = new AssignStmt();
        acceptStatesAssignStmt.setLHS(new NameExpr(new Name(acceptStatesName)));
        acceptStatesAssignStmt.setRHS(stateTable.generateMatlabAcceptArray().treeCopy());
        acceptStatesAssignStmt.setOutputSuppressed(true);
        retFunction.addStmt(acceptStatesAssignStmt);

        ForStmt stateVerificationLoop = new ForStmt();
        String stateVerificationLoopName = varNamespace.generateNewName();
        RangeExpr stateVerificationRange = new RangeExpr();
        stateVerificationRange.setLower(new IntLiteralExpr(new DecIntNumericLiteralValue("1")));
        stateVerificationRange.setUpper(new ParameterizedExpr(
                new NameExpr(new Name("length")),
                new List<Expr>(new NameExpr(new Name(acceptStatesName)))
        ));
        AssignStmt stateVerificationAssign = new AssignStmt();
        stateVerificationAssign.setLHS(new NameExpr(new Name(stateVerificationLoopName)));
        stateVerificationAssign.setRHS(stateVerificationRange);
        stateVerificationLoop.setAssignStmt(stateVerificationAssign);

        EQExpr verificationExpr = new EQExpr();
        verificationExpr.setLHS(new NameExpr(new Name(stateCounterName)));
        verificationExpr.setRHS(new ParameterizedExpr(
                new NameExpr(new Name(acceptStatesName)),
                new List<Expr>(new NameExpr(new Name(stateVerificationLoopName)))
        ));
        IfBlock appendingIfBlock = new IfBlock();
        appendingIfBlock.setCondition(verificationExpr);
        AssignStmt trueReturnAssign = new AssignStmt();
        trueReturnAssign.setLHS(new NameExpr(new Name(outPramName)));
        trueReturnAssign.setRHS(new NameExpr(new Name(this.genTrueReturn)));
        trueReturnAssign.setOutputSuppressed(true);
        appendingIfBlock.addStmt(trueReturnAssign);
        appendingIfBlock.addStmt(new ReturnStmt());
        stateVerificationLoop.addStmt(new IfStmt(new List<IfBlock>(appendingIfBlock), new Opt<ElseBlock>()));

        retFunction.addStmt(stateVerificationLoop);

        /* false return */
        AssignStmt falseReturnAssign = new AssignStmt();
        falseReturnAssign.setLHS(new NameExpr(new Name(outPramName)));
        falseReturnAssign.setRHS(new NameExpr(new Name(this.genFalseReturn)));
        falseReturnAssign.setOutputSuppressed(true);
        retFunction.addStmt(falseReturnAssign);
        retFunction.addStmt(new ReturnStmt());

        return retFunction;
    }
}
