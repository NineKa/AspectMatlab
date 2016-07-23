package abstractPattern.primitive;

import Matlab.Utils.IReport;
import Matlab.Utils.Message;
import Matlab.Utils.Report;
import abstractPattern.Modifier;
import abstractPattern.Primitive;
import abstractPattern.modifier.Dimension;
import abstractPattern.modifier.IsType;
import abstractPattern.modifier.Within;
import abstractPattern.type.WeaveType;
import abstractPattern.utility.Signature;
import ast.ASTNode;
import ast.FullSignature;
import ast.PatternGet;

import java.util.HashMap;
import java.util.Map;

public class Get extends Primitive{
    private PatternGet astNodes = null;

    private String variableName = null;
    private Signature signature = null;

    public Get(PatternGet get) {
        this.astNodes = get;

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
                    "wildcard [..] is not a valid matcher in get pattern for variable name, use [*] instead"
            );
        }
        return report;
    }

    @Override
    public Class<? extends ASTNode> getASTPatternClass() {
        return PatternGet.class;
    }

    @Override
    public String toString() {
        String getStr =  String.format("get(%s : %s)", this.variableName, this.signature.toString());
        if (this.isModified()) {
            String appendingStr = "";
            for (int iter = 0; iter < this.getModifiers().size(); iter++) {
                appendingStr = appendingStr + this.getModifiers().get(iter);
                if (iter + 1 < this.getModifiers().size()) appendingStr = appendingStr + " & ";
            }
            return String.format("(%s & %s)", getStr, appendingStr);
        } else {
            return getStr;
        }
    }

    @Override
    public ASTNode getASTExpr() {
        return this.astNodes;
    }

    @Override
    public boolean isProperlyModified() {
        for (Modifier modifier : this.getBadicModifierSet()) {
            if (modifier instanceof Dimension)  continue;
            if (modifier instanceof IsType)     continue;
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
            if (modifier instanceof Dimension) {
                if (!this.signature.getDimension().needValidation()) continue;
                report.AddWarning(
                        pFilepath,
                        this.astNodes.getStartLine(),
                        this.astNodes.getStartColumn(),
                        String.format(
                                "apply dimension (%s[%d : %d]) pattern to such pattern, may result in no match",
                                modifier.toString(),
                                modifier.getASTExpr().getStartLine(),
                                modifier.getASTExpr().getStartColumn()
                        )
                );
                continue;
            }
            if (modifier instanceof IsType) {
                if (!this.signature.getType().needValidation()) continue;
                report.AddWarning(
                        pFilepath,
                        this.astNodes.getStartLine(),
                        this.astNodes.getStartColumn(),
                        String.format(
                                "apply type (%s[%d : %d]) pattern to such pattern, may result in no match",
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
        weaveTypeBooleanMap.put(WeaveType.After,  true);
        weaveTypeBooleanMap.put(WeaveType.Around, true);
        return weaveTypeBooleanMap;
    }
}
