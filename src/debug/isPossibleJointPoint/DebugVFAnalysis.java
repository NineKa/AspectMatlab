package debug.isPossibleJointPoint;

import ast.ASTNode;
import ast.Name;
import natlab.toolkits.analysis.varorfun.VFDatum;
import natlab.toolkits.analysis.varorfun.VFFlowInsensitiveAnalysis;
import natlab.toolkits.filehandling.FunctionOrScriptQuery;

import java.util.HashMap;
import java.util.Map;

@Deprecated /* Debug usage only */
public class DebugVFAnalysis extends VFFlowInsensitiveAnalysis{
    private Map<Name, VFDatum> overrideData = new HashMap<>();

    public DebugVFAnalysis(ASTNode astNode, FunctionOrScriptQuery query) {
        super(astNode, query);
    }

    @Deprecated @SuppressWarnings("deprecation")
    public DebugVFAnalysis(ASTNode astNode) {
        super(astNode);
    }

    public void override(Name nodes, VFDatum vfDatum) {
        if (overrideData.keySet().contains(nodes)) return;
        this.overrideData.put(nodes, vfDatum);
    }

    public void overrideAsFunction(Name nodes) {
        if (overrideData.keySet().contains(nodes)) {
            assert overrideData.get(nodes).isFunction();
            return;
        }
        this.overrideData.put(nodes, VFDatum.FUN);
    }

    public void overrideAsVariable(Name nodes) {
        if (overrideData.keySet().contains(nodes)) {
            assert overrideData.get(nodes).isVariable();
            return;
        }
        this.overrideData.put(nodes, VFDatum.VAR);
    }

    public void overrideAsIdentifier(Name nodes) {
        if (overrideData.keySet().contains(nodes)) {
            assert overrideData.get(nodes).isID();
            return;
        }
        this.overrideData.put(nodes, VFDatum.BOT);
    }

    @Override
    public VFDatum getResult(Name name) {
        if (this.overrideData.containsKey(name)) return this.overrideData.get(name);
        return super.getResult(name);
    }
}
