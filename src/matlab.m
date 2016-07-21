aspect_ demo
	patterns
		p : p  %pattern p is depend on itself
	end
	actions
		a : before (get(x) | set(x)) & within(function : foo) : ()
			% codes here
		end
	end
end