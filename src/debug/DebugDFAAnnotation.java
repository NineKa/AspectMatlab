package debug;

import ast.Expr;
import matcher.annotation.AbstractAnnotation;
import matcher.annotation.AnnotationMatcher;
import matcher.dfa.DFA;
import matcher.nfa.NFA;
import matcher.nfa.NFAFactory;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class DebugDFAAnnotation {
    private static DFA buildDFAFromSignature(List<String> strings) {
        NFA nfa = NFAFactory.buildNFAfromAnnotateSelector(strings);
        return new DFA(nfa);
    }
    private static List<Class<? extends Expr>> getCandidate(String annotation) {
        AnnotationMatcher matcher = new AnnotationMatcher(annotation);
        assert matcher.isValid();
        assert matcher.getAbstractAnnotation().getAnnotationArgs().getNumChild() > 0;
        AbstractAnnotation abstractAnnotation = matcher.getAbstractAnnotation();
        List<Class<? extends Expr>> retList = new LinkedList<>();
        for (Expr expr : abstractAnnotation.getAnnotationArgs().getChild(0)) retList.add(expr.getClass());
        return retList;
    }

    @Test
    public void test1() {
        /* matching for variable */
        DFA dfa = buildDFAFromSignature(Arrays.asList("var"));
        List<Class<? extends Expr>> candidateVar = getCandidate("%@test1 x");
        List<Class<? extends Expr>> candidateInt = getCandidate("%@test1 10");
        List<Class<? extends Expr>> candidateFP  = getCandidate("%@test1 10.0");
        List<Class<? extends Expr>> candidateStr = getCandidate("%@test1 'str'");
        Assert.assertTrue(dfa.validate(candidateVar));
        Assert.assertFalse(dfa.validate(candidateInt));
        Assert.assertFalse(dfa.validate(candidateFP));
        Assert.assertFalse(dfa.validate(candidateStr));
    }

    @Test
    public void test2() {
        /* matching for number literal */
        DFA dfa = buildDFAFromSignature(Arrays.asList("num"));
        List<Class<? extends Expr>> candidateVar = getCandidate("%@test2 x");
        List<Class<? extends Expr>> candidateInt = getCandidate("%@test2 10");
        List<Class<? extends Expr>> candidateFP  = getCandidate("%@test2 10.0");
        List<Class<? extends Expr>> candidateStr = getCandidate("%@test2 'str'");
        Assert.assertFalse(dfa.validate(candidateVar));
        Assert.assertTrue(dfa.validate(candidateInt));
        Assert.assertTrue(dfa.validate(candidateFP));
        Assert.assertFalse(dfa.validate(candidateStr));
    }

    @Test
    public void test3() {
        /* matching for string literal */
        DFA dfa = buildDFAFromSignature(Arrays.asList("str"));
        List<Class<? extends Expr>> candidateVar = getCandidate("%@test3 x");
        List<Class<? extends Expr>> candidateInt = getCandidate("%@test3 10");
        List<Class<? extends Expr>> candidateFP  = getCandidate("%@test3 10.0");
        List<Class<? extends Expr>> candidateStr = getCandidate("%@test3 'str'");
        Assert.assertFalse(dfa.validate(candidateVar));
        Assert.assertFalse(dfa.validate(candidateInt));
        Assert.assertFalse(dfa.validate(candidateFP));
        Assert.assertTrue(dfa.validate(candidateStr));
    }

    @Test
    public void test4() {
        /* matching for wildcard */
        DFA dfa = buildDFAFromSignature(Arrays.asList("*"));
        List<Class<? extends Expr>> candidateVar = getCandidate("%@test4 x");
        List<Class<? extends Expr>> candidateInt = getCandidate("%@test4 10");
        List<Class<? extends Expr>> candidateFP  = getCandidate("%@test4 10.0");
        List<Class<? extends Expr>> candidateStr = getCandidate("%@test4 'str'");
        Assert.assertTrue(dfa.validate(candidateVar));
        Assert.assertTrue(dfa.validate(candidateInt));
        Assert.assertTrue(dfa.validate(candidateFP));
        Assert.assertTrue(dfa.validate(candidateStr));
    }

    @Test
    public void test5() {
        DFA dfa = buildDFAFromSignature(Arrays.asList("var", "..", "num"));
        List<Class<? extends Expr>> candidateValid1   = getCandidate("%@test5 [x, 'str', 10]");
        List<Class<? extends Expr>> candidateValid2   = getCandidate("%@test5 [x, 10.0]");
        List<Class<? extends Expr>> candidateInvalid1 = getCandidate("%@test5 [x, 'str', 'str']");
        List<Class<? extends Expr>> candidateInvalid2 = getCandidate("%@test5 ['x', 'str', 10]");
        Assert.assertTrue(dfa.validate(candidateValid1));
        Assert.assertTrue(dfa.validate(candidateValid2));
        Assert.assertFalse(dfa.validate(candidateInvalid1));
        Assert.assertFalse(dfa.validate(candidateInvalid2));
    }

    @Test
    public void test6() {
        /* matching for empty */
        DFA dfa = buildDFAFromSignature(Arrays.asList());
        List<Class<? extends Expr>> candidateValid   = getCandidate("%@test6 []");
        List<Class<? extends Expr>> candidateInvalid = getCandidate("%@test6 x");
        Assert.assertTrue(dfa.validate(candidateValid));
        Assert.assertFalse(dfa.validate(candidateInvalid));
    }
}
