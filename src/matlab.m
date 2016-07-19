aspect_ myAspect

actions
    a : before (get(*) & set(*)) & (within(aspect : a) & within(function : b)) : ()

    end
end

end