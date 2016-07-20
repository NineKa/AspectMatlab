package abstractPattern.primitive;

import Matlab.Utils.IReport;
import Matlab.Utils.Report;
import abstractPattern.Primitive;
import abstractPattern.type.LoopType;
import ast.ASTNode;
import ast.Name;
import ast.PatternLoopHead;

public class LoopHead extends Primitive{
    private PatternLoopHead astNodes = null;

    private LoopType loopType = null;
    private String loopName = null;

    public LoopHead(PatternLoopHead loopHead) {
        this.astNodes = loopHead;
        /* --- refactor --- */
        assert this.astNodes.getIdentifier() != null;
        if (this.astNodes.getType() == null) this.astNodes.setType(new Name("*"));
        /* ---------------- */
        this.loopName = this.astNodes.getIdentifier().getID();
        this.loopType = LoopType.fromString(this.astNodes.getType().getID());
    }

    public LoopType getLoopType() {
        return loopType;
    }

    public String getLoopName() {
        return loopName;
    }

    @Override
    public boolean isValid() {
        if (this.loopName.equals("..")) return false;
        return true;
    }

    @Override
    public IReport getValidationReport(String pFilepath) {
        Report report = new Report();
        if (this.loopName.equals("..")) {
            report.AddError(
                    pFilepath,
                    this.astNodes.getIdentifier().getStartLine(),
                    this.astNodes.getIdentifier().getStartColumn(),
                    "wildcard [..] is not a valid matcher in loophead pattern for loop name, use [*] instead"
            );
        }
        return report;
    }

    @Override
    public Class<? extends ASTNode> getASTPatternClass() {
        return PatternLoopHead.class;
    }

    @Override
    public String toString() {
        String loopheadStr = String.format("loophead(%s : %s)", this.loopType.toString(), this.loopName);
        if (this.isModified()) {
            String appendingStr = "";
            for (int iter = 0; iter < this.getModifiers().size(); iter++) {
                appendingStr = appendingStr + this.getModifiers().get(iter);
                if (iter + 1 < this.getModifiers().size()) appendingStr = appendingStr + " & ";
            }
            return String.format("(%s & %s)", loopheadStr, appendingStr);
        } else {
            return loopheadStr;
        }
    }
}
