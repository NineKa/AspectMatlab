package abstractPattern;

import Matlab.Utils.IReport;
import Matlab.Utils.Report;
import abstractPattern.types.LoopType;
import ast.PatternLoopHead;

public class LoopHead implements IValidation{
    private PatternLoopHead astNodes = null;
    private LoopType loopType = null;
    private String loopNameSignature = null;

    public LoopHead(PatternLoopHead init) {
        this.astNodes = init;
        this.loopNameSignature = (init.getIdentifier() == null)? "*" : init.getIdentifier().getID();
        this.loopType = (init.getType() == null)? LoopType.For : LoopType.fromString(init.getType().getID());
    }

    public String getLoopNameSignature() { return this.loopNameSignature; }

    public LoopType getLoopType() { return this.loopType; }

    public boolean isValid() {
        /* determine if the loop name matcher is valid */
        if (this.getLoopNameSignature().equals("..")) return false;

        return true;
    }

    public IReport getValidationReport(String pFilePath) {
        Report retReport = new Report();

        if (this.getLoopNameSignature().equals("..")) {
            retReport.AddError(
                    pFilePath,
                    this.astNodes.getIdentifier().getStartLine(),
                    this.astNodes.getIdentifier().getStartColumn(),
                    "wildcard '..' is not a valid matcher in loop head pattern (use '*' instead)"
            );
        }

        return retReport;
    }
}
