package matcher.nfa;

import matcher.alphabet.Alphabet;
import util.MergeableCollection;
import util.MergeableHashSet;

import java.util.*;

public class NFANode implements Iterable<AbstractMap.SimpleEntry<Integer, NFANode>>{
    private Alphabet alphabet = null;
    private NFA parent = null;
    private int stateNumber = 0;

    public NFANode(Alphabet pAlphabet, NFA pParent, int pStateNumber) {
        this.alphabet = pAlphabet;
        this.parent = pParent;
        this.stateNumber = pStateNumber;
    }

    private Set<AbstractMap.SimpleEntry<Integer, NFANode>> stateTransferMap = new HashSet();

    public void addEdge(int stateTransferCode, NFANode target) {
        AbstractMap.SimpleEntry<Integer, NFANode> appendingEdge = new AbstractMap.SimpleEntry<Integer, NFANode>(
                stateTransferCode,
                target
        );
        this.stateTransferMap.add(appendingEdge);
    }

    public MergeableCollection<NFANode> getReachableNodeSet(int stateTransferCode) {
        MergeableCollection<NFANode> returnSet = new MergeableHashSet<>();
        Collection<NFANode> searchSet = this.getEpsilonClosureSet();
        for (NFANode searchIter : searchSet) {
            MergeableCollection<NFANode> tempSet = new MergeableHashSet<>();
            for (AbstractMap.SimpleEntry<Integer, NFANode> iter : searchIter) {
                if (iter.getKey().intValue() != stateTransferCode) continue;
                MergeableCollection<NFANode> nfaColosure = iter.getValue().getEpsilonClosureSet();
                tempSet = tempSet.merge(nfaColosure);
            }
            returnSet = returnSet.merge(tempSet);
        }
        return returnSet;
    }

    public MergeableCollection<NFANode> getEpsilonClosureSet() {
        MergeableCollection<NFANode> retSet = new MergeableHashSet<>();
        this.getEpsilonClosureSet(retSet);
        return retSet;
    }

    private void getEpsilonClosureSet(MergeableCollection<NFANode> set) {
        int epsilonTransferCode = this.alphabet.getEpsilonTransitionCode();
        if (set.contains(this)) return;
        set.add(this);
        for (AbstractMap.SimpleEntry<Integer, NFANode> iter : this.stateTransferMap) {
            /* current transition is not a epsilon transition */
            if (iter.getKey().intValue() != epsilonTransferCode) continue;
            iter.getValue().getEpsilonClosureSet(set);
        }
    }

    public int getStateNumber() { return this.stateNumber; }

    public Iterator<AbstractMap.SimpleEntry<Integer, NFANode>> iterator() {
        return this.stateTransferMap.iterator();
    }

    @Override public String toString() {
        Collection<Integer> stateSet = new HashSet<>();
        for (AbstractMap.SimpleEntry<Integer, NFANode> iter : this) stateSet.add(iter.getValue().stateNumber);
        return String.format(
                "State#%d:%s",
                this.stateNumber,
                stateSet.toString()
        );
    }
}
