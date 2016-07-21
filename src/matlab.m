aspect_ myAspect

patterns
    pCallFoo : call(foo()) & within(loop : *)
end

actions
    a : before pCallFoo | execution(foo()) : ()

    end
end

end