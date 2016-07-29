aspect_ demo
	patterns
		p : p  %pattern p is depend on itself
	end
	actions
        a : after call(foo()) & (dimension([3,3]) & (istype(x) & istype(y))) : ()
			% codes here
		end
	end
end