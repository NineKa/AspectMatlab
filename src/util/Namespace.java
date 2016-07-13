package util;

import java.util.HashSet;
import java.util.Set;

public abstract class Namespace {
    protected Set<String> definedNameSet = new HashSet<>();

    public abstract String generateNewName();

    public void removeName(String varName) { this.definedNameSet.remove(varName); }

    public boolean isDefined(String varName) { return this.definedNameSet.contains(varName); }

    public Set<String> getDefinedNameSet() {return this.definedNameSet; }
}
