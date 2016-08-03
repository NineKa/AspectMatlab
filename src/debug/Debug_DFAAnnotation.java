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

public class Debug_DFAAnnotation {
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
        List<Class<? extends Expr>> candidate = getCandidate("%@test 10");
        Assert.assertTrue(dfa.validate(candidate));
    }

    @Test
    public void test3() {
        NFA nfa = NFAFactory.buildNFAfromAnnotateSelector(Arrays.asList("num"));
        DFA dfa = new DFA(nfa);
        AnnotationMatcher matcher = new AnnotationMatcher("%@test3 10");
        AbstractAnnotation annotation = matcher.getAbstractAnnotation();
        List<Class<? extends Expr>> candidate = new LinkedList<>();
        for (Expr expr : annotation.getAnnotationArgs().getChild(0)) candidate.add(expr.getClass());
        Assert.assertTrue(dfa.validate(candidate));
    }

    @Test
    public void test4() {
        NFA nfa = NFAFactory.buildNFAfromAnnotateSelector(Arrays.asList("str"));
        DFA dfa = new DFA(nfa);
        AnnotationMatcher matcher = new AnnotationMatcher("%@test4 'str'");
        AbstractAnnotation annotation = matcher.getAbstractAnnotation();
        List<Class<? extends Expr>> candidate = new LinkedList<>();
        for (Expr expr : annotation.getAnnotationArgs().getChild(0)) candidate.add(expr.getClass());
        Assert.assertTrue(dfa.validate(candidate));
    }
}
