package transformer.util;

import util.Mergeable;

public class IsPossibleJointPointResult implements Cloneable, Mergeable<IsPossibleJointPointResult> {
    public boolean isAnnotates = false;
    public boolean isCalls = false;
    public boolean isExecutions = false;
    public boolean isGets = false;
    public boolean isLoops = false;
    public boolean isLoopBodies = false;
    public boolean isLoopHeads = false;
    public boolean isMainExecutions = false;
    public boolean isOperators = false;
    public boolean isSets = false;

    @SuppressWarnings("deprecation")
    public IsPossibleJointPointResult andMerge(IsPossibleJointPointResult target) {
        /* this method will be called upon merging lhs and rhs of a and pattern */
        /* it will return the intersection of two set (AND relationship)        */
        /* using intersection                                                   */
        return intersection(target);
    }

    @SuppressWarnings("deprecation")
    public IsPossibleJointPointResult orMerge(IsPossibleJointPointResult target) {
        /* this method will be called upon merging lhs and rhs of a or pattern  */
        /* it will return the union of two set (OR relationship)                */
        /* using intersection                                                   */
        return union(target);
    }

    public void reset() {
        /* this method will clear all records and reset the result */
        this.isAnnotates = false;
        this.isCalls = false;
        this.isExecutions = false;
        this.isGets = false;
        this.isLoops = false;
        this.isLoopBodies = false;
        this.isLoopHeads = false;
        this.isMainExecutions = false;
        this.isOperators = false;
        this.isSets = false;
    }

    @Override
    public IsPossibleJointPointResult clone() {
        return null;
    }

    @Override @Deprecated @SuppressWarnings("deprecation")
    public IsPossibleJointPointResult merge(IsPossibleJointPointResult target) {
        /* default merge */
        return union(target);
    }

    @Override @Deprecated
    public IsPossibleJointPointResult intersection(IsPossibleJointPointResult target) {
        IsPossibleJointPointResult result = new IsPossibleJointPointResult();
        result.isAnnotates = this.isAnnotates && target.isAnnotates;
        result.isCalls = this.isCalls && target.isCalls;
        result.isExecutions = this.isExecutions && target.isExecutions;
        result.isGets = this.isGets && target.isGets;
        result.isLoops = this.isLoops && target.isLoops;
        result.isLoopBodies = this.isLoopBodies && target.isLoopBodies;
        result.isLoopHeads = this.isLoopHeads && target.isLoopHeads;
        result.isMainExecutions = this.isMainExecutions && target.isMainExecutions;
        result.isOperators = this.isOperators && target.isOperators;
        result.isSets = this.isSets && target.isSets;
        return result;
    }

    @Override @Deprecated
    public IsPossibleJointPointResult union(IsPossibleJointPointResult target) {
        IsPossibleJointPointResult result = new IsPossibleJointPointResult();
        result.isAnnotates = this.isAnnotates || target.isAnnotates;
        result.isCalls = this.isCalls || target.isCalls;
        result.isExecutions = this.isExecutions || target.isExecutions;
        result.isGets = this.isGets || target.isGets;
        result.isLoops = this.isLoops || target.isLoops;
        result.isLoopBodies = this.isLoopBodies || target.isLoopBodies;
        result.isLoopHeads = this.isLoopHeads || target.isLoopHeads;
        result.isMainExecutions = this.isMainExecutions || target.isMainExecutions;
        result.isOperators = this.isOperators || target.isOperators;
        result.isSets = this.isSets || target.isSets;
        return result;
    }

    public boolean isPossible() {
        if (isAnnotates) return true;
        if (isCalls) return true;
        if (isExecutions) return true;
        if (isGets) return true;
        if (isLoops) return true;
        if (isLoopBodies) return true;
        if (isLoopHeads) return true;
        if (isMainExecutions) return true;
        if (isOperators) return true;
        if (isSets) return true;
        /* control flow should not reach here */
        throw new AssertionError();
    }
}
