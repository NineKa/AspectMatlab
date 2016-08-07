function [] = foo()
    if true %@anno a1
        idle;
        %@anno a2
        idle;
    else
        %@anno a2
        idle;
    end
end