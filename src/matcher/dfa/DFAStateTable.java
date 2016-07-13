package matcher.dfa;

import ast.IntLiteralExpr;
import ast.MatrixExpr;
import ast.Row;
import natlab.DecIntNumericLiteralValue;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class DFAStateTable {
    private int[][] stateArray = null;
    private int[] acceptArray = null;

    public DFAStateTable(DFA dfa) {
        int stateSize = dfa.size() + 1;
        int alphabetSize = dfa.getAlphabet().getSigmaTransitionCode() + 1;

        stateArray = new int[stateSize][alphabetSize];

        for (int iterState = 1; iterState < stateSize; iterState++) {
            for (int iterTransferCode = 1; iterTransferCode < alphabetSize; iterTransferCode++) {
                this.stateArray[iterState][iterTransferCode] =
                        dfa.get(iterState).transfer(iterTransferCode).getStateNumber();
            }
        }

        List<Integer> appendingList = new LinkedList<>();
        for (DFANode iter : dfa.getAcceptStates()) {
            appendingList.add(iter.getStateNumber());
        }
        this.acceptArray = new int[appendingList.size()];
        for (int iter = 0; iter < appendingList.size(); iter++) this.acceptArray[iter] = appendingList.get(iter);
    }

    public String prettyPrintStateArray() {
        String retStr = "";
        for (int iter = 0; iter < stateArray.length; iter++) {
            retStr = retStr + "#" + Integer.toString(iter) + " : " + Arrays.toString(this.stateArray[iter]) + "\n";
        }
        return retStr;
    }

    public String prettyPrintAcceptArray() {
        return Arrays.toString(this.acceptArray);
    }

    public MatrixExpr generateMatlabStateArray() {
        /* ignore the 0 row, and 0 column */
        MatrixExpr retMatrixExpr = new MatrixExpr();
        for (int rowIter = 1; rowIter < stateArray.length; rowIter++) {
            Row appendingRow = new Row();
            for (int colIter = 1; colIter < stateArray[rowIter].length; colIter++) {
                appendingRow.addElement(
                        new IntLiteralExpr(new DecIntNumericLiteralValue(
                                Integer.toString(this.stateArray[rowIter][colIter]))
                        )
                );
            }
            retMatrixExpr.addRow(appendingRow);
        }
        return retMatrixExpr;
    }

    public MatrixExpr generateMatlabAcceptArray() {
        MatrixExpr retMatrixExpr = new MatrixExpr();
        Row singleRow = new Row();
        for (int accpetIter = 0; accpetIter < this.acceptArray.length; accpetIter++) {
            singleRow.addElement(new IntLiteralExpr(
                    new DecIntNumericLiteralValue(Integer.toString(this.acceptArray[accpetIter])))
            );
        }
        retMatrixExpr.addRow(singleRow);
        return retMatrixExpr;
    }
}
