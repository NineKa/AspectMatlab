#include "mex.h"
#include "matrix.h"
#include <stdio.h>
#include <stdlib.h>

#define SIG(x)                   #x
#define TOSTR(x)                 SIG(x)
#define mxQuickAssert(assertion) mxAssert(assertion, "@"TOSTR(__LINE__));

mwIndex getCellLength(const mxArray* cellPtr) {
	mxQuickAssert(mxGetClassID(cellPtr) == mxCELL_CLASS);
	mxArray** outputValue = (mxArray**)mxMalloc(sizeof(mxArray*) * 1);
	mxArray** intputValue = (mxArray**)mxMalloc(sizeof(mxArray*) * 1);
	mxQuickAssert(outputValue != NULL);
	mxQuickAssert(intputValue != NULL);
	intputValue[0] = (mxArray*) cellPtr;
	int exec = mexCallMATLAB(1, outputValue, 1, intputValue, "length");
	mxQuickAssert(exec == 0);
	mxQuickAssert(mxIsScalar(outputValue[0]));
	double matlabRet = mxGetScalar(outputValue[0]);
	mxFree(outputValue);
	mxFree(intputValue);
	return (mwIndex)matlabRet;
}

int invoke(const mxArray* func, const mxArray* inPramCell, int numOutPram, mxArray** outPram) {
	mxQuickAssert(mxGetClassID(func) == mxFUNCTION_CLASS);
	mxQuickAssert(mxGetClassID(inPramCell) == mxCELL_CLASS);

	int numInPram = getCellLength(inPramCell) + 1;

	mxArray** inPram = (mxArray**) mxMalloc(sizeof(mxArray*) * numInPram);
	mxQuickAssert(inPram != NULL);
	inPram[0] = (mxArray*) func;
	for (int iter = 1; iter < numInPram; iter++) {
		int subs[2]; subs[0] = 0; subs[1] = iter - 1;
		mwIndex accessIndex = mxCalcSingleSubscript(inPramCell, 2, subs);
		mxArray* candidate = mxGetCell(inPramCell, accessIndex);
		inPram[iter] = candidate;
	}
	int exec = mexCallMATLAB(numOutPram, outPram, numInPram, inPram, "feval");
	mxFree(inPram);
	return exec;
}

mxArray* applyMatcher(mxArray* candidateCell, mxArray* matcher) {
	mxQuickAssert(mxGetClassID(candidateCell) == mxCELL_CLASS);
	mxQuickAssert(mxGetClassID(matcher) == mxFUNCTION_CLASS);

	mxArray** inPram = (mxArray**) mxMalloc(sizeof(mxArray*) * 2);
	mxArray** outPram = (mxArray**) mxMalloc(sizeof(mxArray*) * 2);
	const int numInPram = 2;
	const int numOutPram = 1;

	inPram[0] = matcher;
	inPram[1] = candidateCell;

	int exec = mexCallMATLAB(numOutPram, outPram, numInPram, inPram, "feval");

	mxQuickAssert(mxIsLogicalScalar(outPram[0]));
	mxFree(inPram);
	mxFree(outPram);
	return outPram[0];
}
/*
    callWithMatcher(
        @invokeTarget,          target function for return values matcher
        {invoke args}           arguments applied to the invoke target
        @matcher...             list of matcher to the function call
    )

    @ret resultMatrix           a 1xn logical matrix containing the result for matcher matching
    @ret varargout              the return value of the invoked function

    compile this using mex command in MATLAB
*/
void mexFunction(int nlhs, mxArray** plhs, int nrhs, const mxArray** prhs) {
	#define numInPram		 nrhs
	#define targetFuncHandle prhs[0]
	#define targetFuncInPram prhs[1]
	#define inPram           prhs
	#define numOutPram		 nlhs
	#define outPram  		 plhs

	mxQuickAssert(numInPram >= 2);
	mxQuickAssert(numOutPram >= 1);
	mxQuickAssert(mxGetClassID(targetFuncHandle) == mxFUNCTION_CLASS);
	mxQuickAssert(mxGetClassID(targetFuncInPram) == mxCELL_CLASS);
	
	const int numInvokeOutPram = numOutPram - 1;
	const mxArray* invokeTarget = targetFuncHandle;
	mxArray** invokeOutPram = (mxArray**) mxMalloc(sizeof(mxArray*) * (numInvokeOutPram + 1));
	int invokeExec = invoke(invokeTarget, targetFuncInPram, numInvokeOutPram, invokeOutPram);

	mxArray* invokeOutCell = mxCreateCellMatrix(1, numInvokeOutPram);
	mxQuickAssert(invokeOutPram != NULL);
	for (int index = 0; index < numInvokeOutPram; index++) {
		int subs[2]; subs[0] = 0; subs[1] = index;
		mwIndex accessIndex = mxCalcSingleSubscript(invokeOutCell, 2, subs);
		mxArray* candidate = invokeOutPram[index];
		mxSetCell(invokeOutCell, accessIndex, candidate);
	}
	
	int totalMatcherNum = numInPram - 2;
	mxLogical* rawRetMatrix = (mxLogical*)mxCalloc(totalMatcherNum, sizeof(mxLogical)); 
	for (int matcherIndex = 0; matcherIndex < totalMatcherNum; matcherIndex++) {
		mxArray* matcher = (mxArray*) inPram[matcherIndex + 2];
		mxQuickAssert(mxGetClassID(matcher) == mxFUNCTION_CLASS);

		mxArray* result = applyMatcher(invokeOutCell, matcher);
		rawRetMatrix[matcherIndex] = *mxGetLogicals(result);
	}

	outPram[0] = mxCreateLogicalMatrix(1, totalMatcherNum);
	mxLogical* ptr = mxGetLogicals(outPram[0]);
	for (int index = 0; index < totalMatcherNum; index++) {
		*ptr = rawRetMatrix[index];
		ptr++;
	}

	for (int index = 0; index < numInvokeOutPram; index++) {
		outPram[index + 1] = mxDuplicateArray(invokeOutPram[index]);
	}

	mxFree(invokeOutPram);
}