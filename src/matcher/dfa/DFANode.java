package matcher.dfa;

import matcher.Alphabet;
import matcher.nfa.NFANode;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class DFANode {
    private Collection<NFANode> NFASubset = new HashSet<>();
    private Map<Integer, DFANode> stateTransferMap = new HashMap<>();
    private DFA parent = null;
    private Alphabet alphabet = null;
    private int stateNumber = 0;

    public DFANode(Alphabet pAlphabet, DFA pParent, int pStateNumber) {
        this.parent = pParent;
        this.alphabet = pAlphabet;
        this.stateNumber = pStateNumber;
    }

    public int getStateNumber() { return this.stateNumber; }

    public void addNFANode(NFANode node) {
        if (NFASubset.contains(node)) return;
        this.NFASubset.add(node);
    }

    public void addNFANode(Collection<NFANode> nodes) { for (NFANode iter : nodes) this.addNFANode(iter); }

    public boolean hasSameNFASubset(Collection<NFANode> pSubset) {
        for (NFANode iter : this.NFASubset) if (!pSubset.contains(iter)) return false;
        for (NFANode iter : pSubset) if (!this.NFASubset.contains(iter)) return false;
        return true;
    }

    public boolean hasSameNFASubset(DFANode pNode) { return this.hasSameNFASubset(pNode.getNFASubset()); }

    public Collection<Integer> getAvaliableTransfer() { return this.stateTransferMap.keySet(); }

    public void addEdge(int stateTransferCode, DFANode target) {
        if (this.stateTransferMap.keySet().contains(stateTransferCode) &&
                !this.stateTransferMap.get(stateTransferCode).equals(target)) {
            throw new RuntimeException("Invalid DFA Construction");
        }
        if (this.stateTransferMap.keySet().contains(stateTransferCode) &&
                this.stateTransferMap.get(stateTransferCode).equals(target)) {
            /* duplicate Edge -> Ignored */
            return;
        }
        this.stateTransferMap.put(stateTransferCode, target);
    }

    public DFANode transfer(int stateTransferCode) {
        if (!this.stateTransferMap.keySet().contains(stateTransferCode))
            throw new IndexOutOfBoundsException();
        return this.stateTransferMap.get(stateTransferCode);
    }

    public Collection<NFANode> getNFASubset() { return this.NFASubset; }

    @Override public String toString() {
        Map<Integer, Integer> toStringMap = new HashMap<>();
        for (int iter : this.stateTransferMap.keySet()) {
            toStringMap.put(iter, this.stateTransferMap.get(iter).stateNumber);
        }
        return String.format(
                "State#%d:%s",
                this.stateNumber,
                toStringMap.toString()
        );
    }
}
