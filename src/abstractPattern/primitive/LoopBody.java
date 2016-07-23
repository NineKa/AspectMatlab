package abstractPattern.primitive;

import Matlab.Utils.IReport;
import Matlab.Utils.Report;
import abstractPattern.Modifier;
import abstractPattern.Primitive;
import abstractPattern.modifier.Dimension;
import abstractPattern.modifier.IsType;
import abstractPattern.modifier.Within;
import abstractPattern.type.LoopType;
import abstractPattern.type.WeaveType;
import ast.ASTNode;
import ast.Name;
import ast.PatternLoopBody;

import java.util.HashMap;
import java.util.Map;

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
    public boolean isProperlyModified() {
        for (Modifier modifier : this.getBadicModifierSet()) {
            if (modifier instanceof Dimension)      return false;
            if (modifier instanceof IsType)         return false;
            if (modifier instanceof Within)         continue;
            /* control flow should not reach here */
            throw new AssertionError();
        }
        return true;
    }

    @Override
    public IReport getModifierValidationReport(String pFilepath) {
        Report report = new Report();

        for (Modifier modifier : this.getBadicModifierSet()) {
            if (modifier instanceof Dimension) {
                report.AddError(
                        pFilepath,
                        this.astNodes.getStartLine(),
                        this.astNodes.getStartColumn(),
                        String.format(
                                "cannot apply dimension pattern (%s@[%d : %d]) to loop body pattern",
                                modifier.toString(),
                                modifier.getASTExpr().getStartLine(),
                                modifier.getASTExpr().getStartColumn()
                        )
                );
                continue;
            }
            if (modifier instanceof IsType) {
                report.AddError(
                        pFilepath,
                        this.astNodes.getStartLine(),
                        this.astNodes.getStartColumn(),
                        String.format(
                                "cannot apply type pattern (%s@[%d : %d]) to loop body pattern",
                                modifier.toString(),
                                modifier.getASTExpr().getStartLine(),
                                modifier.getASTExpr().getStartColumn()
                        )
                );
                continue;
            }
            if (modifier instanceof Within) {
                continue;
            }
            /* control flow should not reach here */
            throw new AssertionError();
        }
        return report;
    }

    @Override
    public Map<WeaveType, Boolean> getWeaveInfo() {
        Map<WeaveType, Boolean> weaveTypeBooleanMap = new HashMap<>();
        weaveTypeBooleanMap.put(WeaveType.Before, true);
        weaveTypeBooleanMap.put(WeaveType.After,  true);
        weaveTypeBooleanMap.put(WeaveType.Around, true);
        return weaveTypeBooleanMap;
    }
}
