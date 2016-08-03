package matcher.nfa;

import ast.*;
import matcher.alphabet.Alphabet;

public class NFAFactory {
    public static NFANode getLastGeneratedNode(NFA nfa) {
        NFAStateAlloc allocator = nfa.getAllocator();
        if (allocator.size() == 0) return null;
        return nfa.getState(allocator.size() - 1);
    }

    @SuppressWarnings("deprecation")
    public static NFA buildNFAfromDimension(Alphabet<Integer> alphabet, List<Name> signature) {
        NFA retNFA = new NFA(alphabet);
        NFAStateAlloc allocator = retNFA.getAllocator();
        if (signature.getNumChild() == 0) {
            /* trivial construction */
            NFANode entryAndAccespt = allocator.getNewState();
            retNFA.setEntryState(entryAndAccespt);
            retNFA.addAcceptState(entryAndAccespt);
            return retNFA;
        }
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

    @SuppressWarnings("deprecation")
    public static NFA buildNFAfromType(Alphabet<String> alphabet, List<Name> signature) {
        NFA retNFA = new NFA(alphabet);
        NFAStateAlloc allocator = retNFA.getAllocator();
        if (signature.getNumChild() == 0) {
            /* trivial construction */
            NFANode entryAndAccespt = allocator.getNewState();
            retNFA.setEntryState(entryAndAccespt);
            retNFA.addAcceptState(entryAndAccespt);
            return retNFA;
        }
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

    @SuppressWarnings("deprecation")
    public static NFA buildNFAfromAnnotateSelector(java.util.List<String> selectorSignature) {
        Alphabet<Class<? extends Expr>> alphabet = new Alphabet<>();
        alphabet.add(FPLiteralExpr.class);
        alphabet.add(IntLiteralExpr.class);
        alphabet.add(StringLiteralExpr.class);
        alphabet.add(NameExpr.class);

        NFA retNFA = new NFA(alphabet);
        NFAStateAlloc allocator = retNFA.getAllocator();
        if (selectorSignature.isEmpty()) {
            /* trivial construction */
            NFANode entryAndAccespt = allocator.getNewState();
            retNFA.setEntryState(entryAndAccespt);
            retNFA.addAcceptState(entryAndAccespt);
            return retNFA;
        }

        for (String iter : selectorSignature) {
            NFANode previousState = getLastGeneratedNode(retNFA);
            if (previousState == null) {
                previousState = allocator.getNewState();
                retNFA.setEntryState(previousState);
            }
            if (iter.equals("var")) {
                NFANode nextState = allocator.getNewState();
                int stateTransferCode = alphabet.getLetterCode(NameExpr.class);
                previousState.addEdge(stateTransferCode, nextState);
                continue;
            }
            if (iter.equals("str")) {
                NFANode nextState = allocator.getNewState();
                int stateTransferCode = alphabet.getLetterCode(StringLiteralExpr.class);
                previousState.addEdge(stateTransferCode, nextState);
                continue;
            }
            if (iter.equals("num")) {
                NFANode nextState = allocator.getNewState();
                int stateTransferCodeInt = alphabet.getLetterCode(IntLiteralExpr.class);
                int stateTransferCodeFP  = alphabet.getLetterCode(FPLiteralExpr.class);
                previousState.addEdge(stateTransferCodeInt, nextState);
                previousState.addEdge(stateTransferCodeFP, nextState);
                continue;
            }
            if (iter.equals("*")) {
                NFANode nextState = allocator.getNewState();
                int stateTransferCodeStr = alphabet.getLetterCode(StringLiteralExpr.class);
                int stateTransferCodeInt = alphabet.getLetterCode(IntLiteralExpr.class);
                int stateTransferCodeFP  = alphabet.getLetterCode(FPLiteralExpr.class);
                int stateTransferCodeVar = alphabet.getLetterCode(NameExpr.class);
                previousState.addEdge(stateTransferCodeStr, nextState);
                previousState.addEdge(stateTransferCodeInt, nextState);
                previousState.addEdge(stateTransferCodeFP, nextState);
                previousState.addEdge(stateTransferCodeVar, nextState);
                continue;
            }
            if (iter.equals("..")) {
                NFANode state2Node = allocator.getNewState();
                NFANode state3Node = allocator.getNewState();
                NFANode state4Node = allocator.getNewState();
                int stateTransferCodeStr = alphabet.getLetterCode(StringLiteralExpr.class);
                int stateTransferCodeInt = alphabet.getLetterCode(IntLiteralExpr.class);
                int stateTransferCodeFP  = alphabet.getLetterCode(FPLiteralExpr.class);
                int stateTransferCodeVar = alphabet.getLetterCode(NameExpr.class);
                previousState.addEdge(alphabet.getEpsilonTransitionCode(), state2Node);
                state2Node.addEdge(stateTransferCodeStr, state3Node);
                state2Node.addEdge(stateTransferCodeInt, state3Node);
                state2Node.addEdge(stateTransferCodeFP, state3Node);
                state2Node.addEdge(stateTransferCodeVar, state3Node);
                state2Node.addEdge(alphabet.getEpsilonTransitionCode(), state3Node);
                state3Node.addEdge(alphabet.getEpsilonTransitionCode(), state4Node);
                state4Node.addEdge(alphabet.getEpsilonTransitionCode(), previousState);
                continue;
            }
            /* control flow should not reach here */
            throw new AssertionError();
        }
        retNFA.addAcceptState(getLastGeneratedNode(retNFA));
        return retNFA;
    }
}
