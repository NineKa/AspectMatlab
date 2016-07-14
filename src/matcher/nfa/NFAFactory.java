package matcher.nfa;

import ast.List;
import ast.Name;
import matcher.Alphabet;

public class NFAFactory {
    public static NFANode getLastGeneratedNode(NFA nfa) {
        NFAStateAlloc allocator = nfa.getAllocator();
        if (allocator.size() == 0) return null;
        return nfa.getState(allocator.size() - 1);
    }

    public static NFA buildNFAfromDimension(Alphabet<Integer> alphabet, List<Name> signature) {
        NFA retNFA = new NFA(alphabet);
        NFAStateAlloc allocator = retNFA.getAllocator();
        for (Name iter : signature) {
            NFANode previousState = getLastGeneratedNode(retNFA);
            if (previousState == null) {
                previousState = allocator.getNewState();
                retNFA.setEntryState(previousState);
            }
            if (iter.getID().equals("*")) {
                NFANode nextState = allocator.getNewState();
                for (int letter : alphabet) {
                    int stateTransferCode = alphabet.getLetterCode(letter);
                    previousState.addEdge(stateTransferCode, nextState);
                }
                previousState.addEdge(alphabet.getSigmaTransitionCode(), nextState);
            } else if (iter.getID().equals("..")) {
                NFANode state2Node = allocator.getNewState();
                NFANode state3Node = allocator.getNewState();
                NFANode state4Node = allocator.getNewState();
                previousState.addEdge(alphabet.getEpsilonTransitionCode(), state2Node);
                for (int letter : alphabet) {
                    int stateTransferCode = alphabet.getLetterCode(letter);
                    state2Node.addEdge(stateTransferCode, state3Node);
                }
                state2Node.addEdge(alphabet.getSigmaTransitionCode(), state3Node);
                state2Node.addEdge(alphabet.getEpsilonTransitionCode(), state3Node);
                state3Node.addEdge(alphabet.getEpsilonTransitionCode(), state4Node);
                state4Node.addEdge(alphabet.getEpsilonTransitionCode(), previousState);
            } else {
                NFANode nextNode = allocator.getNewState();
                int stateTransferCode = alphabet.getLetterCode(Integer.parseInt(iter.getID()));
                previousState.addEdge(stateTransferCode, nextNode);
            }
        }
        retNFA.addAcceptState(getLastGeneratedNode(retNFA));
        return retNFA;
    }

    public static NFA buildNFAfromType(Alphabet<String> alphabet, List<Name> signature) {
        NFA retNFA = new NFA(alphabet);
        NFAStateAlloc allocator = retNFA.getAllocator();
        for (Name iter : signature) {
            NFANode previousState = getLastGeneratedNode(retNFA);
            if (previousState == null) {
                previousState = allocator.getNewState();
                retNFA.setEntryState(previousState);
            }
            if (iter.getID().equals("*")) {
                NFANode nextState = allocator.getNewState();
                for (String letter : alphabet) {
                    int stateTransferCode = alphabet.getLetterCode(letter);
                    previousState.addEdge(stateTransferCode, nextState);
                }
                previousState.addEdge(alphabet.getSigmaTransitionCode(), nextState);
            } else if (iter.getID().equals("..")) {
                NFANode state2Node = allocator.getNewState();
                NFANode state3Node = allocator.getNewState();
                NFANode state4Node = allocator.getNewState();
                previousState.addEdge(alphabet.getEpsilonTransitionCode(), state2Node);
                for (String letter : alphabet) {
                    int stateTransferCode = alphabet.getLetterCode(letter);
                    state2Node.addEdge(stateTransferCode, state3Node);
                }
                state2Node.addEdge(alphabet.getEpsilonTransitionCode(), state3Node);
                state2Node.addEdge(alphabet.getSigmaTransitionCode(), state3Node);
                state3Node.addEdge(alphabet.getEpsilonTransitionCode(), state4Node);
                state4Node.addEdge(alphabet.getEpsilonTransitionCode(), previousState);
            } else {
                NFANode nextState = allocator.getNewState();
                int stateTransferCode = alphabet.getLetterCode(iter.getID());
                previousState.addEdge(stateTransferCode, nextState);
            }
        }
        retNFA.addAcceptState(getLastGeneratedNode(retNFA));
        return retNFA;
    }
}
