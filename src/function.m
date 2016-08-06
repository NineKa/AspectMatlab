function [varargout] = foo(varargin)
    %@loopname y
    %@loopname x
    for i = 1 : 10 %comment3

        %@loopname loop2
        for k = 1 : 100

        end

    end

    %@loopname invalid
    x = 10;

    %@loopname while
    while x < 10

    end

end