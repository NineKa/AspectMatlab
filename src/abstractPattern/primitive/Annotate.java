package abstractPattern.primitive;

import Matlab.Utils.IReport;
import Matlab.Utils.Report;
import abstractPattern.Modifier;
import abstractPattern.Primitive;
import abstractPattern.modifier.Dimension;
import abstractPattern.modifier.IsType;
import abstractPattern.modifier.Within;
import abstractPattern.type.WeaveType;
import ast.*;
import matcher.annotation.AnnotationMatcher;
import transformer.IsPossibleJointPointResult;
import transformer.RuntimeInfo;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Annotate extends Primitive {
    private PatternAnnotate astNodes = null;

    private String annotateName = null;
    private List<List<String>> signature = null;

    public Annotate(PatternAnnotate annotate) {
        this.astNodes = annotate;
        /* --- refactor --- */
        assert this.astNodes.getIdentifier() != null;
        if (this.astNodes.getSelectorList() == null) this.astNodes.setSelectorList(new ast.List<>());
        /* ---------------- */
        this.annotateName = this.astNodes.getIdentifier().getID();
        this.signature = new LinkedList<>();
        for (Selector selector : this.astNodes.getSelectorList()) {
            List<String> appendingList = new LinkedList<>();
            for (Name name : selector.getElementList()) {
                appendingList.add(name.getID());
            }
            this.signature.add(appendingList);
        }
    }

    public String getAnnotateName() {
        return annotateName;
    }

    public List<List<String>> getSignature() {
        return signature;
    }

    @Override
    public ASTNode getASTExpr() {
        return this.astNodes;
    }

    @Override
    public boolean isValid() {
        if (this.annotateName.equals("..")) return false;
        if (this.annotateName.equals("loopname")) return false;
        for (List<String> list : this.signature) {
            for (String string : list) {
                if (string.equals("var")) continue;
                if (string.equals("num")) continue;
                if (string.equals("str")) continue;
                if (string.equals("*"))   continue;
                if (string.equals(".."))  continue;
                return false;
            }
        }
        return true;
    }

    @Override
    public IReport getValidationReport(String pFilepath) {
        Report report = new Report();
        for (int listIter = 0; listIter < this.signature.size(); listIter++) {
            List<String> list = this.signature.get(listIter);
            for (int strIter = 0; strIter < list.size(); strIter++) {
                String string = list.get(strIter);
                if (strIter + 1 < list.size()) {
                    String nextToken = list.get(strIter + 1);
                    if (string.equals("..") && nextToken.equals("..")) report.AddWarning(
                            pFilepath,
                            this.astNodes.getSelector(listIter).getElement(strIter).getStartLine(),
                            this.astNodes.getSelector(listIter).getElement(strIter).getStartColumn(),
                            "redundant pattern [.., ..], use [..] instead"
                    );
                }
                if (string.equals("var")) continue;
                if (string.equals("num")) continue;
                if (string.equals("str")) continue;
                if (string.equals("*"))   continue;
                if (string.equals(".."))  continue;
                report.AddError(
                        pFilepath,
                        this.astNodes.getSelector(listIter).getElement(strIter).getStartLine(),
                        this.astNodes.getSelector(listIter).getElement(strIter).getStartColumn(),
                        String.format(
                                "%s is not a valid select in annotation pattern",
                                string
                        )
                );
            }
        }
        if (this.annotateName.equals("..")) {
            report.AddError(
                    pFilepath,
                    this.astNodes.getIdentifier().getStartLine(),
                    this.astNodes.getIdentifier().getStartColumn(),
                    "wildcard [..] is not a valid matcher in annotation pattern for annotation name, use [*] instead"
            );
        }
        if (this.annotateName.equals("loopname")) {
            report.AddError(
                    pFilepath,
                    this.astNodes.getIdentifier().getStartLine(),
                    this.astNodes.getIdentifier().getStartColumn(),
                    "annotation loopname is reserved in AspectMATLAB"
            );
        }

        return report;
    }

    @Override
    public Class<? extends ASTNode> getASTPatternClass() {
        return PatternAnnotate.class;
    }

    @Override
    public String toString() {
        String annotationStr = "";
        for (int listIter = 0; listIter < this.signature.size(); listIter++) {
            List<String> list = this.signature.get(listIter);
            if (list.size() == 1) {
                annotationStr = annotationStr + list.get(0);
            } else {
                String appendingStr = "";
                for (int strIter = 0; strIter < list.size(); strIter++) {
                    String string = list.get(strIter);
                    appendingStr = appendingStr + string;
                    if (strIter + 1 < list.size()) appendingStr = appendingStr + ", ";
                }
                annotationStr = annotationStr + String.format("[%s]", appendingStr);
            }
            if (listIter + 1 < this.signature.size()) annotationStr = annotationStr + ", ";
        }
        if (this.isModified()) {
            String appendingStr = "";
            for (int iter = 0; iter < this.getModifiers().size(); iter++) {
                appendingStr = appendingStr + this.getModifiers().get(iter);
                if (iter + 1 < this.getModifiers().size()) appendingStr = appendingStr + " & ";
            }
            return String.format("(annotate(%s(%s)) & %s)", this.annotateName, annotationStr, appendingStr);
        } else {
            return String.format("annotate(%s(%s))", this.annotateName, annotationStr);
        }
    }

    @Override
    public boolean isProperlyModified() {
        for (Modifier modifier : this.getBadicModifierSet()) {
            if (modifier instanceof IsType)     return false;
            if (modifier instanceof Dimension)  return false;
            if (modifier instanceof Within)     continue;
            /* control flow should not reach here */
            throw new AssertionError();
        }
        return true;
    }

    @Override
    public IReport getModifierValidationReport(String pFilepath) {
        Report report = new Report();

        for (Modifier modifier : this.getBadicModifierSet()) {
            if (modifier instanceof IsType) {
                report.AddError(
                        pFilepath,
                        this.astNodes.getStartLine(),
                        this.astNodes.getStartColumn(),
                        String.format(
                                "cannot apply type pattern (%s@[%d : %d]) to annotation pattern",
                                modifier.toString(),
                                modifier.getASTExpr().getStartLine(),
                                modifier.getASTExpr().getStartColumn()
                        )
                );
                continue;
            }
            if (modifier instanceof Dimension) {
                report.AddError(
                        pFilepath,
                        this.astNodes.getStartLine(),
                        this.astNodes.getStartColumn(),
                        String.format(
                                "cannot apply dimension patern (%s[%d : %d]) to annotation pattern",
                                modifier.toString(),
                                modifier.getASTExpr().getStartLine(),
                                modifier.getASTExpr().getStartColumn()
                        )
                );
                continue;
            }
            if (modifier instanceof Within) continue;
            /* control flow should not reach here */
            throw new AssertionError();
        }
        return report;
    }

    @Override
    public Map<WeaveType, Boolean> getWeaveInfo() {
        Map<WeaveType, Boolean> weaveTypeBooleanMap = new HashMap<>();
        weaveTypeBooleanMap.put(WeaveType.Before, true);
        weaveTypeBooleanMap.put(WeaveType.After, true);
        weaveTypeBooleanMap.put(WeaveType.Around, true);
        return weaveTypeBooleanMap;
    }

    @Override
    public IsPossibleJointPointResult isPossibleJointPoint(ASTNode astNode, RuntimeInfo runtimeInfo) {
        /* TODO */
        /* --- structure check --- */
        if (!(astNode instanceof EmptyStmt)) {
            IsPossibleJointPointResult result = new IsPossibleJointPointResult();
            result.reset();
            return result;
        }
        assert astNode instanceof EmptyStmt;    /* should not fail */
        /* --- retrieve HelpComment --- */
        if (!runtimeInfo.annotationMap.keySet().contains(astNode)) {
            /* such empty comment is not inserted by transformer, ignored */
            IsPossibleJointPointResult result = new IsPossibleJointPointResult();
            result.reset();
            return result;
        }
        HelpComment helpComment = runtimeInfo.annotationMap.get(astNode);
        String annotate = helpComment.getText();
        AnnotationMatcher matcher = new AnnotationMatcher(annotate);
        assert matcher.isValid();   /* comment should be valid as it's in the comment map */


        return super.isPossibleJointPoint(astNode, runtimeInfo);
    }
}
