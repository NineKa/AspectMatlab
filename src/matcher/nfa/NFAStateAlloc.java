package matcher.nfa;

import matcher.alphabet.Alphabet;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class NFAStateAlloc {
    private Alphabet alphabet = null;
    private NFA parent = null;
    private int stateCounter = 0;

    private Map<Integer, NFANode> generateMap = new HashMap<>();

    public NFAStateAlloc(Alphabet pAlphabet, NFA pParent) {
        this.alphabet = pAlphabet;
        this.parent = pParent;
    }

    public int size() { return this.stateCounter; }

    public NFANode getState(int index) {
        if (index >= this.size()) throw new IndexOutOfBoundsException();
        return generateMap.get(index);
    }

    public NFANode getNewState() {
        NFANode retNewNode = new NFANode(this.alphabet, this.parent, this.stateCounter);
        this.generateMap.put(this.stateCounter, retNewNode);
        this.stateCounter = this.stateCounter + 1;
        return retNewNode;
    }

    public Collection<NFANode> getNodesSet() {
        Collection<NFANode> retSet = new HashSet<>();
        for (int iter : generateMap.keySet()) retSet.add(this.generateMap.get(iter));
        return retSet;
    }

    public boolean hasState(NFANode state) { return this.getNodesSet().contains(state); }
}
