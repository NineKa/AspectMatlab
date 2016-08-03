package matcher.parameter;

import abstractPattern.signature.Signature;
import ast.Function;
import util.Namespace;

import java.util.List;

public class AutoMatcher {
    private Function generatedFunction = null;

    public AutoMatcher(List<Signature> signatures, Namespace funcNamespace, Namespace varNamespace) {
        if (EmptyMatcher.canEmptyMatched(signatures)) {
            EmptyMatcher matcher = new EmptyMatcher(signatures, funcNamespace, varNamespace);
            this.generatedFunction = matcher.getFunction();
        } else if (SimpleMatcher.canSimpleMatched(signatures)) {
            SimpleMatcher matcher = new SimpleMatcher(signatures, funcNamespace, varNamespace);
            this.generatedFunction = matcher.getFunction();
        } else {
            FullMatcher matcher = new FullMatcher(signatures, funcNamespace, varNamespace);
            this.generatedFunction = matcher.getFunction();
        }
    }

    public Function getFunction() {
        return this.generatedFunction;
    }
}
