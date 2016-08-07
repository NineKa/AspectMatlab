package transformer.util;

import Matlab.Utils.IReport;
import Matlab.Utils.Report;
import abstractPattern.type.ScopeType;
import ast.*;
import ast.List;
import matcher.annotation.AbstractAnnotation;
import matcher.annotation.AnnotationMatcher;
import natlab.toolkits.analysis.varorfun.VFAnalysis;
import org.javatuples.Pair;
import util.MergeableCollection;
import util.MergeableHashMap;
import util.MergeableHashSet;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public class RuntimeInfo {
    public VFAnalysis kindAnalysis = null;
    public Stack<Pair<ScopeType, String>> scopeTraceStack = null;
    public Map<Stmt, String> loopNameResolveMap = null;
    public AccessMode accessMode = null;
    public Map<EmptyStmt, HelpComment> annotationMap = null;

    public static Pair<Map<Stmt, String>, IReport> resolveLoopName(ASTNode astNode, String filepath) {
        IReport report = new Report();      /* use to collect all messages */

        Collection<HelpComment> allLoopNameAnnotation = (new Function<ASTNode, Collection<HelpComment>>() {
            @Override
            public Collection<HelpComment> apply(ASTNode astNode) {
                MergeableCollection<HelpComment> retCollection = new MergeableHashSet<>();
                if (astNode.hasComments()) {
                    for (int iter = 0; iter < astNode.getComments().size(); iter++) {
                        assert astNode.getComments().get(iter) instanceof HelpComment;
                        HelpComment helpComment = (HelpComment) astNode.getComments().get(iter);

                        AnnotationMatcher matcher = new AnnotationMatcher(helpComment.getText());
                        if (!matcher.isValid()) continue;
                        if (!matcher.getAbstractAnnotation().getAnnotationName().equals("loopname")) continue;
                        retCollection.add(helpComment);
                    }
                }
                for (int iter = 0; iter < astNode.getNumChild(); iter++) {
                    Collection<HelpComment> childCollection = this.apply(astNode.getChild(iter));
                    assert childCollection instanceof MergeableCollection;
                    retCollection = retCollection.union((MergeableCollection<HelpComment>) childCollection);
                }
                return retCollection;
            }
        }).apply(astNode);

        /* weeding on annotations */
        Collection<HelpComment> deleteSet = new HashSet<>();
        for (HelpComment helpComment : allLoopNameAnnotation) {
            AnnotationMatcher matcher = new AnnotationMatcher(helpComment.getText());
            assert matcher.isValid();
            AbstractAnnotation annotation = matcher.getAbstractAnnotation();
            assert annotation.getAnnotationName().equals("loopname");
            if (annotation.getAnnotationArgs().getNumChild() == 0) {
                report.AddWarning(
                        filepath,helpComment.getStartLine(), helpComment.getStartColumn(),
                        "loopname annotation need to provide exactly one identifier, such annotation is ignored"
                );
                deleteSet.add(helpComment);
                continue;
            }
            if (annotation.getAnnotationArgs().getNumChild() != 1) {
                report.AddWarning(
                        filepath,helpComment.getStartLine(), helpComment.getStartColumn(),
                        "such loopname annotation provide more than one name, selecting the first name"
                );
            }
            if (annotation.getAnnotationArgs().getChild(0).getNumChild() != 1) {
                report.AddWarning(
                        filepath,helpComment.getStartLine(), helpComment.getStartColumn(),
                        "an array arugment provided, but a name argument is expected, such annotation is ignored"
                );
                deleteSet.add(helpComment);
                continue;
            }
            if (!(annotation.getAnnotationArgs().getChild(0).getChild(0) instanceof NameExpr)) {
                report.AddWarning(
                        filepath,helpComment.getStartLine(), helpComment.getStartColumn(),
                        "expecting a identifier to resolve the loopname, such annotation is ignored"
                );
                deleteSet.add(helpComment);
                continue;
            }
        }
        allLoopNameAnnotation.removeAll(deleteSet);

        Collection<HelpComment> resolvedAnnotations = new HashSet<>();

        Map<Stmt, String> resolvedMap = (new Function<ASTNode, Map<Stmt, String>>(){
            /* this function provide an initial guess to the loop name resolve */
            /* For Statement   - the initial guess will be based on the name of control variable */
            /* While Statement - the initial guess will be a empty string                        */
            @Override
            public Map<Stmt, String> apply(ASTNode astNode) {
                MergeableHashMap<Stmt, String> retMap = new MergeableHashMap<Stmt, String>();
                if (astNode instanceof ForStmt) {
                    assert ((ForStmt) astNode).getAssignStmt().getLHS() instanceof NameExpr;
                    Name loopControlVar = ((NameExpr) ((ForStmt) astNode).getAssignStmt().getLHS()).getName();
                    retMap.put((ForStmt) astNode, loopControlVar.getID());
                }
                if (astNode instanceof WhileStmt) {
                    retMap.put((WhileStmt) astNode, "");
                }
                for (int iter = 0; iter < astNode.getNumChild(); iter++) {
                    Map<Stmt, String> childMap = this.apply(astNode.getChild(iter));
                    assert childMap instanceof MergeableHashMap;
                    retMap = retMap.union((MergeableHashMap<Stmt, String>) childMap);
                }
                return retMap;
            }
        }).apply(astNode);

        Collection<Pair<Stmt, String>> modifyPendingSet = new HashSet<>();

        for (Stmt stmt : resolvedMap.keySet()) {
            BiFunction<HelpComment, Stmt, Boolean> belongToSameRoot = (HelpComment comment, Stmt statement) -> {
                ASTNode root = null;
                if (statement.getParent() instanceof List) {
                    root = statement.getParent().getParent();
                } else {
                    root = statement.getParent();
                }
                assert root != null;
                return root.getComments().contains(comment);
            };
            BiFunction<HelpComment, Stmt, Boolean> inProperPosition = (HelpComment comment, Stmt statement) -> {
                int previousStmtPos = Integer.MIN_VALUE;
                int currentStmtPos = statement.GetRelativeChildIndex();
                int commentPos = comment.GetRelativeChildIndex();

                int currentStmtIndex = Integer.MIN_VALUE;
                for (int iter = 0; iter < statement.getParent().getNumChild(); iter++)
                    if (statement.getParent().getChild(iter) == statement) currentStmtIndex = iter;
                assert currentStmtIndex != Integer.MIN_VALUE;

                previousStmtPos = (currentStmtIndex != 0)?
                        statement.getParent().getChild(currentStmtIndex - 1).GetRelativeChildIndex():
                        previousStmtPos;
                currentStmtPos = statement.GetRelativeChildIndex();

                return previousStmtPos <= commentPos && commentPos <= currentStmtPos;
            };

            Collection<HelpComment> workspace = new HashSet<>();
            for (HelpComment helpComment : allLoopNameAnnotation) {
                if (belongToSameRoot.apply(helpComment, stmt) && inProperPosition.apply(helpComment, stmt)) {
                    workspace.add(helpComment);
                }
            }

            if (workspace.isEmpty()) continue;  /* no loopname annotation applies */
            HelpComment selectedComment = workspace.iterator().next();
            if (workspace.size() != 1) {        /* multiple loopname annotations applies */
                for (HelpComment comment : workspace) {
                    report.AddWarning(
                            filepath, comment.getStartLine(), comment.getStartColumn(),
                            "multiple loopname annotation applies, using the latest one"
                    );
                }
            }
            for (HelpComment comment : workspace) {
                if (comment.GetRelativeChildIndex() > selectedComment.GetRelativeChildIndex()) {
                    selectedComment = comment;
                }
            }
            AnnotationMatcher matcher = new AnnotationMatcher(selectedComment.getText());
            assert matcher.isValid();
            assert matcher.getAbstractAnnotation().getAnnotationName().equals("loopname");
            assert matcher.getAbstractAnnotation().getAnnotationArgs().getNumChild() >= 1;
            assert matcher.getAbstractAnnotation().getAnnotationArgs().getChild(0).getNumChild() >= 1;
            assert matcher.getAbstractAnnotation().getAnnotationArgs().getChild(0).getChild(0) instanceof NameExpr;
            NameExpr idExpr = (NameExpr) matcher.getAbstractAnnotation().getAnnotationArgs().getChild(0).getChild(0);
            String resolvedName = idExpr.getName().getID();

            resolvedAnnotations.add(selectedComment);
            modifyPendingSet.add(new Pair<>(stmt, resolvedName));
        }

        /* apply modification */
        for (Pair<Stmt, String> resolvedPair : modifyPendingSet) {
            assert resolvedMap.keySet().contains(resolvedPair.getValue0());
            resolvedMap.put(resolvedPair.getValue0(), resolvedPair.getValue1());
        }

        for (HelpComment comment : allLoopNameAnnotation) {
            if (!resolvedAnnotations.contains(comment)) {
                report.AddWarning(
                        filepath, comment.getStartLine(), comment.getStartColumn(),
                        String.format("unresolved loopname annotation (%s)", comment.getText())
                );
            }
        }

        return new Pair<>(resolvedMap, report);
    }

    public static void insertAnnotationEmptyStmt(ASTNode astNode) {  /* using in-place insertion */
        /* TODO */
        /* build up the delegates */
        Map<Class<? extends ASTNode>, Consumer<ASTNode>> handlerMap = new HashMap<>();

        /* collect all valid annotation at such scope */
        java.util.List<Pair<HelpComment, AbstractAnnotation>> validAnnotation = new LinkedList<>();
        for (Object symbol : astNode.getComments()) {
            assert symbol instanceof HelpComment;
            HelpComment comment = (HelpComment) symbol; /* promise by the parser */
            AnnotationMatcher matcher = new AnnotationMatcher(comment.getText());
            if (!matcher.isValid()) continue;
            System.out.println(comment.getText());
            AbstractAnnotation annotation = matcher.getAbstractAnnotation();
            if (annotation.getAnnotationName().equals("loopname")) continue;    /* reserved annotation, ignored */
            validAnnotation.add(new Pair<>(comment, annotation));
        }
        validAnnotation.sort((Pair<HelpComment, AbstractAnnotation> p1, Pair<HelpComment, AbstractAnnotation> p2) -> {
            int p1RelativeIndex = p1.getValue0().GetRelativeChildIndex();
            int p2RelativeIndex = p2.getValue0().GetRelativeChildIndex();
            if (p1RelativeIndex == p2RelativeIndex) assert  p1.equals(p2);      /* fulfill java implementation */
            return p1RelativeIndex - p2RelativeIndex;
        });

        /* */

        /* recursive */
        for (int iter = 0; iter < astNode.getNumChild(); iter++) {
            ASTNode child = astNode.getChild(iter);
            insertAnnotationEmptyStmt(child);
        }
    }
}
