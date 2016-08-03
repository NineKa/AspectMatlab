package matcher.parameter;

import abstractPattern.signature.Signature;
import ast.Function;
import ast.Name;
import matcher.alphabet.Alphabet;
import matcher.alphabet.TypeAlphabetCompareFunc;
import matcher.dfa.DFA;
import matcher.nfa.NFA;
import matcher.nfa.NFAFactory;
import util.Namespace;

import java.util.List;

public class SimpleMatcher {
    private List<Signature> signatures = null;
    private Namespace funcNamespace = null;
    private Namespace varNamespace = null;

    private Function generatedFunction = null;

    public SimpleMatcher(List<Signature> signatures, Namespace funcNamespace, Namespace varNamespace) {
        this.signatures = signatures;
        this.funcNamespace = funcNamespace;
        this.varNamespace = varNamespace;

        assert canSimpleMatched(this.signatures);

        ast.List<Name> nameList = new ast.List<>();
        Alphabet<String> alphabet = new Alphabet<>();
        for (Signature signature : signatures) {
            nameList.add(new Name(signature.getType().getSignature()));
            if (!signature.getType().isWildcard()) alphabet.add(signature.getType().getSignature());
        }
        Function alphabetFunc = alphabet.generateMatlabFunc(
                new TypeAlphabetCompareFunc(),
                this.funcNamespace,
                this.varNamespace
        );
        NFA nfa = NFAFactory.buildNFAfromType(alphabet, nameList);
        DFA dfa = new DFA(nfa);
        this.generatedFunction = dfa.getMatlabMatchFunction(
                alphabetFunc,
                this.funcNamespace,
                this.varNamespace
        );
        this.generatedFunction.addNestedFunction(alphabetFunc);
    }

    public static boolean canSimpleMatched(List<Signature> signatures) {
        for (Signature signature :  signatures) {
            if (signature.getDimension().needValidation()) return false;
        }
        return true;
    }

    public Function getFunction() {
        return this.generatedFunction;
    }
}
