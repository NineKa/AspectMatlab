aspect_ demo
	patterns
		p : p  %pattern p is depend on itself
	end
	actions
        a : after call(foo(int[3,3])) & ~~~(istype(double) & dimension([3,3])) : ()

		end
	end
end