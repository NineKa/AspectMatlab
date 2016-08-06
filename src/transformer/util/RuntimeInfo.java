package transformer.util;

import Matlab.Utils.IReport;
import Matlab.Utils.Report;
import abstractPattern.type.ScopeType;
import ast.*;
import matcher.annotation.AnnotationMatcher;
import natlab.toolkits.analysis.varorfun.VFAnalysis;
import org.javatuples.Pair;
import util.MergeableCollection;
import util.MergeableHashMap;
import util.MergeableHashSet;

import java.util.Collection;
import java.util.Map;
import java.util.Stack;
import java.util.function.Function;

public class RuntimeInfo {
    public VFAnalysis kindAnalysis = null;
    public Stack<Pair<ScopeType, String>> scopeTraceStack = null;
    public Map<Stmt, String> loopNameResolveMap = null;
    public AccessMode accessMode = null;
    public Map<EmptyStmt, HelpComment> annotationMap = null;

    public static Pair<Map<Stmt, String>, IReport> resolveLoopName(ASTNode astNode, String filepath) {
        /* TODO */

        IReport report = new Report();      /* use to collect all messages */

        Collection<HelpComment> allLoopNameAnnotation = (new Function<ASTNode, Collection<HelpComment>>() {
            @Override
            public Collection<HelpComment> apply(ASTNode astNode) {
                MergeableCollection<HelpComment> retCollection = new MergeableHashSet<>();
                if (astNode.hasComments()) {
                    for (int iter = 0; iter < astNode.getComments().size(); iter++) {
                        assert astNode.getChild(iter) instanceof HelpComment;
                        HelpComment helpComment = (HelpComment) astNode.getComments().get(iter);

                        AnnotationMatcher matcher = new AnnotationMatcher(helpComment.getText());
                        if (!matcher.isValid()) continue;
                        if (!matcher.getAbstractAnnotation().getAnnotationName().equals("loopname")) continue;
                        retCollection.add(helpComment);
                    }
                    for (int iter = 0; iter < astNode.getNumChild(); iter++) {
                        Collection<HelpComment> childCollection = this.apply(astNode.getChild(iter));
                        assert childCollection instanceof MergeableCollection;
                        retCollection = retCollection.union((MergeableCollection<HelpComment>) childCollection);
                    }
                }
                return retCollection;
            }
        }).apply(astNode);

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

        return new Pair<>(resolvedMap, report);
    }
}
