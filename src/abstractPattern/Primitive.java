package abstractPattern;

import java.util.Collection;
import java.util.HashSet;

public abstract class Primitive extends Pattern{
    private Collection<Modifier> modifiers = new HashSet<>();

    public void addModifier(Modifier modifier) {
        this.modifiers.add(modifier);
    }

    public boolean isModified() {
        return !this.modifiers.isEmpty();
    }

    public Collection<Modifier> getModifiers(){
        return this.modifiers;
    }
}
