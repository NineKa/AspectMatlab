package abstractPattern;

import Matlab.Utils.IReport;
import Matlab.Utils.Message;
import Matlab.Utils.Report;
import abstractPattern.analysis.Analysis;
import abstractPattern.analysis.Backtrace;
import abstractPattern.analysis.PatternType;
import abstractPattern.type.WeaveType;
import ast.Expr;
import ast.List;
import ast.Patterns;
import ast.Stmt;

import java.util.HashMap;
import java.util.Map;

public class Action {
    private Pattern pattern = null;
    private ast.Action astNodes = null;
    private Map<String, Expr> preDefPatternMap = null;
    private String filepath = null;

    private WeaveType weaveType = null;
    private String actionName = null;
    private ast.List<Stmt> actionBody = null;

    private IReport report = new Report();

    public Action(ast.Action pPattern, Map<String, Expr> pMap, String pFilepath) {
        assert pPattern != null;
        assert pMap != null;
        this.astNodes = pPattern;
        this.preDefPatternMap = pMap;
        this.filepath = pFilepath;

        this.actionName = this.astNodes.getName();
        this.weaveType = weaveType.fromString(this.astNodes.getType());

        try {
            /* pattern type analysis */
            Analysis analysis = new Analysis(this.filepath, this.astNodes.getExpr());
            if (analysis.getResult() != PatternType.Primitive) {
                report.AddError(
                        this.filepath,
                        this.astNodes.getStartLine(),
                        this.astNodes.getStartColumn(),
                        "unable to weave such pattern, a primitive pattern expected"
                );
                return;
            }
            AbstractBuilder builder = new AbstractBuilder(this.filepath, this.astNodes.getExpr(), this.preDefPatternMap);
            for (Message message : builder.getReport()) this.report.Add(message);
            if (!builder.getReport().GetIsOk()) return;
            /* pattern weeding */
            IReport validationReport = builder.getPattern().getValidationReport(this.filepath);
            for (Message message : validationReport) this.report.Add(message);
            if (!validationReport.GetIsOk()) return;
            /* modifier analysis */
            assert builder.getPattern() instanceof Primitive;
            IReport modifierReport = ((Primitive) builder.getPattern()).getModifierValidationReport(this.filepath);
            for (Message message : modifierReport) this.report.Add(message);
            if (!modifierReport.GetIsOk()) return;
            /* reformat modifier */
            assert builder.getPattern() instanceof Primitive;
            ((Primitive) builder.getPattern()).reformatModifier();
            /* weave type analysis */
            Map<WeaveType, Boolean> weaveTypeReprot = ((Primitive) builder.getPattern()).getWeaveInfo();
            if (!weaveTypeReprot.get(this.weaveType)) {
                report.AddError(
                        this.filepath,
                        this.astNodes.getExpr().getStartLine(),
                        this.astNodes.getExpr().getStartColumn(),
                        String.format(
                                "unable to weave such pattern using %s",
                                this.weaveType.toString()
                        )
                );
                return;
            }

            this.pattern = builder.getPattern();
            this.actionBody = this.astNodes.getStmtList();
        } catch (Backtrace backtrace) {
            report.Add(backtrace.getBacktraceMsg());
            return;
        }
    }

    public Pattern getPattern() {
        if (this.pattern == null) throw new UnsupportedOperationException();
        return pattern;
    }

    public IReport getReport() {
        return report;
    }

    public List<Stmt> getActionBody() {
        if (this.actionBody == null) throw new UnsupportedOperationException();
        return actionBody;
    }

    @Deprecated
    public static Map<String, Expr> parsePatternSection(Patterns patterns) {
        Map<String, Expr> stringExprMap = new HashMap<>();
        for (ast.Pattern pattern : patterns.getPatternList()) {
            String patternID = pattern.getName();
            Expr patternExpr = pattern.getExpr();
            if (stringExprMap.keySet().contains(patternID)) {
                throw new RuntimeException();
            }
            stringExprMap.put(patternID, patternExpr);
        }
        return stringExprMap;
    }
}
