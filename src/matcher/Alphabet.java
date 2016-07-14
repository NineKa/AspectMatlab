package matcher;

import ast.*;
import ast.List;
import natlab.DecIntNumericLiteralValue;
import util.MergeableCollection;
import util.Namespace;

import java.util.*;

public class Alphabet<T> implements Iterable<T>{
    private int stateCounter = 0;

    private Map<T, Integer> alphabetTable = new HashMap<T, Integer>();

    public void add(T letter) {
        /* ignore duplicate letters in alphabet */
        if (alphabetTable.containsKey(letter)) return;
        stateCounter = stateCounter + 1;
        this.alphabetTable.put(letter, stateCounter);
    }

    public int getEpsilonTransitionCode() { return 0; }

    public int getSigmaTransitionCode() { return this.stateCounter + 1; }

    public int getLetterCode(T letter) {
        if (this.alphabetTable.containsKey(letter)) return this.alphabetTable.get(letter).intValue();
        return this.getSigmaTransitionCode();
    }

    public int size() { return this.stateCounter; }

    public Collection<T> getLetters() { return  this.alphabetTable.keySet(); }

    public Function generateMatlabFunc(
            AbstractAlphabetCompareFunc pComparator,
            Namespace pFuncNamespace,
            Namespace pVarNamespace) {
        Function returnFunction = new Function();

        /* generate Function Name */
        String retFuncName = pFuncNamespace.generateNewName();
        returnFunction.setName(new Name(retFuncName));

        /* generate Function inPram & out Pram */
        String inPramName = pVarNamespace.generateNewName();
        String outPramName = pVarNamespace.generateNewName();
        returnFunction.setInputParamList(new List<>(new Name(inPramName)));
        returnFunction.setOutputParamList(new List<>(new Name(outPramName)));

        /* generate Function body */
        Collection<IfBlock> appendingIfBlocks = new HashSet<>();
        Expr compareIterExpr = new NameExpr(new Name(inPramName));
        for (T compareTargetIter : this) {
            int alphabetCode = this.getLetterCode(compareTargetIter);
            Expr compareExpr = pComparator.getCompareFunc(compareIterExpr.treeCopy(), compareTargetIter);
            AssignStmt returnAssignmentStmt = new AssignStmt();
            returnAssignmentStmt.setLHS(new NameExpr(new Name(outPramName)));
            returnAssignmentStmt.setRHS(new IntLiteralExpr(new DecIntNumericLiteralValue(Integer.toString(alphabetCode))));
            returnAssignmentStmt.setOutputSuppressed(true);

            IfBlock appendingBlock = new IfBlock();
            appendingBlock.setCondition(compareExpr);
            appendingBlock.addStmt(returnAssignmentStmt);
            appendingIfBlocks.add(appendingBlock);
        }
        ElseBlock appendingElseBlock = new ElseBlock();
        AssignStmt returnAssignmentStmt = new AssignStmt();
        returnAssignmentStmt.setLHS(new NameExpr(new Name(outPramName)));
        returnAssignmentStmt.setRHS(new IntLiteralExpr(new DecIntNumericLiteralValue(Integer.toString(this.getSigmaTransitionCode()))));
        returnAssignmentStmt.setOutputSuppressed(true);
        appendingElseBlock.addStmt(returnAssignmentStmt);

        IfStmt appendingIfStmt = new IfStmt();
        for (IfBlock iter : appendingIfBlocks) appendingIfStmt.addIfBlock(iter);
        appendingIfStmt.setElseBlock(appendingElseBlock);

        returnFunction.addStmt(appendingIfStmt);

        return returnFunction;
    }

    public Alphabet<T> merge(Alphabet<T> target) {
        Alphabet<T> retAlphabet = new Alphabet<T>();
        for (T iter : this.alphabetTable.keySet()) {
            retAlphabet.add(iter);
        }
        for (T iter : target.getLetters()) {
            retAlphabet.add(iter);
        }
        return retAlphabet;
    }

    @Override public String toString() { return alphabetTable.toString(); }

    public Iterator<T> iterator() { return this.alphabetTable.keySet().iterator(); }
}
