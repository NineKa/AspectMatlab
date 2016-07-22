aspect_ demo
	patterns
		p : p  %pattern p is depend on itself
	end
	actions
		a : before (annotate(x(var)) | set(x : int[3,3])) & istype(double) : ()
			% codes here
		end
	end
end