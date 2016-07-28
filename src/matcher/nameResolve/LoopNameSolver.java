package matcher.nameResolve;

import Matlab.Nodes.CommentNode;
import Matlab.Nodes.FileNode;
import Matlab.Utils.IReport;
import Matlab.Utils.Node;
import Matlab.Utils.Report;
import abstractPattern.type.LoopType;
import ast.*;
import matcher.annotation.AnnotationMatcher;
import org.javatuples.Pair;
import util.MergeableHashSet;

import java.util.*;
import java.util.function.Function;

public class LoopNameSolver {
    private FileNode rawNode = null;
    private Program  astNode = null;

    private Map<Stmt, Pair<LoopType, String>> solveMap = new HashMap<>();
    private Collection<HelpComment> comments = null;
    private IReport report = new Report();

    public LoopNameSolver(Program astNode, FileNode fileNode) {
        this.rawNode = fileNode;
        this.astNode = astNode;

        this.solveMap = initStmtCollection(this.astNode);
        /* initial guess */
        this.comments = collectHelpCommentFromFileNode(this.rawNode);

        /* filter the comment pool */
        Collection<HelpComment> deleteCollection = new HashSet<>();
        for (HelpComment comment : this.comments) {
            /* using annotation parser to decide if it is a annotation*/
            AnnotationMatcher matcher = new AnnotationMatcher(comment.getText());
            if (!matcher.isValid()) {
                /* oops, not an annotation */
                deleteCollection.add(comment);
                continue;   /* move on */
            }

        }
    }

    public Map<Stmt, Pair<LoopType, String>> getSolveMap() {
        return solveMap;
    }

    private static Collection<HelpComment> collectHelpCommentFromFileNode(FileNode fileNode) {
        Collection<HelpComment> helpComments = new HashSet<>();

        Function<Node, Collection<CommentNode>> recWorker = new Function<Node, Collection<CommentNode>>() {
            @Override
            public Collection<CommentNode> apply(Node node) {
                Collection<CommentNode> retCollection = new MergeableHashSet();
                if (node instanceof CommentNode) {
                    retCollection.add((CommentNode)node);
                } else {
                    for (Node iterNode : node.GetChildren()) {
                        Collection<CommentNode> mergingSet = this.apply(iterNode);
                        retCollection.addAll(mergingSet);
                    }
                }
                return retCollection;
            }
        };
        Collection<CommentNode> rawComments = recWorker.apply(fileNode);

        for (CommentNode commentNode : rawComments) {
            if (commentNode.GetText().startsWith("%{")) {
                MultiLineHelpComment multiLineHelpComment = new MultiLineHelpComment();
                multiLineHelpComment.setText(commentNode.GetText());
                multiLineHelpComment.setStartLine(commentNode.GetLine());
                multiLineHelpComment.setStartColumn(commentNode.GetColumn());
                helpComments.add(multiLineHelpComment);
            } else {
                OneLineHelpComment oneLineHelpComment = new OneLineHelpComment();
                oneLineHelpComment.setText(commentNode.GetText());
                oneLineHelpComment.setStartLine(commentNode.GetLine());
                oneLineHelpComment.setStartColumn(commentNode.GetColumn());
                helpComments.add(oneLineHelpComment);
            }
        }
        return helpComments;
    }

    private static Map<Stmt, Pair<LoopType, String>> initStmtCollection(Program program) {
        Function<ASTNode, Map<Stmt, Pair<LoopType, String>>> recWorker = new Function<ASTNode, Map<Stmt, Pair<LoopType, String>>>() {
            @Override
            public Map<Stmt, Pair<LoopType, String>> apply(ASTNode astNode) {
                Map<Stmt, Pair<LoopType, String>> mergeMap = new HashMap<>();
                if (astNode instanceof ForStmt) {
                    Expr controlVariableExpr = ((ForStmt) astNode).getAssignStmt().getLHS();
                    assert controlVariableExpr instanceof NameExpr;
                    String controlVariableName = ((NameExpr) controlVariableExpr).getName().getID();
                    Pair<LoopType, String> loopInfo = new Pair<>(LoopType.For, controlVariableName);
                    mergeMap.put((ForStmt)astNode, loopInfo);
                }
                if (astNode instanceof WhileStmt) {
                    Pair<LoopType, String> loopInfo = new Pair<>(LoopType.While, "");
                    mergeMap.put((WhileStmt)astNode, loopInfo);
                }
                for (int iter = 0; iter < astNode.getNumChild(); iter++) {
                    ASTNode iterNode = astNode.getChild(iter);
                    Map<Stmt, Pair<LoopType, String>> retMap = this.apply(iterNode);
                    mergeMap.putAll(retMap);
                }
                return mergeMap;
            }
        };
        return recWorker.apply(program);
    }
}
