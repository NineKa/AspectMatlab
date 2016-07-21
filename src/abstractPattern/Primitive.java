package abstractPattern;

import Matlab.Utils.IReport;
import Matlab.Utils.Report;
import abstractPattern.modifier.ModifierAnd;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public abstract class Primitive extends Pattern{
    private List<Modifier> modifiers = new LinkedList<>();

    public void addModifier(Modifier modifier) {
        if (modifier instanceof ModifierAnd) {
            this.addModifier(((ModifierAnd)modifier).getLHS());
            this.addModifier(((ModifierAnd)modifier).getRHS());
        } else {
            this.modifiers.add(modifier);
        }
    }

    public boolean isModified() {
        return !this.modifiers.isEmpty();
    }

    public List<Modifier> getModifiers(){
        return this.modifiers;
    }

    public Collection<Modifier> getBadicModifierSet() {
        Collection<Modifier> collection = new HashSet<>();
        for (Modifier modifier: this.getModifiers()) {
            collection.addAll(modifier.getAllModfiierSet());
        }
        return collection;
    }

    public abstract boolean isProperlyModified();

    public abstract IReport getModifierValidationReport(String pFilepath);
}
