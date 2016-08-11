function [ret, varargout] = callWithMatcherMATLAB(funcHandle, pramCell, varargin)

    %@type funcHandle 'function_handle'
    %@type pramCell   'cell'
    %@type varargin   'cell'

    numMatcherCase = nargin - 2;
    ret = true(1, numMatcherCase);

    funcHandleRetCell = cell(1, nargout - 1);
    [funcHandleRetCell{:}] = funcHandle(pramCell{:});

    for i = 1 : numMatcherCase
        matcherHandle = varargin{i};
        %@type matcherHandle 'function_handle'
        ret(i) = matcherHandle(funcHandleRetCell);
    end

    varargout = funcHandleRetCell;
end