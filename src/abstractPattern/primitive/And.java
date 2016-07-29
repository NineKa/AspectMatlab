package abstractPattern.primitive;

import Matlab.Utils.IReport;
import Matlab.Utils.Message;
import Matlab.Utils.Report;
import abstractPattern.Modifier;
import abstractPattern.Primitive;
import abstractPattern.type.WeaveType;
import ast.ASTNode;
import ast.AndExpr;
import transformer.RuntimeInfo;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class And extends Primitive{
    private AndExpr astNodes = null;

    private Primitive lhs = null;
    private Primitive rhs = null;

    public void setAndExpr(AndExpr astNodes) {
        this.astNodes = astNodes;
    }

    @Deprecated
    public void setRHS(Primitive rhs) {
        this.rhs = rhs;
    }

    @Deprecated
    public void setLHS(Primitive lhs) {
        this.lhs = lhs;
    }

    public Primitive getRHS() {
        return rhs;
    }

    public Primitive getLHS() {
        return lhs;
    }

    @Override
    public boolean isValid() {
        return this.lhs.isValid() && this.rhs.isValid();
    }

    @Override
    public IReport getValidationReport(String pFilepath) {
        Report report = new Report();
        for (Message message : this.lhs.getValidationReport(pFilepath)) report.Add(message);
        for (Message message : this.rhs.getValidationReport(pFilepath)) report.Add(message);
        return report;
    }

    @Override
    public Class<? extends ASTNode> getASTPatternClass() {
        return AndExpr.class;
    }

    @Override
    public String toString() {
        if (this.isModified()) {
            String appendingStr = "";
            for (int iter = 0; iter < this.getModifiers().size(); iter++) {
                appendingStr = appendingStr + this.getModifiers().get(iter);
                if (iter + 1 < this.getModifiers().size()) appendingStr = appendingStr + " & ";
            }
            return String.format("(%s & %s) & (%s)", this.lhs.toString(), this.rhs.toString(), appendingStr);
        } else {
            return String.format("(%s & %s)", this.lhs.toString(), this.rhs.toString());
        }
    }

    @Override
    public ASTNode getASTExpr() {
        return this.astNodes;
    }

    @Override
    public void addModifier(Modifier modifier) {
        this.lhs.addModifier(modifier);
        this.rhs.addModifier(modifier);
    }

    @Override
    public boolean isProperlyModified() {
        return this.lhs.isProperlyModified() && this.rhs.isProperlyModified();
    }

    @Override
    public IReport getModifierValidationReport(String pFilepath) {
        Report report = new Report();
        for (Message message : this.lhs.getModifierValidationReport(pFilepath)) report.Add(message);
        for (Message message : this.rhs.getModifierValidationReport(pFilepath)) report.Add(message);
        return report;
    }

    @Override
    public Map<WeaveType, Boolean> getWeaveInfo() {
        Map<WeaveType, Boolean> weaveTypeBooleanMap = new HashMap<>();
        Map<WeaveType, Boolean> lhsMap = this.lhs.getWeaveInfo();
        Map<WeaveType, Boolean> rhsMap = this.rhs.getWeaveInfo();
        /* --- assertions --- */
        assert lhsMap.keySet().containsAll(Arrays.asList(
                WeaveType.Before,
                WeaveType.After,
                WeaveType.Around
        ));
        assert rhsMap.keySet().containsAll(Arrays.asList(
                WeaveType.Before,
                WeaveType.After,
                WeaveType.Around
        ));
        /* ------------------ */
        weaveTypeBooleanMap.put(WeaveType.Before, lhsMap.get(WeaveType.Before) && rhsMap.get(WeaveType.Before));
        weaveTypeBooleanMap.put(WeaveType.After, lhsMap.get(WeaveType.After) && rhsMap.get(WeaveType.After));
        weaveTypeBooleanMap.put(WeaveType.Around, lhsMap.get(WeaveType.Around) && rhsMap.get(WeaveType.Around));
        return weaveTypeBooleanMap;
    }

    @Override
    public boolean isPossibleJointPoint(ASTNode astNode, RuntimeInfo runtimeInfo) {
        boolean lhsResult = this.lhs.isPossibleJointPoint(astNode, runtimeInfo);
        boolean rhsResult = this.rhs.isPossibleJointPoint(astNode, runtimeInfo);
        return lhsResult && rhsResult;
    }
}
