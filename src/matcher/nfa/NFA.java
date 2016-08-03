package matcher.nfa;

import matcher.alphabet.Alphabet;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

public class NFA implements Iterable<NFANode>{
    private NFAStateAlloc alloc = null;
    private Alphabet alphabet = null;

    private Collection<NFANode> acceptStates = new HashSet<>();
    private NFANode entryState = null;

    @Deprecated
    public NFA(Alphabet pAlphabet) {
        this.alphabet = pAlphabet;
        this.alloc = new NFAStateAlloc(this.alphabet, this);
    }

    public NFANode getState(int index) { return this.alloc.getState(index); }

    public NFAStateAlloc getAllocator() { return this.alloc; }

    public NFANode getEntryState() { return this.entryState; }

    public Collection<NFANode> getAcceptStates() { return this.acceptStates; }

    public boolean isAccpetState(NFANode state) { return this.acceptStates.contains(state); }

    public boolean isValid() {
        if (alphabet == null) return false;
        if (alloc == null) return false;
        if (acceptStates.isEmpty()) return false;
        if (entryState == null) return false;
        return true;
    }

    public void setEntryState(NFANode state) {
        assert(this.alloc.hasState(state));
        this.entryState = state;
    }

    public void addAcceptState(NFANode state) {
        assert(this.alloc.hasState(state));
        if (this.acceptStates.contains(state)) return;
        this.acceptStates.add(state);
    }

    public void setAcceptStates(Collection<NFANode> states) {
        for (NFANode iter : states) {
            if (!this.alloc.hasState(iter)) throw new RuntimeException("Invalid State Setting");
        }
        this.acceptStates = states;
    }

    public Alphabet getAlphabet() { return this.alphabet; }

    @Override public String toString() {
        return alloc.getNodesSet().toString();
    }

    public Iterator<NFANode> iterator() { return this.alloc.getNodesSet().iterator(); }
}
