function [] = foo()
    x = 20;
    x = (10 && 20) || (10 && (2 .* 3));
end