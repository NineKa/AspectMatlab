aspect_ demo
	patterns
		p : p  %pattern p is depend on itself
	end
	actions
        a1 : after call(*(..)) : ()

        end

        a2 : after get(*) : ()

        end

        a3 : after op(-) : ()

        end

        a4 : after op(+) : ()

        end

        a5 : after set(*) : ()

        end
	end
end