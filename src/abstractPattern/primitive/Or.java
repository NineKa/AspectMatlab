package abstractPattern.primitive;

import Matlab.Utils.IReport;
import Matlab.Utils.Message;
import Matlab.Utils.Report;
import abstractPattern.Modifier;
import abstractPattern.Primitive;
import abstractPattern.type.WeaveType;
import ast.ASTNode;
import ast.AndExpr;
import ast.OrExpr;
import transformer.util.IsPossibleJointPointResult;
import transformer.util.RuntimeInfo;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Or extends Primitive{
    private OrExpr astNodes = null;

    private Primitive lhs = null;
    private Primitive rhs = null;

    public void setRHS(Primitive rhs) {
        this.rhs = rhs;
    }

    public void setLHS(Primitive lhs) {
        this.lhs = lhs;
    }

    public void setOrExpr(OrExpr astNodes) {
        this.astNodes = astNodes;
    }

    public Primitive getRHS() {
        return rhs;
    }

    public Primitive getLHS() {
        return lhs;
    }

    @Override
    public ASTNode getASTExpr() {
        return this.astNodes;
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
        String orStr = String.format("(%s | %s)", this.lhs.toString(), this.rhs.toString());
        if (this.isModified()) {
            String appendingStr = "";
            for (int iter = 0; iter < this.getModifiers().size(); iter++) {
                appendingStr = appendingStr + this.getModifiers().get(iter);
                if (iter + 1 < this.getModifiers().size()) appendingStr = appendingStr + " & ";
            }
            return String.format("(%s & %s)", orStr, appendingStr);
        } else {
            return orStr;
        }
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
        Map<WeaveType, Boolean> weaveTypeBooleanWeaveType = new HashMap<>();
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
        weaveTypeBooleanWeaveType.put(WeaveType.Before, lhsMap.get(WeaveType.Before) && rhsMap.get(WeaveType.Before));
        weaveTypeBooleanWeaveType.put(WeaveType.After, lhsMap.get(WeaveType.After) && rhsMap.get(WeaveType.After));
        weaveTypeBooleanWeaveType.put(WeaveType.Around, lhsMap.get(WeaveType.Around) && rhsMap.get(WeaveType.Around));
        return weaveTypeBooleanWeaveType;
    }

    @Override
    public IsPossibleJointPointResult isPossibleJointPoint(ASTNode astNode, RuntimeInfo runtimeInfo) {
        IsPossibleJointPointResult lhsResult = this.lhs.isPossibleJointPoint(astNode, runtimeInfo).clone();
        IsPossibleJointPointResult rhsResult = this.rhs.isPossibleJointPoint(astNode, runtimeInfo).clone();

        return lhsResult.orMerge(rhsResult);
    }
}
