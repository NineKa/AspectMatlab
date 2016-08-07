function [varargout] = foo(varargin)
    %@loopname y
    %@loopname x,z
    for i = 1 : 10 %comment3
        %@ann a1
        %@ann a2
        %@loopname loop2
        for k = 1 : 100

        end

        %@ann a3
    end

    %@loopname invalid
    x = 10;

    %@loopname while
    while x < 10

    end

end