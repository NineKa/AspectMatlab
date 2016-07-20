package abstractPattern.primitive;

import Matlab.Utils.IReport;
import Matlab.Utils.Message;
import Matlab.Utils.Report;
import abstractPattern.Primitive;
import abstractPattern.utility.Signature;
import ast.ASTNode;
import ast.FullSignature;
import ast.PatternSet;

public class Set extends Primitive{
    private PatternSet astNodes = null;

    private String variableName = null;
    private Signature signature = null;

    public Set(PatternSet set) {
        this.astNodes = set;
        /* --- refactor --- */
        assert this.astNodes.getIdentifier() != null;
        if (this.astNodes.getFullSignature() == null) this.astNodes.setFullSignature(new FullSignature());
        /* ---------------- */
        this.variableName = this.astNodes.getIdentifier().getID();
        this.signature = new Signature(this.astNodes.getFullSignature());
    }

    public String getVariableName() {
        return variableName;
    }

    public Signature getSignature() {
        return signature;
    }

    @Override
    public boolean isValid() {
        if (this.variableName.equals("..")) return false;
        if (!this.signature.isValid()) return false;
        return true;
    }

    @Override
    public IReport getValidationReport(String pFilepath) {
        Report report = new Report();
        for (Message message : this.signature.getValidationReport(pFilepath)) report.Add(message);
        if (this.variableName.equals("..")) {
            report.AddError(
                    pFilepath,
                    this.astNodes.getIdentifier().getStartLine(),
                    this.astNodes.getIdentifier().getStartColumn(),
                    "wildcard [..] is not a valid matcher in set pattern for variable name, use [*] instead"
            );
        }
        return report;
    }

    @Override
    public Class<? extends ASTNode> getASTPatternClass() {
        return PatternSet.class;
    }

    @Override
    public String toString() {
        return String.format(
                "set(%s : %s)",
                this.variableName,
                this.signature.toString()
        );
    }
}
