aspect_ demo
	patterns
		p : p  %pattern p is depend on itself
	end
	actions
        a : after call(foo()) & istype(double) : ()
			% codes here
		end
	end
end