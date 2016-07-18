package abstractPattern.utility;

import Matlab.Utils.IReport;
import Matlab.Utils.Report;
import abstractPattern.Pattern;
import ast.ASTNode;
import ast.DimensionSignature;
import matcher.alphabet.Alphabet;
import matcher.dfa.DFA;
import matcher.nfa.NFA;
import matcher.nfa.NFAFactory;

import java.util.Arrays;
import java.util.Iterator;

public class SignatureDimension extends Pattern implements Iterable<String>{
    private DimensionSignature astNode = null;
    private String[] signatureDimension = null;

    private Alphabet<Integer> alphabet = new Alphabet<>();

    public SignatureDimension(DimensionSignature signature) {
        this.astNode = signature;
        this.signatureDimension = new String[signature.getNumDimension()];
        for (int iter = 0; iter < signature.getNumDimension(); iter++) {
            this.signatureDimension[iter] = signature.getDimension(iter).getID();
        }
        for (String iter : signatureDimension) {
            if (iter.equals("..")) continue;
            if (iter.equals("*")) continue;
            alphabet.add(Integer.parseInt(iter));
        }
    }

    public DFA generateDFA() {
        NFA nfa = NFAFactory.buildNFAfromDimension(this.alphabet, this.astNode.getDimensionList());
        DFA dfa = new DFA(nfa);
        return dfa;
    }

    public DFA generateDFA(Alphabet<Integer> alphabet) {
        NFA nfa = NFAFactory.buildNFAfromDimension(alphabet, this.astNode.getDimensionList());
        DFA dfa = new DFA(nfa);
        return dfa;
    }

    public Alphabet<Integer> generateAlphabet() {
        return this.alphabet;
    }

    public boolean needValidation() {
        for (String iter : this.signatureDimension) if (!iter.equals("..")) return true;
        return false;
    }

    public String[] getSignature() {
        return this.signatureDimension;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public IReport getValidationReport(String pFilepath) {
        Report retReport = new Report();
        // Add warning to pattern [.., ..]
        for (int iter = 0; iter < this.signatureDimension.length - 1; iter++) {
            if (signatureDimension[iter].equals("..") && signatureDimension[iter + 1].equals("..")) {
                retReport.AddWarning(
                        pFilepath,
                        this.astNode.getDimension(iter).getStartLine(),
                        this.astNode.getDimension(iter).getStartColumn(),
                        "redundant pattern [.., ..], use [..] instead"
                );
            }
        }
        return retReport;
    }

    @Override
    public Class<? extends ASTNode> getASTPatternClass() {
        return DimensionSignature.class;
    }

    @Override
    public Iterator<String> iterator() {
        return Arrays.asList(this.signatureDimension).iterator();
    }

    @Override
    public String toString() {
        return Arrays.toString(this.signatureDimension);
    }
}
