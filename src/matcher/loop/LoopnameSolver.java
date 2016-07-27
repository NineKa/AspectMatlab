package matcher.loop;

import ast.*;
import matcher.annotation.AnnotationMatcher;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class LoopnameSolver {
    private boolean isExistBefore(ASTNode node1, ASTNode node2) {
        int node1EndLine = node1.getEndLine();
        int node1EndColumn = node1.getEndColumn();
        int node2StartLine = node2.getStartLine();
        int node2StartColumn = node2.getStartColumn();
        /* condition I : the line node 2 line is after the node 1 line */
        if (node2StartLine > node1EndLine) return true;
        /* condition II : on the same line, but column number is greater */
        if (node1EndLine == node2StartLine && node1EndColumn <= node2StartColumn) return true;
        /* otherwise return false */
        return false;
    }

    private Map<ForStmt, String> resolve(List<Stmt> stmts, Collection<HelpComment> comments) {
        Map<ForStmt, String> retMap = new HashMap<>();
        Collection<HelpComment> deleteCollection = new HashSet<>();
        for (HelpComment comment : comments) {
            AnnotationMatcher matcher = new AnnotationMatcher(comment.getText());
            if (!matcher.isValid()) deleteCollection.add(comment);
        }
        comments.removeAll(deleteCollection);

    }
}
