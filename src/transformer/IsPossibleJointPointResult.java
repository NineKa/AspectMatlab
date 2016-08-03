package transformer;

import abstractPattern.primitive.*;
import util.Mergeable;
import util.MergeableCollection;
import util.MergeableHashSet;

public class IsPossibleJointPointResult implements Cloneable, Mergeable<IsPossibleJointPointResult> {
    public MergeableCollection<Annotate> annotates = new MergeableHashSet<>();
    public MergeableCollection<Call> calls = new MergeableHashSet<>();
    public MergeableCollection<Execution> executions = new MergeableHashSet<>();
    public MergeableCollection<Get> gets = new MergeableHashSet<>();
    public MergeableCollection<Loop> loops = new MergeableHashSet<>();
    public MergeableCollection<LoopBody> loopBodies = new MergeableHashSet<>();
    public MergeableCollection<LoopHead> loopHeads = new MergeableHashSet<>();
    public MergeableCollection<MainExecution> mainExecutions = new MergeableHashSet<>();
    public MergeableCollection<Operator> operators = new MergeableHashSet<>();
    public MergeableCollection<Set> sets = new MergeableHashSet<>();

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
        this.annotates.clear();
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
        result.annotates = this.annotates.intersection(target.annotates);
        result.calls = this.calls.intersection(target.calls);
        result.executions = this.executions.intersection(target.executions);
        result.gets = this.gets.intersection(target.gets);
        result.loops = this.loops.intersection(target.loops);
        result.loopBodies = this.loopBodies.intersection(target.loopBodies);
        result.loopHeads = this.loopHeads.intersection(target.loopHeads);
        result.mainExecutions = this.mainExecutions.intersection(target.mainExecutions);
        result.operators = this.operators.intersection(target.operators);
        result.sets = this.sets.intersection(target.sets);
        return result;
    }

    @Override @Deprecated
    public IsPossibleJointPointResult union(IsPossibleJointPointResult target) {
        IsPossibleJointPointResult result = new IsPossibleJointPointResult();
        result.annotates = this.annotates.union(target.annotates);
        result.calls = this.calls.union(target.calls);
        result.executions = this.executions.union(target.executions);
        result.gets = this.gets.union(target.gets);
        result.loops = this.loops.union(target.loops);
        result.loopBodies = this.loopBodies.union(target.loopBodies);
        result.loopHeads = this.loopHeads.union(target.loopHeads);
        result.mainExecutions = this.mainExecutions.union(target.mainExecutions);
        result.operators = this.operators.union(target.operators);
        result.sets = this.sets.union(target.sets);
        return result;
    }

    public boolean isAnnotate() {
        return !this.annotates.isEmpty();
    }

    public boolean isCall() {
        return !this.calls.isEmpty();
    }

    public boolean isExecution() {
        return !this.executions.isEmpty();
    }

    public boolean isGet() {
        return !this.gets.isEmpty();
    }

    public boolean isLoop() {
        return !this.loops.isEmpty();
    }

    public boolean isLoopBody() {
        return !this.loopBodies.isEmpty();
    }

    public boolean isLoopHead() {
        return !this.loopHeads.isEmpty();
    }

    public boolean isMainExecution() {
        return !this.mainExecutions.isEmpty();
    }

    public boolean isOperator() {
        return !this.operators.isEmpty();
    }

    public boolean isSet() {
        return !this.sets.isEmpty();
    }
}
