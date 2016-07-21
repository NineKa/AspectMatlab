package abstractPattern;

import abstractPattern.modifier.ModifierAnd;

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
}
