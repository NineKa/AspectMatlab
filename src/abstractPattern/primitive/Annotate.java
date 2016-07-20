package abstractPattern.primitive;

import Matlab.Utils.IReport;
import Matlab.Utils.Report;
import abstractPattern.Primitive;
import ast.ASTNode;
import ast.Name;
import ast.PatternAnnotate;
import ast.Selector;

import java.util.LinkedList;
import java.util.List;

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
    public boolean isValid() {
        if (this.annotateName.equals("..")) return false;
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
        return String.format(
                "annotate(%s(%s))",
                this.annotateName,
                annotationStr
        );
    }
}