aspect_ demo
	patterns
		p : p  %pattern p is depend on itself
	end
	actions
        a : after op(+) | get(*) | call(*(..))  : ()

		end
	end
end