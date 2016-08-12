package matcher.parameter.mex;

import matcher.alphabet.Alphabet;
import util.Namespace;

import java.util.LinkedList;
import java.util.List;
import java.util.function.BiFunction;

public class MEXAlphabet {
    private static final String INDENT = "\t";

    private Alphabet alphabet = null;
    private BiFunction<Alphabet, Object, String> codeGenFunc = null;
    private Namespace funcNamespace = null;
    private Namespace varNamespace = null;

    public MEXAlphabet(Alphabet alphabet, BiFunction<Alphabet, Object, String> codeGenFunc,
                       Namespace funcNamespace, Namespace varNamespace) {
        this.alphabet = alphabet;
        this.codeGenFunc = codeGenFunc;
        this.funcNamespace = funcNamespace;
        this.varNamespace = varNamespace;
    }

    public String generate() {
        String functionName = this.funcNamespace.generateNewName();
        List<String> checkEntry = new LinkedList<>();
        for (Object letter : this.alphabet.getLetters()) {
            String entryStmt = this.codeGenFunc.apply(this.alphabet, letter);
            checkEntry.add(entryStmt);
        }
        checkEntry.add(String.format("return %d;", this.alphabet.getSigmaTransitionCode()));

        String funcBody = "";
        for (String entry : checkEntry) {
            funcBody = funcBody + String.format("%s%s\n", INDENT, entry);
        }
        return String.format(
            "int %s("
        );
    }
}
