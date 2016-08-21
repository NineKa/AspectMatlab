aspect_ demo
	patterns
		p : p  %pattern p is depend on itself
	end
	actions
        a : after op(+) | call(*(..)) | get(*) | set(*)  : ()

		end
	end
end