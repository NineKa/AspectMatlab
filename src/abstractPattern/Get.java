package abstractPattern;

import Matlab.Utils.IReport;
import Matlab.Utils.Message;
import Matlab.Utils.Report;
import ast.ASTNode;
import ast.FullSignature;
import ast.PatternGet;

public class Get implements IPattern {
    private PatternGet astNodes = null;
    private ArgumentSignature signature = null;
    private String variableName = null;

    public Get(PatternGet init) {
        this.astNodes = init;
        if (init.getFullSignature() == null) {
            /* we need to construct our onw signature - using *[..] as default*/
            FullSignature signature = new FullSignature();
            signature.setStartColumn(this.astNodes.getStartColumn());
            signature.setStartLine(this.astNodes.getStartLine());
            this.signature = new ArgumentSignature(signature);
        } else {
            this.signature = new ArgumentSignature(init.getFullSignature());
        }
        this.variableName = init.getIdentifier().getID();
    }

    public String getVariableName() { return this.variableName; }

    public ArgumentSignature getSignature() { return this.signature; }

    public boolean isValid() {
        /* validate signature argument */
        if (!this.signature.isValid()) return false;
        /* validate variable name is valid */
        if (this.variableName.equals("..")) return false;

        return true;
    }

    public IReport getValidationReport(String pFilePath) {
        IReport retReport = new Report();
        /* recursively merge the argument analysis report */
        for (Message iter : this.signature.getValidationReport(pFilePath)) {
            retReport.Add(iter);
        }
        /* check if variable name is valid */
        if (this.variableName.equals("..")) {
            retReport.AddError(
                    pFilePath,
                    this.astNodes.getStartLine(),
                    this.astNodes.getStartColumn(),
                    "wildcard '..' is not a valid name matcher in get pattern (use '*' instead)"
            );
        }

        return retReport;
    }

    public Class<? extends ASTNode> getASTNodeClass() { return PatternGet.class; }

    @Override public String toString() {
        return String.format(
                "Get(%s,%s)",
                this.getVariableName(),
                this.getSignature().toString()
        );
    }
}
