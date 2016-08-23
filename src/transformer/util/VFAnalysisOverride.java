package transformer.util;

import ast.ASTNode;
import ast.Name;
import natlab.toolkits.analysis.varorfun.VFAnalysis;
import natlab.toolkits.analysis.varorfun.VFDatum;
import natlab.toolkits.analysis.varorfun.VFFlowInsensitiveAnalysis;
import natlab.toolkits.filehandling.FunctionOrScriptQuery;

import java.util.HashMap;
import java.util.Map;

public final class VFAnalysisOverride extends VFFlowInsensitiveAnalysis implements VFAnalysis {
    private Map<Name, VFDatum> overrideMap = new HashMap<>();

    public VFAnalysisOverride(ASTNode target, FunctionOrScriptQuery query) {
        super(target, query);
    }

    @Deprecated
    @SuppressWarnings("deprecation")
    public VFAnalysisOverride(ASTNode target) {
        super(target);
    }

    @Override
    public VFDatum getResult(Name name) {
        if (overrideMap.keySet().contains(name)) {
            return overrideMap.get(name);
        } else {
            return super.getResult(name);
        }
    }

    public void override(Name name, VFDatum vfDatum) {
        overrideMap.put(name, vfDatum);
    }

    public void removeOverride(Name name) {
        assert overrideMap.keySet().contains(name);
        overrideMap.remove(name);
    }
}
