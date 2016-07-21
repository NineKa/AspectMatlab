package abstractPattern.primitive;

import Matlab.Utils.IReport;
import Matlab.Utils.Report;
import abstractPattern.Primitive;
import abstractPattern.type.LoopType;
import ast.ASTNode;
import ast.Name;
import ast.PatternLoopBody;

public class LoopBody extends Primitive{
    private PatternLoopBody astNodes = null;

    private LoopType loopType = null;
    private String loopName = null;

    public LoopBody(PatternLoopBody loopBody) {
        this.astNodes = loopBody;
        /* --- refactor --- */
        assert this.astNodes.getIdentifier() != null;
        if (this.astNodes.getType() == null) this.astNodes.setType(new Name("*"));
        /* ---------------- */
        this.loopName = this.astNodes.getIdentifier().getID();
        this.loopType = LoopType.fromString(this.astNodes.getType().getID());
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
                    "wildcard [..] is not a valid matcher in loopbody pattern for loop name, use [*] instead"
            );
        }
        return report;
    }

    @Override
    public Class<? extends ASTNode> getASTPatternClass() {
        return PatternLoopBody.class;
    }

    @Override
    public String toString() {
        String loopbodyStr = String.format( "loopbody(%s : %s)", this.loopType.toString(), this.loopName);
        if (this.isModified()) {
            String appendingStr = "";
            for (int iter = 0; iter < this.getModifiers().size(); iter++) {
                appendingStr = appendingStr + this.getModifiers().get(iter);
                if (iter + 1 < this.getModifiers().size()) appendingStr = appendingStr + " & ";
            }
            return String.format("(%s & %s)", loopbodyStr, appendingStr);
        } else {
            return loopbodyStr;
        }
    }

    @Override
    public ASTNode getASTExpr() {
        return this.astNodes;
    }

    @Override
    public boolean isProperlyModified() {   // TODO
        return false;
    }

    @Override
    public IReport getModifierValidationReport(String pFilepath) {  // TODO
        return null;
    }
}
