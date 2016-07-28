package abstractPattern;

import Matlab.Utils.IReport;
import abstractPattern.modifier.*;
import abstractPattern.type.WeaveType;

import java.util.*;
import java.util.function.Function;

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

    public abstract Map<WeaveType, Boolean> getWeaveInfo();

    public void reformatModifier() {
        /* peephole reformat on tree (remove doubleNeg, logical reduction) */
        Function<Modifier, Modifier> doubleNegReduce = new Function<Modifier, Modifier>() {
            @Override
            @SuppressWarnings("ann-dep")
            public Modifier apply(Modifier modifier) {
                /* check double negation */
                if (modifier instanceof ModifierNot && ((ModifierNot) modifier).getOperand() instanceof ModifierNot) {
                    return this.apply(((ModifierNot) ((ModifierNot) modifier).getOperand()).getOperand());
                }
                /* recursive */
                if (modifier instanceof Dimension) return modifier;
                if (modifier instanceof IsType) return modifier;
                if (modifier instanceof Within) return modifier;
                if (modifier instanceof ModifierAnd) {
                    ((ModifierAnd) modifier).setLHS(this.apply(((ModifierAnd) modifier).getLHS()));
                    ((ModifierAnd) modifier).setRHS(this.apply(((ModifierAnd) modifier).getRHS()));
                    return modifier;
                }
                if (modifier instanceof ModifierOr) {
                    ((ModifierOr) modifier).setLHS(this.apply(((ModifierOr) modifier).getLHS()));
                    ((ModifierOr) modifier).setRHS(this.apply(((ModifierOr) modifier).getRHS()));
                    return modifier;
                }
                if (modifier instanceof ModifierNot) {
                    ((ModifierNot) modifier).setOperand(this.apply(((ModifierNot) modifier).getOperand()));
                    return modifier;
                }
                /* control flow should not reach here */
                throw new AssertionError();
            }
        };
    }

    // public abstract boolean isPossibleJointPoint(ASTNode astNode, RuntimeInfo runtimeInfo);
}
