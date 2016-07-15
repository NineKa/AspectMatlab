package abstractPattern;

import Matlab.Utils.IReport;
import Matlab.Utils.Message;
import Matlab.Utils.Report;
import abstractPattern.types.OperatorType;
import ast.FullSignature;
import ast.PatternOperator;

import java.util.LinkedList;
import java.util.List;

public class Operator implements IValidation {
    private PatternOperator astNodes = null;
    private OperatorType operatorType;
    private List<ArgumentSignature> signatures = new LinkedList<>();

    public Operator(PatternOperator init) {
        this.astNodes = init;
        this.operatorType = OperatorType.fromString(init.getType().getID());
        ast.List<FullSignature> fullSignatures = this.astNodes.getFullSignatures();
        for (FullSignature iter : fullSignatures) {
            this.signatures.add(new ArgumentSignature(iter));
        }
    }

    public List<ArgumentSignature> getSignatures() { return this.signatures; }

    public OperatorType getOperatorType() { return this.operatorType; }

    public boolean isValid() {
        /* recursively check wither its argument is valid */
        for (ArgumentSignature iter : signatures) {
            if (!iter.isValid()) return false;
        }
        /* check if the number of arguments exceeds its maximum */
        if (this.signatures.size() > this.operatorType.getMaximumArgumentNumber()) return false;
        return true;
    }

    public IReport getValidationReport(String pFilePath) {
        Report retReport = new Report();
        /* collect validation report from arumgnets */
        for (ArgumentSignature iter : signatures) {
            IReport appendingReport = iter.getValidationReport(pFilePath);
            for (Message msgIter : appendingReport) {
                retReport.Add(msgIter);
            }
        }
        /* argument number check */
        int maxinumArg = this.operatorType.getMaximumArgumentNumber();
        if (this.signatures.size() > maxinumArg) {
            retReport.AddError(
                    pFilePath,
                    this.astNodes.getStartLine(),
                    this.astNodes.getStartColumn(),
                    String.format(
                            "%s expects %d argument(s), but %d argument(s) found",
                            this.operatorType.toString(),
                            maxinumArg,
                            this.signatures.size()
                    )
            );
        }


        return retReport;
    }

    @Override public String toString() {
        return this.operatorType.toString() + this.signatures.toString();
    }
}
