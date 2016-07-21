aspect_ demo
	patterns
		p : p  %pattern p is depend on itself
	end
	actions
		a : before (annotate(x(var)) | get(x)) & istype(double) : ()
			% codes here
		end
	end
end