package abstractPattern;

import Matlab.Utils.IReport;
import Matlab.Utils.Message;
import Matlab.Utils.Report;
import ast.FullSignature;
import ast.PatternOperator;

import java.util.LinkedList;
import java.util.List;

public class Operator implements IValidation {
    public enum OperatorType {
        Plus    (2),       /* "+"  */
        Minus   (2),       /* "-"  */
        mTimes  (2),       /* "*"  */
        Times   (2),       /* ".*" */
        mrDivide(2),       /* "/"  */
        rDivide (2),       /* "./" */
        mlDivide(2),       /* "\"  */
        lDivide (2),       /* ".\" */
        mPower  (2),       /* "^"  */
        Power   (2),       /* ".^" */
        Transpose(1)       /* ".'"  */;

        private int maxArgs = 0;

        OperatorType (int pMaxArgs) { this.maxArgs = pMaxArgs; }
        public int getMaximumArgumentNumber() { return this.maxArgs; }

        public static OperatorType fromString(String typeString) {
            if (typeString.equals("+"))   return OperatorType.Plus;
            if (typeString.equals("-"))   return OperatorType.Minus;
            if (typeString.equals("*"))   return OperatorType.mTimes;
            if (typeString.equals(".*"))  return OperatorType.Times;
            if (typeString.equals("/"))   return OperatorType.mrDivide;
            if (typeString.equals("./"))  return OperatorType.rDivide;
            if (typeString.equals("\\"))  return OperatorType.mlDivide;
            if (typeString.equals(".\\")) return OperatorType.lDivide;
            if (typeString.equals("^"))   return OperatorType.mPower;
            if (typeString.equals(".^"))  return OperatorType.Power;
            if (typeString.equals(".'"))   return OperatorType.Transpose;
            throw new RuntimeException();
        }

        public boolean isUniaryOperator() { return this.maxArgs == 1; }

        @Override public String toString() {
            switch (this) {
                case Plus:     return "Plus(*)";
                case Minus:    return "Minus(-)";
                case mTimes:   return "Matrix Multiplication(*)";
                case Times:    return "Multiplication(.*)";
                case mrDivide: return "Matrix Right Division(/)";
                case rDivide:  return "Right Division(./)";
                case mlDivide: return "Matrix Left Division(\\)";
                case lDivide:  return "Left Division(.\\)";
                case mPower:   return "Matrix Power(^)";
                case Power:    return "Power(.^)";
                case Transpose:return "Transpose(.')";
                default:       throw new RuntimeException();
            }
        }
    }

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
