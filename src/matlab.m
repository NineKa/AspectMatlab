aspect_ myAspect

actions
    a : before (get(x) | set(x)) & (istype(type) & dimension([3,3])) & within(function : foo) : ()

    end
end

end