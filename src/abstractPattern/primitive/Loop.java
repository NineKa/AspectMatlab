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
import ast.*;
import transformer.util.IsPossibleJointPointResult;
import transformer.util.RuntimeInfo;

import java.util.HashMap;
import java.util.Map;

public class Loop extends Primitive{
    private PatternLoop astNodes = null;

    private LoopType loopType = null;
    private String loopName = null;

    public Loop(PatternLoop loop){
        this.astNodes = loop;
        /* --- refactor --- */
        assert this.astNodes.getIdentifier() != null;
        if (this.astNodes.getType() == null) this.astNodes.setType(new Name("*"));
        /* ---------------- */
        this.loopName = this.astNodes.getIdentifier().getID();
        this.loopType = LoopType.fromString(this.astNodes.getType().getID());
    }

    public LoopType getLoopType() {
        return loopType;
    }

    public String getLoopName() {
        return loopName;
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
                    "wildcard [..] is not a valid matcher in loop pattern for loop name, use [*] instead"
            );
        }
        return report;
    }

    @Override
    public Class<? extends ASTNode> getASTPatternClass() {
        return PatternLoop.class;
    }

    @Override
    public String toString() {
        String loopStr = String.format("loop(%s : %s)", this.loopType.toString(), this.loopName);
        if (this.isModified()) {
            String appendingStr = "";
            for (int iter = 0; iter < this.getModifiers().size(); iter++) {
                appendingStr = appendingStr + this.getModifiers().get(iter);
                if (iter + 1 < this.getModifiers().size()) appendingStr = appendingStr + " & ";
            }
            return String.format("(%s & %s)", loopStr, appendingStr);
        } else {
            return loopStr;
        }
    }

    @Override
    public ASTNode getASTExpr() {
        return this.astNodes;
    }

    @Override
    public boolean isProperlyModified() {
        for (Modifier modifier : this.getBadicModifierSet()) {
            if (modifier instanceof Dimension)  return false;
            if (modifier instanceof IsType)     return false;
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
                report.AddError(
                        pFilepath,
                        this.astNodes.getStartLine(),
                        this.astNodes.getStartColumn(),
                        String.format(
                                "cannot apply dimension pattern (%s@[%d : %d]) to loop pattern",
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
                                "cannot apply type pattern (%s@[%d : %d]) to loop pattern",
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
        weaveTypeBooleanMap.put(WeaveType.After, true);
        weaveTypeBooleanMap.put(WeaveType.Around, true);
        return weaveTypeBooleanMap;
    }

    @Override
    public IsPossibleJointPointResult isPossibleJointPoint(ASTNode astNode, RuntimeInfo runtimeInfo) {
        /* structure check */
        boolean isValidStructure = false;
        if (astNode instanceof ForStmt) isValidStructure = true;
        if (astNode instanceof WhileStmt) isValidStructure = true;
        if (!isValidStructure) {
            IsPossibleJointPointResult result = new IsPossibleJointPointResult();
            result.reset();
            return result;
        }
        /* loop name check */
        if (!runtimeInfo.loopNameResolveMap.keySet().contains(astNode)) {
            /* in proper loop resolve map         */
            /* control flow should not reach here */
            throw new AssertionError();
        }
        String actualName = runtimeInfo.loopNameResolveMap.get(astNode);
        if (!this.loopName.equals("*") && !this.loopName.equals(actualName)) {
            /* miss matched name */
            IsPossibleJointPointResult result = new IsPossibleJointPointResult();
            result.reset();
            return result;
        }
        /* loop type check */
        boolean isValidLoopType = false;
        if (this.loopType == LoopType.Any) isValidLoopType = true;
        if (this.loopType == LoopType.For && astNode instanceof ForStmt) isValidLoopType = true;
        if (this.loopType == LoopType.While && astNode instanceof WhileStmt) isValidLoopType = true;
        if (!isValidLoopType) { /* miss matched */
            IsPossibleJointPointResult result = new IsPossibleJointPointResult();
            result.reset();
            return result;
        }

        /* claim such pattern is possibly matched joint point */
        IsPossibleJointPointResult result = new IsPossibleJointPointResult();
        result.reset();
        result.isLoops = true;
        return result;
    }
}
