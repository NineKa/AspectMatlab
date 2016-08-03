package transformer;

import util.Mergeable;

public class IsPossibleJointPointResult implements Mergeable<IsPossibleJointPointResult>, Cloneable {
    public boolean isAnnotate = false;
    public boolean isCall = false;
    public boolean isExecution = false;
    public boolean isGet = false;
    public boolean isLoop = false;
    public boolean isLoopBody = false;
    public boolean isLoopHead = false;
    public boolean isMainExecution = false;
    public boolean isOperator = false;
    public boolean isSet = false;

    @Override
    public String toString() {
        return String.format(
                "[annotate: %b, call: %b, execution: %b, get: %b, loop:%b, " +
                        "loopbody: %b, loophead: %b, mainexecution: %b, operator: %b, set: %b]",
                isAnnotate,isCall,isExecution,isGet,isLoop,
                isLoop,isLoopBody, isLoopHead, isMainExecution, isOperator, isSet
        );
    }

    @Override
    public IsPossibleJointPointResult merge(IsPossibleJointPointResult target) {
        IsPossibleJointPointResult retResult = new IsPossibleJointPointResult();
        retResult.isAnnotate = this.isAnnotate || target.isAnnotate;
        retResult.isCall = this.isCall || target.isCall;
        retResult.isExecution = this.isExecution || target.isExecution;
        retResult.isGet = this.isGet || target.isGet;
        retResult.isLoop = this.isLoop || target.isLoop;
        retResult.isLoopHead = this.isLoopHead || target.isLoopHead;
        retResult.isLoopBody = this.isLoopBody || target.isLoopBody;
        retResult.isMainExecution = this.isMainExecution || target.isMainExecution;
        retResult.isOperator = this.isOperator || target.isOperator;
        retResult.isSet = this.isSet || target.isSet;
        return retResult;
    }

    @Override
    public IsPossibleJointPointResult clone() {
        IsPossibleJointPointResult clonedObj = new IsPossibleJointPointResult();
        clonedObj.isAnnotate = this.isAnnotate;
        clonedObj.isCall = this.isCall;
        clonedObj.isExecution = this.isExecution;
        clonedObj.isGet = this.isGet;
        clonedObj.isLoop = this.isLoop;
        clonedObj.isLoopBody = this.isLoopBody;
        clonedObj.isLoopHead = this.isLoopHead;
        clonedObj.isMainExecution = this.isMainExecution;
        clonedObj.isOperator = this.isOperator;
        clonedObj.isSet = this.isSet;
        return clonedObj;
    }
}
